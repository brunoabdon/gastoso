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

import javax.ws.rs.core.MediaType;

/**
 *
 * @author Bruno Abdon
 */
public class MediaTypes {

    public static final MediaType APPLICATION_GASTOSO_SIMPLES_TYPE =
        new MediaType("application", "vnd.gastoso.v1.simples+json");

    public static final MediaType APPLICATION_GASTOSO_NORMAL_TYPE =
        new MediaType("application", "vnd.gastoso.v1.normal+json");

    public static final MediaType APPLICATION_GASTOSO_DACONTA_TYPE =
        new MediaType("application", "vnd.gastoso.v1.daconta+json");

    public static final String APPLICATION_GASTOSO_SIMPLES =
        "application/vnd.gastoso.v1.simples+json";

    public static final String APPLICATION_GASTOSO_NORMAL =
        "application/vnd.gastoso.v1.normal+json";

    public static final String APPLICATION_GASTOSO_DACONTA =
        "application/vnd.gastoso.v1.daconta+json";
    
}
