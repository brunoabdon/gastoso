/*
 * Copyright (C) 2016 Bruno Abdon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package br.nom.abdon.gastoso.rest.serial;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import br.nom.abdon.gastoso.Conta;
import br.nom.abdon.gastoso.Fato;
import br.nom.abdon.gastoso.Lancamento;
import br.nom.abdon.gastoso.rest.FatoDetalhado;
import br.nom.abdon.gastoso.rest.MediaTypes;

/**
 *
 * @author Bruno Abdon
 */
@Provider
@Consumes({
    MediaTypes.APPLICATION_GASTOSO_SIMPLES,
    MediaTypes.APPLICATION_GASTOSO_NORMAL,
    MediaTypes.APPLICATION_GASTOSO_FULL,
    MediaTypes.APPLICATION_GASTOSO_PATCH,
})
public class GastosoMessageBodyReader implements MessageBodyReader<Object>{
    
    private static final Class[] KNOWN_CLASSES = {
        Conta.class, Fato.class, Lancamento.class
    };
    
    @Override
    public boolean isReadable(
        final Class<?> type, 
        final Type genericType, 
        final Annotation[] annotations, 
        final MediaType mediaType) {

        return 
            Arrays
                .stream(KNOWN_CLASSES)
                .anyMatch(k -> k.isAssignableFrom(type))
                && MediaTypes.acceptGastosoMediaTypes(mediaType);

    }

    @Override
    public Object readFrom(
        final Class<Object> type, 
        final Type genericType, 
        final Annotation[] annotations, 
        final MediaType mediaType,
        final MultivaluedMap<String, String> httpHeaders,
        final InputStream entityStream) 
            throws IOException {
        
        final JsonParser jParser = Serial.JSON_FACT.createParser(entityStream);
        
        return 
            Conta.class.isAssignableFrom(type)
                ? parseConta(jParser, true)
                : Fato.class.isAssignableFrom(type)
                    ? parseFato(jParser, true)
                    : parseLancamento(jParser, true);
    }
    
    
    private Fato parseFato(
                final JsonParser jParser, 
                final boolean hasStartObject)
            throws IOException{
        
        Integer id = null, origemId = null, destinoId = null, contaId = null;
        String descricao = null;
        LocalDate dia = null;
        Integer valor = null;
        
        List<Lancamento> lancamentos = null;
        Conta conta = null, origem = null, destino = null;
        
        if(hasStartObject)jParser.nextToken(); // START_OBJECT
        
        String fieldName;
        
        while((fieldName = jParser.nextFieldName()) != null){
            switch(fieldName) {
                case Serial.ID:
                    id = jParser.nextIntValue(0);
                    break;
                case Serial.CONTA_ID:
                    contaId = jParser.nextIntValue(0);
                    break;
                case Serial.ORIGEM_ID:
                    origemId = jParser.nextIntValue(0);
                    break;
                case Serial.DESTINO_ID:
                    destinoId = jParser.nextIntValue(0);
                    break;
                case Serial.CONTA:
                    conta = parseConta(jParser,true);
                    break;
                case Serial.ORIGEM:
                    origem = parseConta(jParser,true);
                    break;
                case Serial.DESTINO:
                    destino = parseConta(jParser,true);
                    break;
                case Serial.VALOR:
                    valor = jParser.nextIntValue(0);
                    break;
                case Serial.DIA:
                    dia = LocalDate.parse(jParser.nextTextValue());
                    break;
                case Serial.DESC:
                    descricao = jParser.nextTextValue();
                    break;
                case Serial.LANCAMENTOS:
                    lancamentos = parseLancamentos(jParser);
                    break;
                default:
                    //see https://java.net/jira/browse/JERSEY-3005
                    throw new IOException("Couldn't parse");
            }
        }
        
        final Fato fato = new Fato(id);
        fato.setDia(dia);
        fato.setDescricao(descricao);

        if(lancamentos == null){
            if(origem == null){
                if(origemId != null){
                    lancamentos = 
                        makeTransferencia(
                            fato,
                            new Conta(origemId), 
                            new Conta(destinoId), 
                            valor);

                } else if(conta == null){
                    if(contaId != null){
                        lancamentos = makeGasto(fato, new Conta(contaId), valor);
                    } 
                } else {
                    lancamentos = makeGasto(fato, conta, valor);
                }
            } else {
                lancamentos = makeTransferencia(fato,origem, destino, valor);
            }
        }
        
        return lancamentos == null? fato : new FatoDetalhado(fato,lancamentos);
    }

    private static List<Lancamento> makeGasto(final Fato fato, Conta conta, Integer valor) {
        return Collections
                .singletonList(new Lancamento(fato,conta,valor));
    }

    private List<Lancamento> parseLancamentos(
            final JsonParser jParser) throws IOException {
        
        final List<Lancamento> lancamentos = new LinkedList<>();
        
        jParser.nextToken(); //START_ARRAY

        while(jParser.nextToken() != JsonToken.END_ARRAY) {
            final Lancamento lancamento = parseLancamento(jParser,false);
            lancamentos.add(lancamento);
        }
        return lancamentos;
    }

    private Conta parseConta(
            final JsonParser jParser, 
            final boolean hasStartObject) throws IOException{
        
        Integer id = null;
        String nome = null;

        if(hasStartObject)jParser.nextToken(); // START_OBJECT
        
        String fieldName;
        
        while((fieldName = jParser.nextFieldName()) != null){
            switch(fieldName) {
                case Serial.ID:
                    id = jParser.nextIntValue(0);
                    break;
                case Serial.NOME:
                    nome = jParser.nextTextValue();
                    break;
                default: 
                    //see https://java.net/jira/browse/JERSEY-3005
                    throw new IOException("Couldn't parse");

            }
        }
        
        return new Conta(id, nome);
    }

    private Lancamento parseLancamento(
            final JsonParser jParser, 
            final boolean hasStartObject) throws IOException{

        Integer id = null, contaId = null, fatoId = null, valor = null;
        Conta conta = null;
        
        if(hasStartObject)jParser.nextToken(); // START_OBJECT
        
        String fieldName;
        
        while((fieldName = jParser.nextFieldName()) != null){
            switch(fieldName) {
                case Serial.ID:
                    id = jParser.nextIntValue(0);
                    break;
                case Serial.CONTA_ID:
                    contaId = jParser.nextIntValue(0);
                    break;
                case Serial.CONTA:
                    conta = parseConta(jParser, true);
                    break;
                case Serial.VALOR:
                    valor = jParser.nextIntValue(0);
                    break;
                default:
                    //see https://java.net/jira/browse/JERSEY-3005
                    throw new IOException(
                        "Couldn't parse. Whats's "
                        + fieldName 
                        + " on Lancamento?");
            }
        }
        
        final Lancamento lancamento = new Lancamento();
        lancamento.setId(id);
        lancamento.setValor(valor);
        if(contaId != null){
            conta = new Conta(contaId);
        }
        lancamento.setConta(conta);
        
        return lancamento;
    }

    private List<Lancamento> makeTransferencia(
            final Fato fato, 
            final Conta origem, 
            final Conta destino, 
            final Integer valor) {
        
        final Lancamento de = new Lancamento(fato, origem, -valor);
        final Lancamento pra = new Lancamento(fato, destino, valor);

        return Arrays.asList(de,pra);
    }
}