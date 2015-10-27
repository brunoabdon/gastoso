package br.nom.abdon.gastoso.rest;

import br.nom.abdon.gastoso.Fato;
import br.nom.abdon.gastoso.dal.FatosDao;
import br.nom.abdon.rest.AbstractRestCrud;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author bruno
 */
@Path(Fatos.PATH)
@Produces(MediaType.APPLICATION_JSON)
public class Fatos extends AbstractRestCrud<Fato,Integer>{

    protected static final String PATH = "fatos";

    private final FatosDao dao;

    public Fatos() {
        super(PATH);
        this.dao = new FatosDao();
    }

    @Override
    public FatosDao getDao() {
        return dao;
    }
    
    @GET
    public List<Fato> listar(
        final @QueryParam("dataMin") String dataMin,
        final @QueryParam("dataMax") String dataMax){
        
        final List<Fato> fatos;

        if(dataMax == null){
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        
        final LocalDate dataMinima, dataMaxima;

        try {
            dataMaxima = LocalDate.parse(dataMax);
            dataMinima = dataMin != null
                ? LocalDate.parse(dataMin)
                : dataMaxima.minusMonths(1);
            
        } catch (DateTimeParseException e){
            throw new WebApplicationException(e,Response.Status.BAD_REQUEST);
        }
        
        final EntityManager entityManager = emf.createEntityManager();
        
        try {
            fatos = dao.listar(entityManager, dataMinima, dataMaxima);
        } finally {
            entityManager.close();
        }
        
        return fatos;
    }
}
