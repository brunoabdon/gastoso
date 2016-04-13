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
import java.util.Collection;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import pl.touk.throwing.ThrowingConsumer;

import static br.nom.abdon.gastoso.rest.serial.MediaTypes.APPLICATION_GASTOSO_NORMAL_TYPE;
import static br.nom.abdon.gastoso.rest.serial.MediaTypes.APPLICATION_GASTOSO_SIMPLES_TYPE;

/**
 *
 * @author Bruno Abdon
 */
@Produces({
    MediaTypes.APPLICATION_GASTOSO_SIMPLES,
    MediaTypes.APPLICATION_GASTOSO_NORMAL,
    MediaTypes.APPLICATION_GASTOSO_FULL
})
public abstract class CollecaoMessageBodyWriter<X> 
        implements MessageBodyWriter<Collection<X>>{

    //resusable, thread-safe. move somewhere.
    private static final JsonFactory JSON_FACT = new JsonFactory(); 

    private final String className;
    
    protected CollecaoMessageBodyWriter(Class<X> klass){
        this.className = klass.getName();
    }
    
    @Override
    public boolean isWriteable(
            final Class<?> type, 
            final Type genericType, 
            final Annotation[] annotations, 
            final MediaType mediaType) {

        return 
            Collection.class.isAssignableFrom(type) 
            && genericType.getTypeName().contains(className);
    }

    @Override
    public void writeTo(
        final Collection<X> colecao, 
        final Class<?> type, 
        final Type genericType, 
        final Annotation[] annotations, 
        final MediaType mediaType, 
        final MultivaluedMap<String, Object> httpHeaders, 
        final OutputStream entityStream) 
            throws IOException, WebApplicationException {
        
        final JsonGenerator gen = JSON_FACT.createGenerator(entityStream);
        
        gen.writeStartArray();

        if(!colecao.isEmpty()){
            Marshaller.TIPO tipo = 
                mediaType.isCompatible(APPLICATION_GASTOSO_SIMPLES_TYPE)
                ? Marshaller.TIPO.BARE
                : mediaType.isCompatible(APPLICATION_GASTOSO_NORMAL_TYPE)
                    ? Marshaller.TIPO.NORM
                    : Marshaller.TIPO.FULL;
            
            foreach(colecao, getMarshaller(gen,tipo));
        }

        gen.writeEndArray();

        gen.flush();
    }
    
    protected abstract ThrowingConsumer<X,IOException> getMarshaller(
        final JsonGenerator gen, 
        final Marshaller.TIPO tipo);

    @Override
    public long getSize(
            final Collection<X> t, Class<?> type, 
            final Type genericType, 
            final Annotation[] annotations, 
            final MediaType mediaType) {
        return -1;
    }
    
   //utility function.. move to abd-utils someday...
    private void foreach(
        final Collection<X> colection, 
        final ThrowingConsumer<X,IOException> consumer) throws IOException{
        
        for(X x : colection) {
            consumer.accept(x);
        }
    }
}