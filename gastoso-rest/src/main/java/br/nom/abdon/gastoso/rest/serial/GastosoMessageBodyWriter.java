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

import br.nom.abdon.gastoso.rest.MediaTypes;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.core.JsonGenerator;
import pl.touk.throwing.ThrowingConsumer;

import br.nom.abdon.gastoso.Conta;
import br.nom.abdon.gastoso.Fato;
import br.nom.abdon.gastoso.Lancamento;

import br.nom.abdon.gastoso.ext.Saldo;

/**
 *
 * @author Bruno Abdon
 */
@Produces({
    MediaTypes.APPLICATION_GASTOSO_SIMPLES,
    MediaTypes.APPLICATION_GASTOSO_FULL,
    MediaTypes.APPLICATION_GASTOSO_PATCH,
})
@Provider
public class GastosoMessageBodyWriter implements MessageBodyWriter<Object>{

    @Override
    public boolean isWriteable(
            final Class<?> type, 
            final Type genericType, 
            @SuppressWarnings("unused") 
            final Annotation[] annotations, 
            final MediaType mediaType) {

        return Serial.isAccecptable(type, 
                    genericType, 
                    Serial.SALDO_CLASSNAME, 
                    Serial.CONTA_CLASSNAME, 
                    Serial.FATO_CLASSNAME,
                    Serial.FATO_DETALHADO_CLASSNAME,
                    Serial.LANCAMENTO_CLASSNAME)
                && MediaTypes.acceptGastosoMediaTypes(mediaType);
    }

    @Override
    @SuppressWarnings("unused")
    public void writeTo(
        final Object entity, 
        final Class<?> type, 
        final Type genericType, 
        final Annotation[] annotations, 
        final MediaType mediaType, 
        final MultivaluedMap<String, Object> httpHeaders, 
        final OutputStream entityStream) 
            throws IOException, WebApplicationException {

        final JsonGenerator gen = 
            Serial.JSON_FACT.createGenerator(entityStream);

        final Marshaller marshaller = new Marshaller(gen,mediaType);

        final boolean ehColecao = Collection.class.isAssignableFrom(type);
        
        final String className = 
            Serial.getRelevantTypeName(ehColecao, type, genericType);

        MediaType tipo = MediaTypes.getCompatibleInstance(mediaType);
        
        final ThrowingConsumer<Object, IOException> entityMarshallerMethod = 
            className.contains(Serial.FATO_CLASSNAME) 
                || className.contains(Serial.FATO_DETALHADO_CLASSNAME) 
                ? f -> marshaller.marshall((Fato)f,tipo)
                : className.contains(Serial.SALDO_CLASSNAME)
                    ? s -> marshaller.marshall((Saldo)s,tipo)
                    : className.contains(Serial.CONTA_CLASSNAME)
                        ? c -> marshaller.marshall((Conta)c,tipo)
                        : l -> marshaller.marshall((Lancamento)l,tipo);
        
        final ThrowingConsumer<Object,IOException> marshallerMethod = 
            ehColecao
                ? col -> {
                            gen.writeStartArray();
                            for(Object x : (Collection<?>)col) {
                                entityMarshallerMethod.accept(x);
                            }
                            gen.writeEndArray();
                } : entityMarshallerMethod;

        marshallerMethod.accept(entity);

        gen.flush();
    }

    @Override
    @SuppressWarnings("unused")
    public long getSize(
            final Object t, Class<?> type, 
            final Type genericType, 
            final Annotation[] annotations, 
            final MediaType mediaType) {
        return -1;
    }
}