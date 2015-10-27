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
package br.nom.abdon.rest;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import javax.ws.rs.ext.ParamConverter;

/**
 *
 * @author Bruno Abdon
 */
public class TimeParamConverter implements ParamConverter<LocalDate>{

    public static TimeParamConverter instance = new TimeParamConverter();

    private TimeParamConverter() {}
    
    @Override
    public LocalDate fromString(final String strLocalDate) {
        
        if(strLocalDate == null)
            throw new IllegalArgumentException(strLocalDate);
            
        final LocalDate localDate;
        
        try {
            localDate = LocalDate.parse(strLocalDate);
            
        } catch (DateTimeParseException e){
            throw new IllegalArgumentException(strLocalDate);
        }
        
        return localDate;
        
    }

    @Override
    public String toString(LocalDate localDate) {
        if(localDate == null) throw new IllegalArgumentException();
        return localDate.toString();
    }
    
}
