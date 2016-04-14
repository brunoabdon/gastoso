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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;



/**
 *
 * @author Bruno Abdon
 * @param <T> o tipo escrito
 */
public abstract class AbsMessageBodyWriter<T> implements MessageBodyWriter<T>  {

    //resusable, thread-safe. move somewhere.
    private static final JsonFactory JSON_FACT = new JsonFactory(); 

    private final Class<T> klass;

    public AbsMessageBodyWriter(final Class<T> klass) {
        this.klass = klass;
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return 
            klass.isAssignableFrom(type)
            && MediaTypes.acceptMediaTypes(
                mediaType,
                MediaTypes.APPLICATION_GASTOSO_FULL_TYPE,
                MediaTypes.APPLICATION_GASTOSO_NORMAL_TYPE,
                MediaTypes.APPLICATION_GASTOSO_SIMPLES_TYPE);
    }
    
    @Override
    public long getSize(
            final T t, 
            final Class<?> type, 
            final Type genericType, 
            final Annotation[] annotations, 
            final MediaType mediaType) {
        return -1;
    }    

    @Override
    public void writeTo(
            final T entity, 
            final Class<?> type, 
            final Type genericType, 
            final Annotation[] annotations, 
            final MediaType mediaType, 
            final MultivaluedMap<String, Object> httpHeaders, 
            final OutputStream entityStream) throws IOException, WebApplicationException {

        final JsonGenerator gen = JSON_FACT.createGenerator(entityStream);

        marshall(gen, entity, mediaType);

        gen.flush();
    }

    protected abstract void marshall(
        final JsonGenerator gen, 
            final T entity, 
            final MediaType mediaType) throws IOException;
}
