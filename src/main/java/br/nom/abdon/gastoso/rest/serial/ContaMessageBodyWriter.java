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
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

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
public class ContaMessageBodyWriter extends AbsMessageBodyWriter<Conta>{

    public ContaMessageBodyWriter() {
        super(Conta.class);
    }

    @Override
    protected void marshall(
            final JsonGenerator gen, 
            final Conta conta, 
            final MediaType mediaType) throws IOException {
        
        Marshaller.marshall(gen, conta, Marshaller.TIPO.NORM);
    }

    
    

}