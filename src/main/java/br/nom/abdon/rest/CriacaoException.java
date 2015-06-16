package br.nom.abdon.rest;

import br.nom.abdon.modelo.Entidade;
import javax.ws.rs.core.Response;

/**
 *
 * @author bruno
 */
public class CriacaoException extends CrudException{

    private static final String BASE_MSG = "Não foi possivel criar.";
    
//    private final Entidade entidade;
    
    public CriacaoException(Throwable causa, Entidade entidade) {
        super(
            Response.Status.INTERNAL_SERVER_ERROR,
            String.format(BASE_MSG, entidade),
            causa);
//        this.entidade = entidade;
    }
    
}
