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
package br.nom.abdon.gastoso.core.util;

import br.nom.abdon.gastoso.Lancamento;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

/**
 *
 * @author Bruno Abdon
 */
public class LancamentoJsonSerializer extends JsonSerializer<Lancamento>{

    @Override
    public void serialize(
            final Lancamento lancamento, 
            final JsonGenerator jgen, 
            final SerializerProvider serializerProvider) 
        throws IOException, JsonProcessingException {

        jgen.writeStartObject();
        jgen.writeNumberField("id", lancamento.getId());
        jgen.writeNumberField("fatoId", lancamento.getFato().getId());
        jgen.writeNumberField("contaId", lancamento.getConta().getId());
        jgen.writeNumberField("valor", lancamento.getValor());
        jgen.writeEndObject();
    }
    
}
