package br.nom.abdon.gastoso.rest;

import br.nom.abdon.rest.AbstractRestCrud;
import br.nom.abdon.gastoso.Conta;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author bruno
 */
@Path(Contas.PATH)
@Produces(MediaType.APPLICATION_JSON)
public class Contas extends AbstractRestCrud<Conta,Integer>{

    static final String PATH = "contas";
    
    public Contas() {
        super(Conta.class,PATH);
    }
}