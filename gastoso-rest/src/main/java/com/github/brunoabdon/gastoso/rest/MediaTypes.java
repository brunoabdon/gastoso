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
package com.github.brunoabdon.gastoso.rest;

import java.util.Arrays;

import javax.ws.rs.core.MediaType;

/**
 *
 * @author Bruno Abdon
 */
public class MediaTypes {

    public static final MediaType APPLICATION_GASTOSO_SIMPLES_TYPE =
        new MediaType("application", "vnd.gastoso.v1.simples+json");

    public static final MediaType APPLICATION_GASTOSO_FULL_TYPE =
        new MediaType("application", "vnd.gastoso.v1.full+json");

    public static final MediaType APPLICATION_GASTOSO_PATCH_TYPE =
        new MediaType("application", "vnd.gastoso.v1.patch+json");

    public static final String APPLICATION_GASTOSO_SIMPLES =
        "application/vnd.gastoso.v1.simples+json";

    public static final String APPLICATION_GASTOSO_FULL =
        "application/vnd.gastoso.v1.full+json";

    public static final String APPLICATION_GASTOSO_PATCH =
        "application/vnd.gastoso.v1.patch+json";
    
    
    public static final MediaType[] GASTOSO_MEDIATYPES = {
        APPLICATION_GASTOSO_SIMPLES_TYPE,
        APPLICATION_GASTOSO_FULL_TYPE,
        APPLICATION_GASTOSO_PATCH_TYPE
    };

    public static final boolean acceptGastosoMediaTypes(
            final MediaType mediaTypeToAccept){
        return acceptMediaTypes(mediaTypeToAccept, GASTOSO_MEDIATYPES);
    }

    public static final boolean acceptMediaTypes(
            final MediaType mediaTypeToAccept, 
            final MediaType ... acceptableMediaTypes){

        return Arrays
                .stream(acceptableMediaTypes)
                .anyMatch(mediaTypeToAccept::isCompatible);
    }

    public static final MediaType getCompatibleInstance(
            final MediaType originalMediaType){

        return Arrays
                .stream(GASTOSO_MEDIATYPES)
                .filter(originalMediaType::isCompatible)
                .findFirst().get();
    }    
}