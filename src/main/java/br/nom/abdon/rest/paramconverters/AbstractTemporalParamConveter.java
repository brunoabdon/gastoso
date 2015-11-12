/*
 * Copyright (C) 2015 Bruno Abdon
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
package br.nom.abdon.rest.paramconverters;

import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;
import javax.ws.rs.ext.ParamConverter;

/**
 * @param <T> o temporal que é convertido por essa classe
 * @author Bruno Abdon
 */
public abstract class AbstractTemporalParamConveter<T extends Temporal> 
        implements ParamConverter<T>{

    @Override
    public T fromString(final String strTemporal) {
       if(strTemporal == null)
            throw new IllegalArgumentException(strTemporal);
            
        final T temporal;
        
        try {
            temporal = parse(strTemporal);
            
        } catch (DateTimeParseException e){
            throw new IllegalArgumentException(strTemporal);
        }
        
        return temporal;
         
    }

    @Override
    public String toString(T temporal) {
        return temporal.toString();
    }
    
    protected abstract T parse(String strTemporal);
    
}
