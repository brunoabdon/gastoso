package br.nom.abdon.rest;


import br.nom.abdon.modelo.Entidade;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.PersistenceUnit;
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
import javax.ws.rs.core.Response;

/**
 *
 * @author bruno
 * @param <X>
 * @param <Key>
 */
@Produces(MediaType.APPLICATION_JSON)
public abstract class AbstractRestCrud<X extends Entidade<Key>,Key> {

    private static final Logger LOG = 
        Logger.getLogger(AbstractRestCrud.class.getName());
    
    @PersistenceUnit(unitName = "gastoso_peruni")
    protected EntityManagerFactory emf;

    private final Class<X> klass;
    private final String path;

    public AbstractRestCrud(Class<X> klass, String path) {
        this.klass = klass;
        this.path = path + "/" ;
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response criar(X x) throws CrudException{
        
        validarCriacao(x);

        EntityManager entityManager = emf.createEntityManager();
        try {
            
            entityManager.getTransaction().begin();
            
            entityManager.persist(x);

            entityManager.getTransaction().commit();
        } catch (PersistenceException e){
            throw new CriacaoException(e,x);
        } finally {
            entityManager.close();
        }
        
        return Response.created(makeURI(x)).entity(x).build(); //sujou
    }
    
    protected URI makeURI(X x) {
        URI uri;
        
        try {
            uri = new URI(path + String.valueOf(x.getId()));
        } catch (URISyntaxException ex) {
            LOG.log(Level.SEVERE, null, ex);
            uri = null;
        }
        return uri;
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<? super X> listar(){
        
        final List<X> xis;
        
        EntityManager entityManager = emf.createEntityManager();
        try {
            
            CriteriaQuery<X> cq = 
                entityManager.getCriteriaBuilder().createQuery(klass);
            Root<X> x = cq.from(klass);
            cq.select(x);

            TypedQuery<X> tq = entityManager.createQuery(cq);

            xis = tq.getResultList();
        } finally {
            entityManager.close();
        }
            
        return xis;
    }
    
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public X pegar(@PathParam("id") int id){

        X x;
        
        EntityManager entityManager = emf.createEntityManager();
        try {
            x = entityManager.find(klass, id);
        } finally {
            entityManager.close();
        }
        
        if(x == null) //sujou aqui. usar excecao da app e ExceptionMapper 
            throw new NotFoundException(); 

        return x;
    }

    @POST
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void atualizar(@PathParam("id") Key id, X x){
        
        if(x == null) //sujou aqui. usar excecao da app e ExceptionMapper 
            throw new NotFoundException(); 

        x.setId(id);
        EntityManager entityManager = emf.createEntityManager();
        try {
            entityManager.getTransaction().begin();
            entityManager.merge(x);
            entityManager.getTransaction().commit();
        } finally {
            entityManager.close();
        }
    }
    
    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deletar(@PathParam("id") int id) throws CrudException{

        EntityManager entityManager = emf.createEntityManager();
        try {
            X x = entityManager.find(klass, id);
            
            if(x == null) //sujou aqui. usar excecao da app e ExceptionMapper 
                throw new NotFoundException(); 
            
            validarExclusao(entityManager,x);

            entityManager.getTransaction().begin();

            deletaDependencias(entityManager, x);
            
            entityManager.remove(x);
            entityManager.getTransaction().commit();

        } finally {
            entityManager.close();
        }
        
        return Response.noContent().build(); //sujou
    }

    protected void validarCriacao(X x) throws ValidacaoException{
    }

    protected void validarExclusao(EntityManager entityManager, X x) throws ExclusaoException{
    }

    protected void deletaDependencias(EntityManager entityManager, X x) throws ExclusaoException{
    }
    
}