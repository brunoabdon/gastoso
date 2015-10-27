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

import java.time.YearMonth;

/**
 *
 * @author Bruno Abdon
 */
public class YearMonthParamConverter extends AbstractTemporalParamConveter<YearMonth>{

    public static final YearMonthParamConverter instance = 
        new YearMonthParamConverter();
    
    @Override
    protected YearMonth parse(String strTemporal) {
        return YearMonth.parse(strTemporal);
    }
    
}
