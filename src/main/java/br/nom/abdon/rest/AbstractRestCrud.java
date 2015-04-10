/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package br.nom.abdon.rest;


import br.nom.abdon.modelo.Entidade;
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
 * @param <X>
 * @param <Key>
 */
@Produces(MediaType.APPLICATION_JSON)
public abstract class  AbstractRestCrud<X extends Entidade<Key>,Key> {

    private static final EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("gastoso_peruni");
    private static final EntityManager entityManager = entityManagerFactory.createEntityManager();    
    
    private final Class<X> klass;

    public AbstractRestCrud(Class<X> klass) {
        this.klass = klass;
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public X criar(X x){
        entityManager.getTransaction().begin();
        entityManager.persist(x);
        entityManager.getTransaction().commit();
        return x;
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<X> listar(){
        CriteriaQuery<X> cq = 
            entityManager.getCriteriaBuilder().createQuery(klass);
        Root<X> x = cq.from(klass);
        cq.select(x);
        
        TypedQuery<X> tq = entityManager.createQuery(cq);
        
        List<X> xis = tq.getResultList();
            
        return xis;
    }
    
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public X pegar(@PathParam("id") int id){

        X x = entityManager.find(klass, id);
        
        if(x == null) //sujou aqui. usar excecao da app e ExceptionMapper 
            throw new NotFoundException(); 
        
        return x;
        
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void atualizar(@PathParam("id") Key id, X x){
        
        if(x == null) //sujou aqui. usar excecao da app e ExceptionMapper 
            throw new NotFoundException(); 

        x.setId(id);
        entityManager.getTransaction().begin();
        entityManager.merge(x);
        entityManager.getTransaction().commit();
    }
    
    

    
    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public void deleta(@PathParam("id") int id){

        X x = entityManager.find(klass, id);
        
        if(x == null) //sujou aqui. usar excecao da app e ExceptionMapper 
            throw new NotFoundException(); 
        
        entityManager.getTransaction().begin();
        entityManager.remove(x);
        entityManager.getTransaction().commit();
    }
}