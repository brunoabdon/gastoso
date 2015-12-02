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

import java.security.GeneralSecurityException;
import java.util.logging.Logger;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Bruno Abdon
 */
@Path("login")
public class LoginRS {
    
    private static final Logger log = Logger.getLogger(LoginRS.class.getName());
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public AuthToken login(final String password){
        
        final AuthToken authToken;

        try {
            final String strToken = Auth.getInstance().login(password);
            authToken = new AuthToken(strToken);
            
        } catch (GeneralSecurityException ex){
            throw new WebApplicationException(
                ex.getMessage(), 
                Response.Status.UNAUTHORIZED);
        }
        
        return authToken;
    }
    
    public class AuthToken {
        public final String token;

        public AuthToken(final String token) {
            this.token = token;
        }
    }
}