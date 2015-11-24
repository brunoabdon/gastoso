/*
 * Copyright (C) 2015 Bruno Abdon
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

import br.nom.abdon.gastoso.Conta;
import br.nom.abdon.gastoso.Fato;
import br.nom.abdon.gastoso.Lancamento;
import br.nom.abdon.gastoso.rest.model.FatoDetalhe;
import br.nom.abdon.gastoso.rest.model.Extrato;
import br.nom.abdon.util.LocalDateISO8601Serializer;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author Bruno Abdon
 */
public class FatosJsonSerializer extends JsonSerializer<Extrato>{

    @Override
    public void serialize(
            final Extrato fatos, 
            final JsonGenerator jgen, 
            final SerializerProvider serpro) 
                throws IOException, JsonProcessingException {
        
        final LocalDateISO8601Serializer dateSerializer = 
            LocalDateISO8601Serializer.INSTANCE;

        jgen.writeStartObject();

        final Conta conta = fatos.getConta();
        final boolean unicaConta = conta != null;
        
        if(unicaConta){
            jgen.writeNumberField("contaId", conta.getId());
            jgen.writeNumberField("saldoIncial", fatos.getSaldoInicial());
        }
        
        jgen.writeFieldName("inicio");
        dateSerializer.serialize(fatos.getDataInicial(), jgen, serpro);
        
        jgen.writeFieldName("fim");
        dateSerializer.serialize(fatos.getDataFinal(), jgen, serpro);
        
        jgen.writeArrayFieldStart("fatos");
        for (FatoDetalhe f: fatos.getFatos()) {
            final Fato fato = f.getFato();
            jgen.writeStartObject();
            
            jgen.writeNumberField("id", fato.getId());
            jgen.writeFieldName("dia");
            dateSerializer.serialize(fato.getDia(), jgen, serpro);
            
            jgen.writeStringField("desc", fato.getDescricao());

            final List<Lancamento> lancamentos = f.getLancamentos();
            if(lancamentos.size() == 1){
                writeLancamento(jgen, lancamentos.get(0),!unicaConta);
            } else {
                jgen.writeArrayFieldStart("lancamentos");

                for (Lancamento l : lancamentos) {
                    jgen.writeStartObject();
                    writeLancamento(jgen, l, true);
                    jgen.writeEndObject();
                }
                jgen.writeEndArray();
            }
            
            jgen.writeEndObject();
        }
        jgen.writeEndArray();
        jgen.writeEndObject();
    }

    private void writeLancamento(
            final JsonGenerator jgen, 
            final Lancamento lancamento,
            final boolean incluirConta) throws IOException {
        if(incluirConta){
            jgen.writeNumberField("contaId", lancamento.getConta().getId());
        }

        jgen.writeNumberField("valor", lancamento.getValor());

    }
}