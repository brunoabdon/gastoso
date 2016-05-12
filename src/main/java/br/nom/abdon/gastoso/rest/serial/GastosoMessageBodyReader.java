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
import java.util.Arrays;

import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.core.JsonParser;

import br.nom.abdon.gastoso.Conta;
import br.nom.abdon.gastoso.Fato;
import br.nom.abdon.gastoso.Lancamento;

import br.nom.abdon.gastoso.ext.FatoDetalhado;
import br.nom.abdon.gastoso.ext.Saldo;

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
        Conta.class,Fato.class,FatoDetalhado.class,Lancamento.class,Saldo.class
    };

    @Override
    public boolean isReadable(
        final Class<?> type, 
        final Type genericType, 
        final Annotation[] annotations, 
        final MediaType mediaType) {

        final boolean ehUmediaTypeConhecido = 
            MediaTypes.acceptGastosoMediaTypes(mediaType);

        final boolean ehUmTipoConhecido = 
            Arrays
                .stream(KNOWN_CLASSES)
                .parallel()
                .anyMatch(k -> k.isAssignableFrom(type));
        
        return ehUmTipoConhecido && ehUmediaTypeConhecido;
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
                ? UnMarshaller.parseConta(jParser)
                : Fato.class.isAssignableFrom(type)
                    ? UnMarshaller.parseFato(jParser)
                    : Saldo.class.isAssignableFrom(type)
                        ? UnMarshaller.parseSaldo(jParser)
                        : UnMarshaller.parseLancamento(jParser);
    }
}