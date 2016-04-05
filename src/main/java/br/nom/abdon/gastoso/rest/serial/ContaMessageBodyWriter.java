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
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import br.nom.abdon.gastoso.Conta;

/**
 *
 * @author Bruno Abdon
 */
@Provider
@Produces({
    MediaTypes.APPLICATION_GASTOSO_NORMAL,
    MediaTypes.APPLICATION_GASTOSO_SIMPLES
})
public class ContaMessageBodyWriter implements EntidadeMessageBodyWriter<Conta>{

    //resusable, thread-safe. move somewhere.
    private static final JsonFactory JSON_FACT = new JsonFactory(); 

    @Override
    public boolean isWriteable(
            final Class<?> type, 
            final Type genericType, 
            final Annotation[] annotations, 
            final MediaType mediaType) {

        System.out.printf("sei ler %s/%s como %s?",type,genericType,mediaType);

        return 
            type == Conta.class
            && (mediaType
                .isCompatible(MediaTypes.APPLICATION_GASTOSO_NORMAL_TYPE)
                || mediaType
                    .isCompatible(MediaTypes.APPLICATION_GASTOSO_SIMPLES_TYPE));
    }

    @Override
    public void writeTo(
        final Conta conta, 
        final Class<?> type, 
        final Type genericType, 
        final Annotation[] annotations, 
        final MediaType mediaType, 
        final MultivaluedMap<String, Object> httpHeaders, 
        final OutputStream entityStream) 
            throws IOException, WebApplicationException {

        final JsonGenerator gen = JSON_FACT.createGenerator(entityStream);
        
        marshall(gen, conta, mediaType);
        
        gen.flush();
    }

    @Override
    public void marshall(
            final JsonGenerator gen, 
            final Conta conta, 
            final MediaType mediaType) throws IOException {
        
        gen.writeStartObject();
        gen.writeNumberField("id", conta.getId());
        if(mediaType.isCompatible(MediaTypes.APPLICATION_GASTOSO_NORMAL_TYPE)){
            gen.writeStringField("nome", conta.getNome());
        }
        gen.writeEndObject();
    }

    @Override
    public long getSize(
            final Conta t, 
            final Class<?> type, 
            final Type genericType, 
            final Annotation[] annotations, 
            final MediaType mediaType) {
        return -1;
    }    
}