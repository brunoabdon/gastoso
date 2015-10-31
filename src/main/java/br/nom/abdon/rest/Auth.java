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
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author Bruno Abdon
 */
public class Auth {

    private static final Auth INSTANCE = new Auth();
    
    private final Set<String> currentAuthorizations;
    
    private Auth(){    
        this.currentAuthorizations = new HashSet<>();
    }
    
    public static Auth getInstance(){
        return INSTANCE;
    }
    
    public String login(final String password) throws GeneralSecurityException{
        
        if(!"segredo".equals(password)){
            throw new GeneralSecurityException();
        }
        
        final String authToken = UUID.randomUUID().toString();
        currentAuthorizations.add(authToken);
        return authToken;
    }
    
    public boolean isValid(final String authToken){
        return currentAuthorizations.contains(authToken);
    }
    
    public void logout(final String authToken){
        currentAuthorizations.remove(authToken);
    }
}
