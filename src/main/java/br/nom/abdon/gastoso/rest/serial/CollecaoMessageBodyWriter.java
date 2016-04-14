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
@Provider
public class CollecaoMessageBodyWriter
        implements MessageBodyWriter<Collection>{

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
        
        final String typeName = genericType.getTypeName();

        return 
            Collection.class.isAssignableFrom(type) 
            && Arrays.stream(KNOWN_CLASSNAMES).anyMatch(typeName::contains);
    }

    @Override
    public void writeTo(
        final Collection colecao, 
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
            writeContents(gen, genericType, mediaType, colecao);
        }

        gen.writeEndArray();

        gen.flush();
    }

    private void writeContents(
            final JsonGenerator gen, 
            final Type genericType, 
            final MediaType mediaType, 
            final Collection colecao) throws IOException {
        
        final Marshaller.TIPO tipo = getTipo(mediaType);
        
        final ThrowingConsumer<Object, IOException> marshaller =
                getMarshaller(gen,genericType,tipo);
        
        for(Object x : colecao) {
            marshaller.accept(x);
        }
    }

    protected Marshaller.TIPO getTipo(final MediaType mediaType) {
        Marshaller.TIPO tipo =
                mediaType.isCompatible(APPLICATION_GASTOSO_SIMPLES_TYPE)
                ? Marshaller.TIPO.BARE
                : mediaType.isCompatible(APPLICATION_GASTOSO_NORMAL_TYPE)
                ? Marshaller.TIPO.NORM
                : Marshaller.TIPO.FULL;
        return tipo;
    }
    
    protected ThrowingConsumer<Object,IOException> getMarshaller(
        final JsonGenerator gen, 
        final Type genericType,
        final Marshaller.TIPO tipo){
        
        final String className = genericType.getTypeName();
        
        return 
            className.contains(FATO_NORMAL_CLASSNAME)
                ? f -> Marshaller.marshall(gen, (FatoNormal)f, tipo)
                : className.contains(SALDO_CLASSNAME)
                    ? s -> Marshaller.marshall(gen, (Saldo)s, tipo)
                    : className.contains(CONTA_CLASSNAME)
                        ? c -> Marshaller.marshall(gen, (Conta)c, tipo)
                        : l -> Marshaller.marshall(gen, (Lancamento)l);
    }

    @Override
    public long getSize(
            final Collection t, Class<?> type, 
            final Type genericType, 
            final Annotation[] annotations, 
            final MediaType mediaType) {
        return -1;
    }
}