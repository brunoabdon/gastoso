package com.github.brunoabdon.gastoso.rest.server;


import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import com.github.brunoabdon.commons.rest.AbstractRestCrud;
import com.github.brunoabdon.gastoso.Conta;
import com.github.brunoabdon.gastoso.dal.ContasDao;
import com.github.brunoabdon.gastoso.rest.MediaTypes;


/**
 *
 * @author bruno
 */
@Path(Contas.PATH)
@Produces({
    MediaTypes.APPLICATION_GASTOSO_FULL,
    MediaTypes.APPLICATION_GASTOSO_SIMPLES
})
@Consumes(MediaTypes.APPLICATION_GASTOSO_PATCH)
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