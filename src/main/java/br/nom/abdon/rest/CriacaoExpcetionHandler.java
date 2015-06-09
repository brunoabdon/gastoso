package br.nom.abdon.rest;

import br.nom.abdon.modelo.CriacaoExpcetion;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author bruno
 */
@Provider
public class CriacaoExpcetionHandler implements ExceptionMapper<CriacaoExpcetion>{

    @Override
    public Response toResponse(CriacaoExpcetion exception) {
        return Response
                .status(Response.Status.BAD_REQUEST)
		.entity(exception.getMessage())
                .type(MediaType.APPLICATION_JSON)
                .build();
    }	        
}
