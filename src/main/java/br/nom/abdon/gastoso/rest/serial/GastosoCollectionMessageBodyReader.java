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
import java.lang.annotation.Annotation;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import com.fasterxml.jackson.core.JsonParser;
import pl.touk.throwing.ThrowingConsumer;
import pl.touk.throwing.ThrowingFunction;

import br.nom.abdon.gastoso.Fato;
import br.nom.abdon.rest.providers.CollectionMessageBodyReader;

/**
 *
 * @author Bruno Abdon
 */
class GastosoCollectionMessageBodyReader<E> 
        extends CollectionMessageBodyReader<E> {

    private final ThrowingFunction<JsonParser,E,IOException> unmarshaller;
    
    public GastosoCollectionMessageBodyReader(
            final Class<E> elementType, 
            final ThrowingFunction<JsonParser,E,IOException> unmarshaller) {
        super(elementType, Serial.JSON_FACT);
        this.unmarshaller = unmarshaller;
    }

    @Override
    protected E tryToReadEntity(
        final Annotation[] annotations, 
        final MediaType mediaType, 
        final MultivaluedMap<String, String> httpHeaders, 
        final JsonParser jParser) throws IOException {

        return unmarshaller.apply(jParser);
    }
    
}
