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
import java.util.Arrays;
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

import br.nom.abdon.gastoso.Conta;
import br.nom.abdon.gastoso.Lancamento;
import br.nom.abdon.gastoso.aggregate.Saldo;
import br.nom.abdon.gastoso.rest.model.FatoNormal;

/**
 *
 * @author Bruno Abdon
 */
@Produces({
    MediaTypes.APPLICATION_GASTOSO_SIMPLES,
    MediaTypes.APPLICATION_GASTOSO_NORMAL,
    MediaTypes.APPLICATION_GASTOSO_FULL
})
@Provider
public class GastosoMessageBodyWriter
        implements MessageBodyWriter<Object>{

    //resusable, thread-safe. move somewhere.
    private static final JsonFactory JSON_FACT = new JsonFactory(); 

    private static final String SALDO_CLASSNAME = Saldo.class.getName();
    private static final String CONTA_CLASSNAME = Conta.class.getName();

    private static final String FATO_NORMAL_CLASSNAME = 
        FatoNormal.class.getName();
    
    private static final String LANCAMENTO_CLASSNAME = 
        Lancamento.class.getName();
    
    private static final String[] KNOWN_CLASSNAMES = {
        SALDO_CLASSNAME, 
        CONTA_CLASSNAME, 
        FATO_NORMAL_CLASSNAME,
        LANCAMENTO_CLASSNAME
    };

    @Override
    public boolean isWriteable(
            final Class<?> type, 
            final Type genericType, 
            final Annotation[] annotations, 
            final MediaType mediaType) {

        return
            Arrays
                .stream(KNOWN_CLASSNAMES)
                .anyMatch(getRelevantType(type, genericType)::contains);
    }

    @Override
    public void writeTo(
        final Object entity, 
        final Class<?> type, 
        final Type genericType, 
        final Annotation[] annotations, 
        final MediaType mediaType, 
        final MultivaluedMap<String, Object> httpHeaders, 
        final OutputStream entityStream) 
            throws IOException, WebApplicationException {

        final JsonGenerator gen = JSON_FACT.createGenerator(entityStream);

        final Marshaller marshaller = new Marshaller(gen,mediaType);

        final boolean ehColecao = Collection.class.isAssignableFrom(type);
        
        final String className = 
            getRelevantTypeName(ehColecao, type, genericType);
        
        final ThrowingConsumer<Object, IOException> entityMarshallerMethod = 
            className.contains(FATO_NORMAL_CLASSNAME)
                ? f -> marshaller.marshall((FatoNormal)f)
                : className.contains(SALDO_CLASSNAME)
                    ? s -> marshaller.marshall((Saldo)s)
                    : className.contains(CONTA_CLASSNAME)
                        ? c -> marshaller.marshall((Conta)c)
                        : l -> marshaller.marshall((Lancamento)l);
        
        final ThrowingConsumer<Object,IOException> marshallerMethod = 
            ehColecao
                ? col -> {
                            gen.writeStartArray();
                            for(Object x : (Collection)col) {
                                entityMarshallerMethod.accept(x);
                            }
                            gen.writeEndArray();
                } : entityMarshallerMethod;

        marshallerMethod.accept(entity);

        gen.flush();
    }

    private String getRelevantType(
            final Class<?> type, 
            final Type genericType) {
        return 
            getRelevantTypeName(
                Collection.class.isAssignableFrom(type),
                type, 
                genericType);
    }

    private String getRelevantTypeName(
            final boolean isCollection, 
            final Class<?> type, 
            final Type genericType) {
        return (isCollection ? genericType : type).getTypeName();
    }

    @Override
    public long getSize(
            final Object t, Class<?> type, 
            final Type genericType, 
            final Annotation[] annotations, 
            final MediaType mediaType) {
        return -1;
    }
}