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
package com.github.brunoabdon.gastoso.rest.serial;

import javax.ws.rs.Consumes;
import javax.ws.rs.ext.Provider;

import com.github.brunoabdon.gastoso.Lancamento;
import com.github.brunoabdon.gastoso.rest.MediaTypes;

/**
 *
 * @author Bruno Abdon
 */
@Provider
@Consumes({
    MediaTypes.APPLICATION_GASTOSO_SIMPLES,
    MediaTypes.APPLICATION_GASTOSO_FULL,
    MediaTypes.APPLICATION_GASTOSO_PATCH,
})
public class LancamentosMessageBodyReader extends GastosoCollectionMessageBodyReader<Lancamento>{
    
    public LancamentosMessageBodyReader() {
        super(Lancamento.class, UnMarshaller::parseLancamento);
    }
    
}