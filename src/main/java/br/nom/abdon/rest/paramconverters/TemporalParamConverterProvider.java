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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.YearMonth;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author Bruno Abdon
 */
@Provider
public class TemporalParamConverterProvider implements ParamConverterProvider{

    @Override
    public <T> ParamConverter<T> getConverter(
            Class<T> rawType, 
            Type genericType, 
            Annotation[] annotations) {
        return (ParamConverter<T>)
                (rawType == LocalDate.class 
                    ? LocalDateParamConverter.instance
                    : rawType == YearMonth.class
                        ? YearMonthParamConverter.instance
                        : null);
    }
}
