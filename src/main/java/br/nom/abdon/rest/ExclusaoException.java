package br.nom.abdon.rest;

import javax.ws.rs.core.Response;

/**
 *
 * @author bruno
 */
public class ExclusaoException extends CrudException{

    public ExclusaoException(String message) {
        super(Response.Status.FORBIDDEN,"Não posso excluir. " + message);
    }
    
    
    
}
