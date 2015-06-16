package br.nom.abdon.rest;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/** 
 *
 * @author bruno
 */
@Provider
public class CrudExpcetionHandler implements ExceptionMapper<CrudException>{

    @Override
    public Response toResponse(CrudException exception) {
        
        return Response
                .status(exception.getStatus())
		.entity(exception.getErrorPojo())
                .type(MediaType.APPLICATION_JSON)
                .build();
    }	        
}
