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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import br.nom.abdon.gastoso.Conta;
import br.nom.abdon.gastoso.Fato;
import br.nom.abdon.gastoso.Lancamento;

import br.nom.abdon.gastoso.ext.FatoDetalhado;
import br.nom.abdon.gastoso.ext.Saldo;

/**
 *
 * @author Bruno Abdon
 */
class UnMarshaller {

    public static Fato parseFato(final JsonParser jParser) throws IOException{

        if(jParser.nextToken() == JsonToken.END_ARRAY) return null;
        
        Integer id = null, origemId = null, destinoId = null, contaId = null;
        String descricao = null;
        LocalDate dia = null;
        Integer valor = null;
        
        List<Lancamento> lancamentos = null;
        Conta conta = null, origem = null, destino = null;
        
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
                    conta = parseConta(jParser);
                    break;
                case Serial.ORIGEM:
                    origem = parseConta(jParser);
                    break;
                case Serial.DESTINO:
                    destino = parseConta(jParser);
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
                    throw new IOException("Couldn't parse. What's " 
                                            + fieldName
                                            + " on fato?");
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

    private static List<Lancamento> makeGasto(
            final Fato fato, 
            final Conta conta, 
            final Integer valor) {
        final List<Lancamento> list = new ArrayList<>(1);
        list.add(new Lancamento(fato,conta,valor));
        return list;
        
    }

    public static List<Lancamento> parseLancamentos(
            final JsonParser jParser) throws IOException {
        
        final List<Lancamento> lancamentos = new LinkedList<>();
        
        jParser.nextToken(); //START_ARRAY

        boolean leu;
        do{
            final Lancamento lancamento = parseLancamento(jParser);

            if(leu = (lancamento != null)){
                lancamentos.add(lancamento);
            }

        } while(leu);
        
        return lancamentos;
    }

    public static Conta parseConta(final JsonParser jParser) throws IOException {

        if(jParser.nextToken() == JsonToken.END_ARRAY) return null;
        
        Integer id = null;
        String nome = null;

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

    public static Lancamento parseLancamento(final JsonParser jParser) 
            throws IOException{

        if(jParser.nextToken() == JsonToken.END_ARRAY) return null;

        Integer id = null, contaId = null, fatoId = null, valor = null;
        Conta conta = null;
        Fato fato = null;
        LocalDate dia = null;
        String descricao = null;

        String fieldName;

        while((fieldName = jParser.nextFieldName()) != null){
            switch(fieldName) {
                case Serial.ID:
                    id = jParser.nextIntValue(0);
                    break;
                case Serial.CONTA_ID:
                    contaId = jParser.nextIntValue(0);
                    break;
                case Serial.FATO:
                    fato = parseFato(jParser);
                    break;
                case Serial.FATO_ID:
                    fatoId = jParser.nextIntValue(0);
                    break;
                case Serial.CONTA:
                    conta = parseConta(jParser);
                    break;
                case Serial.DIA:
                    dia = LocalDate.parse(jParser.nextTextValue());
                    break;
                case Serial.DESC:
                    descricao = jParser.nextTextValue();
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

        if(fatoId != null){
            fato = new Fato(fatoId);
            lancamento.setFato(fato);
        } else if(dia != null){
            fato = new Fato(dia,descricao);
        } 
        lancamento.setFato(fato); //pode ser null

        return lancamento;
    }

    static Saldo parseSaldo(JsonParser jParser) throws IOException {
        
        if(jParser.nextToken() == JsonToken.END_ARRAY) return null;
        
        Conta conta = null;
        LocalDate dia = null;
        Long valor = null;

        String fieldName;

        while((fieldName = jParser.nextFieldName()) != null){
            switch(fieldName) {
                case Serial.CONTA:
                    conta = parseConta(jParser);
                    break;
                case Serial.DIA:
                    dia = LocalDate.parse(jParser.nextTextValue());
                    break;
                case Serial.VALOR:
                    valor = jParser.nextLongValue(0);
                    break;
                default:
                    //see https://java.net/jira/browse/JERSEY-3005
                    throw new IOException(
                        "Couldn't parse. Whats's "
                        + fieldName 
                        + " on Saldo?");
            }
        }
        
        return new Saldo(conta, dia, valor);

    }
    
    
    private static List<Lancamento> makeTransferencia(
            final Fato fato, 
            final Conta origem, 
            final Conta destino, 
            final Integer valor) {

        final Lancamento de = new Lancamento(fato, origem, -valor);
        final Lancamento pra = new Lancamento(fato, destino, valor);

        return Arrays.asList(de,pra);
    }

}