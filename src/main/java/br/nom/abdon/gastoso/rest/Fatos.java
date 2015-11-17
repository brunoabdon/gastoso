package br.nom.abdon.gastoso.rest;

import br.nom.abdon.gastoso.Fato;
import br.nom.abdon.gastoso.dal.FatosDao;
import br.nom.abdon.gastoso.dal.LancamentosDao;
import br.nom.abdon.gastoso.rest.model.FatoDetalhe;
import br.nom.abdon.rest.AbstractRestCrud;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
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
    private final LancamentosDao lancamentosDao;

    public Fatos() {
        super(PATH);
        this.dao = new FatosDao();
        this.lancamentosDao = new LancamentosDao();
    }

    @Override
    public FatosDao getDao() {
        return dao;
    }

    @GET
    public Response listar(
        final @Context Request request,
        final @QueryParam("mes") YearMonth mes,
        @QueryParam("dataMin") LocalDate dataMinima,
        @QueryParam("dataMax") LocalDate dataMaxima){

        final List<Fato> fatos;
        Response.ResponseBuilder builder;
        
        
        if((mes == null && dataMaxima == null)
            || (mes != null && (dataMaxima != null || dataMinima != null))){
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        
        if(mes != null){
            dataMinima = mes.atDay(1);
            dataMaxima  = mes.atEndOfMonth();
        } else if(dataMinima == null) {
            dataMinima = dataMaxima.minusMonths(1);
        }
            
        final EntityManager entityManager = emf.createEntityManager();

        final Function<Fato, FatoDetalhe> detalhaFato = 
            fato -> new FatoDetalhe(
                            fato, 
                            lancamentosDao.listar(entityManager, fato));
        
        try {
            fatos = dao.listar(entityManager, dataMinima, dataMaxima);
            
            final List<FatoDetalhe> fatosDetalhados = new ArrayList<>(fatos.size());
            
            fatos.stream().map(detalhaFato).forEach(fatosDetalhados::add);
            
            
            final br.nom.abdon.gastoso.rest.model.Fatos fs = new br.nom.abdon.gastoso.rest.model.Fatos(dataMinima,dataMaxima,fatosDetalhados);

            final EntityTag tag = new EntityTag(Integer.toString(fs.hashCode()));
            builder = request.evaluatePreconditions(tag);
            if(builder==null){
		//preconditions are not met and the cache is invalid
		//need to send new value with reponse code 200 (OK)
		builder = Response.ok(fs);
		builder.tag(tag);
            }
            
        } finally {
            entityManager.close();
        }
        
        return builder.build();
    }
}
