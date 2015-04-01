/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package br.nom.abdon.gastoso.rest;

import br.nom.abdon.gastoso.Conta;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author bruno
 */
@Path("gastoso")
@Produces(MediaType.APPLICATION_JSON)
public class GastosoRest {

    private static final EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("gastoso_peruni");
    private static final EntityManager entityManager = entityManagerFactory.createEntityManager();    

    
    @POST
    @Path("/contas")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Conta criarConta(Conta conta){
        entityManager.getTransaction().begin();
        entityManager.persist(conta);
        entityManager.getTransaction().commit();
        return conta;
    }
    
    @GET
    @Path("/contas")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Conta> contas(){
        
        CriteriaQuery<Conta> cq = 
            entityManager.getCriteriaBuilder().createQuery(Conta.class);
        Root<Conta> conta = cq.from(Conta.class);
        cq.select(conta);
        
        TypedQuery<Conta> tq = entityManager.createQuery(cq);
        
        List<Conta> contas = tq.getResultList();
            
        return contas;
    }
    
    @GET
    @Path("/contas/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Conta pegaConta(@PathParam("id") int id){

        Conta conta = entityManager.find(Conta.class, id);
        
        if(conta == null) //sujou aqui. usar excecao da app e ExceptionMapper 
            throw new NotFoundException(); 
        
        return conta;
        
    }

    @PUT
    @Path("/contas/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void atualizaConta(@PathParam("id") int id, Conta conta){
        
        if(conta == null) //sujou aqui. usar excecao da app e ExceptionMapper 
            throw new NotFoundException(); 

        conta.setId(id);
        entityManager.getTransaction().begin();
        entityManager.merge(conta);
        entityManager.getTransaction().commit();
    }
    
    

    
    @DELETE
    @Path("/contas/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public void deletaConta(@PathParam("id") int id){

        Conta conta = entityManager.find(Conta.class, id);
        
        if(conta == null) //sujou aqui. usar excecao da app e ExceptionMapper 
            throw new NotFoundException(); 
        
        entityManager.getTransaction().begin();
        entityManager.remove(conta);
        entityManager.getTransaction().commit();
    }
    

    
    
}
