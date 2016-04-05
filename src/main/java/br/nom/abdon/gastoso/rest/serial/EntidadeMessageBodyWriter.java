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

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyWriter;

import com.fasterxml.jackson.core.JsonGenerator;

import br.nom.abdon.gastoso.Conta;
import br.nom.abdon.modelo.Entidade;

/**
 *
 * @author Bruno Abdon
 * @param <E>
 */
public interface EntidadeMessageBodyWriter<E extends Entidade> 
        extends MessageBodyWriter<E>  {
    
    public void marshall(
        final JsonGenerator gen, 
        final E conta, 
        final MediaType mediaType) throws IOException;
    
}
