package br.nom.abdon.gastoso.rest;

import br.nom.abdon.gastoso.Fato;
import br.nom.abdon.gastoso.dal.FatosDao;
import br.nom.abdon.rest.AbstractRestCrud;
import java.time.LocalDate;
import java.time.YearMonth;
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
        @QueryParam("dataMin") LocalDate dataMinima,
        final @QueryParam("dataMax") LocalDate dataMaxima){

        final List<Fato> fatos;

        if(dataMaxima == null){
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        if(dataMinima == null) dataMinima = dataMaxima.minusMonths(1);
            
        final EntityManager entityManager = emf.createEntityManager();
        
        try {
            fatos = dao.listar(entityManager, dataMinima, dataMaxima);
        } finally {
            entityManager.close();
        }
        
        return fatos;
    }
}
