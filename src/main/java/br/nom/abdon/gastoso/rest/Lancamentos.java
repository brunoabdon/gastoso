package br.nom.abdon.gastoso.rest;

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
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import br.nom.abdon.gastoso.Conta;
import br.nom.abdon.gastoso.Fato;
import br.nom.abdon.gastoso.Lancamento;
import br.nom.abdon.gastoso.dal.LancamentosDao;
import br.nom.abdon.gastoso.system.FiltroContas;
import br.nom.abdon.gastoso.system.FiltroFatos;
import br.nom.abdon.gastoso.system.FiltroLancamentos;
import br.nom.abdon.rest.AbstractRestCrud;


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
            final @Context HttpHeaders httpHeaders,
            final @QueryParam("fato") Fato fato,
            final @QueryParam("conta") Conta conta,
            final @QueryParam("mes") YearMonth mes,
            @QueryParam("dataMin") LocalDate dataMinima,
            @QueryParam("dataMax") LocalDate dataMaxima){
        
        final List<Lancamento> lancamentos;

        final EntityManager entityManager = emf.createEntityManager();

        try {
            if(fato != null){
                final FiltroFatos filtroFatos = new FiltroFatos();
                filtroFatos.setFato(fato);
                FiltroLancamentos filtroLancamentos = new FiltroLancamentos();
                filtroLancamentos.setFiltroFatos(filtroFatos);

                lancamentos = dao.listar(entityManager, filtroLancamentos);
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
                    
                FiltroContas filtroContas = new FiltroContas();
                filtroContas.setConta(conta);

                FiltroFatos filtroFatos = new FiltroFatos();
                filtroFatos.setDataMinima(dataMinima);
                filtroFatos.setDataMaxima(dataMaxima);
                
                FiltroLancamentos filtroLancamentos = new FiltroLancamentos();
                filtroLancamentos.setFiltroContas(filtroContas);
                filtroLancamentos.setFiltroFatos(filtroFatos);
                
                lancamentos = dao.listar(entityManager, filtroLancamentos);
            } else {
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
            }
            
        } finally {
            entityManager.close();
        }
            
        return buildResponse(request, httpHeaders, lancamentos);
    }
}
