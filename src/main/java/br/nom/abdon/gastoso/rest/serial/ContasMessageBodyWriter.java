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

import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.core.JsonGenerator;
import pl.touk.throwing.ThrowingConsumer;

import br.nom.abdon.gastoso.Conta;

/**
 *
 * @author Bruno Abdon
 */
@Provider
public class ContasMessageBodyWriter extends CollecaoMessageBodyWriter<Conta>{

    public ContasMessageBodyWriter() {
        super(Conta.class);
    }

    @Override
    protected ThrowingConsumer<Conta, IOException> getMarshaller(
            final JsonGenerator gen, final Marshaller.TIPO tipo) {
        return c -> Marshaller.marshall(gen, c, tipo);
    }
}
