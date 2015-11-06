package br.nom.abdon.gastoso.rest;

import br.nom.abdon.gastoso.Lancamento;
import br.nom.abdon.gastoso.Conta;
import br.nom.abdon.gastoso.Fato;
import br.nom.abdon.gastoso.dal.LancamentosDao;
import br.nom.abdon.rest.AbstractRestCrud;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

/**
 *
 * @author bruno
 */
@Path(Lancamentos.PATH)
@Produces(MediaType.APPLICATION_JSON)
public class Lancamentos extends AbstractRestCrud<Lancamento, Integer> {

    protected static final String PATH = "lancamentos";
            
    private final LancamentosDao dao;

    public Lancamentos() {
        super(PATH);
        this.dao = new LancamentosDao();
    }

    @Override
    public LancamentosDao getDao() {
        return dao;
    }

    @GET
    public Response listar(
            final @Context Request request,
            final @QueryParam("fato") Fato fato,
            final @QueryParam("conta") Conta conta,
            final @QueryParam("mes") YearMonth mes,
            @QueryParam("dataMin") LocalDate dataMinima,
            @QueryParam("dataMax") LocalDate dataMaxima){
        
        final List<Lancamento> lancamentos;

        final EntityManager entityManager = emf.createEntityManager();

        try {
            if(fato != null){
                lancamentos = dao.listar(entityManager, fato);
            } else if (conta != null
                        &&
                            (mes != null 
                            || 
                            (dataMinima != null && dataMaxima != null))
                    ){
                if(mes != null){
                    dataMinima = mes.atDay(1);
                    dataMaxima = mes.atEndOfMonth();
                }
                    
                lancamentos = dao.listar(
                            entityManager, 
                            conta, 
                            dataMinima, 
                            dataMaxima);
            } else {
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
            }
            
        } finally {
            entityManager.close();
        }
            
        return buildResponse(request, lancamentos);
    }
}
