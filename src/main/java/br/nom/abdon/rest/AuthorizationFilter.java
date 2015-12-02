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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author Bruno Abdon
 */
@Provider
@PreMatching
public class AuthorizationFilter implements ContainerRequestFilter{
    
    private final static Logger log = 
        Logger.getLogger(AuthorizationFilter.class.getName());
 
    @Override
    public void filter(final ContainerRequestContext requestCtx) throws IOException {
 
        final String path = requestCtx.getUriInfo().getPath();
        log.log(Level.FINEST, "Filtering request path: {0}", path);
        
        // IMPORTANT!!! First, Acknowledge any pre-flight test from browsers for 
        // this case before validating the headers (CORS stuff)
        if ( requestCtx.getRequest().getMethod().equals( "OPTIONS" ) ) {
            requestCtx.abortWith(Response.status(Response.Status.OK).build());
        } else if (!path.equals("login")) {
            final String authToken = 
                requestCtx.getHeaderString( "X-Abd-auth_token" );
 
            // if it isn't valid, just kick them out.
            if ( !Auth.getInstance().isValid(authToken)) {
                final Response response = 
                    Response.status(Response.Status.UNAUTHORIZED).build();
                requestCtx.abortWith(response);
            }
        }
    }
}