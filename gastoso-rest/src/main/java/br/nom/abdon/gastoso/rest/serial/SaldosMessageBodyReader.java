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


import javax.ws.rs.Consumes;
import javax.ws.rs.ext.Provider;

import br.nom.abdon.gastoso.ext.Saldo;
import br.nom.abdon.gastoso.rest.MediaTypes;

/**
 *
 * @author Bruno Abdon
 */
@Provider
@Consumes({
    MediaTypes.APPLICATION_GASTOSO_SIMPLES,
    MediaTypes.APPLICATION_GASTOSO_FULL
})
public class SaldosMessageBodyReader 
    extends GastosoCollectionMessageBodyReader<Saldo>{

    public SaldosMessageBodyReader() {
        super(Saldo.class,UnMarshaller::parseSaldo);
    }

    
}
