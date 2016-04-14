package br.nom.abdon.gastoso.rest;


import br.nom.abdon.gastoso.Conta;
import br.nom.abdon.gastoso.dal.ContasDao;
import br.nom.abdon.rest.AbstractRestCrud;

import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;


/**
 *
 * @author bruno
 */
@Path(Contas.PATH)
@Produces({
    MediaTypes.APPLICATION_GASTOSO_FULL,
    MediaTypes.APPLICATION_GASTOSO_NORMAL,
    MediaTypes.APPLICATION_GASTOSO_SIMPLES
})
public class Contas extends AbstractRestCrud<Conta,Integer>{

    static final String PATH = "contas";
    private final ContasDao dao;
    
    public Contas() {
        super(PATH);
        this.dao = new ContasDao();
    }

    @Override
    protected ContasDao getDao() {
        return this.dao;
    }
    
    @GET
    @Produces({MediaTypes.APPLICATION_GASTOSO_NORMAL,MediaTypes.APPLICATION_GASTOSO_SIMPLES})
    public Response listar(
            final @Context Request request,
            final @Context HttpHeaders httpHeaders) {
        
        final List<Conta> contas;
        
        final EntityManager em = emf.createEntityManager();
        
        try {
            contas = dao.listar(em);
        } finally {
            em.close();
        }
        
        return super.buildResponse(request,httpHeaders,contas);
    }
 }