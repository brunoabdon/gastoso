/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package br.nom.abdon.rest;


import br.nom.abdon.modelo.Entidade;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
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

    
    private static final EntityManagerFactory emf;
    
    static {
        final String databaseUrl = System.getenv("DATABASE_URL");
        final StringTokenizer st = new StringTokenizer(databaseUrl, ":@/");
        
        final String dbVendor = st.nextToken(); //if DATABASE_URL is set
        final String userName = st.nextToken();
        final String password = st.nextToken();
        final String host = st.nextToken();
        final String port = st.nextToken();
        final String databaseName = st.nextToken();

        final String jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory", host, port, databaseName);

        System.out.println(jdbcUrl);
        
        final Map<String, String> properties = new HashMap<>();
        properties.put("javax.persistence.jdbc.url", jdbcUrl );
        properties.put("javax.persistence.jdbc.user", userName );
        properties.put("javax.persistence.jdbc.password", password );
        properties.put("javax.persistence.jdbc.driver", "org.postgresql.Driver");
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");

        emf = Persistence.createEntityManagerFactory("gastoso_peruni", properties);        
    }
            
    
    private final Class<X> klass;
    private final EntityManager entityManager;

    public AbstractRestCrud(Class<X> klass) {
        this.klass = klass;
        this.entityManager = emf.createEntityManager();    
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
    public void deletar(@PathParam("id") int id){

        X x = entityManager.find(klass, id);
        
        if(x == null) //sujou aqui. usar excecao da app e ExceptionMapper 
            throw new NotFoundException(); 
        
        entityManager.getTransaction().begin();
        entityManager.remove(x);
        entityManager.getTransaction().commit();
    }
}