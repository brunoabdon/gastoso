package br.nom.abdon.gastoso.rest;

import br.nom.abdon.gastoso.Fato;
import br.nom.abdon.gastoso.dal.FatosDao;
import br.nom.abdon.gastoso.dal.LancamentosDao;
import br.nom.abdon.rest.AbstractRestCrud;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import jersey.repackaged.com.google.common.base.Suppliers;

import br.nom.abdon.dal.DalException;
import br.nom.abdon.gastoso.Lancamento;
import br.nom.abdon.gastoso.rest.model.FatoNormal;
import br.nom.abdon.gastoso.system.FiltroFatos;
import br.nom.abdon.gastoso.system.FiltroLancamentos;


/**
 *
 * @author bruno
 */
@Path(Fatos.PATH)
@Produces({
    MediaTypes.APPLICATION_GASTOSO_FULL,
    MediaTypes.APPLICATION_GASTOSO_NORMAL,
    MediaTypes.APPLICATION_GASTOSO_SIMPLES
})
public class Fatos extends AbstractRestCrud<Fato,Integer>{

    protected static final String PATH = "fatos";
    
    private static final int MAX_RESULTS = 200;
    
    private static final Collector<Lancamento, 
                                    ?, 
                                    ConcurrentMap<Fato, List<Lancamento>>> 
        GROUP_BY_FATO_COLLECTOR = 
            Collectors.groupingByConcurrent(Lancamento::getFato);
 
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
        final @Context HttpHeaders httpHeaders,
        final @QueryParam("mes") YearMonth mes,
        @QueryParam("dataMin") LocalDate dataMinima,
        @QueryParam("dataMax") LocalDate dataMaxima){

        final List<FatoNormal> fatosNormais;

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
            
        FiltroFatos filtroFatos = new FiltroFatos();
        filtroFatos.setDataMaxima(dataMaxima);
        filtroFatos.setDataMinima(dataMinima);
        filtroFatos.setQuantos(MAX_RESULTS);

        FiltroLancamentos filtroLancamentos = new FiltroLancamentos();
        filtroLancamentos.setFiltroFatos(filtroFatos);
        
        final EntityManager entityManager = emf.createEntityManager();
        
        try {

            fatosNormais = 
                lancamentosDao
                .listar(entityManager, filtroLancamentos)
                .stream()
                .collect(GROUP_BY_FATO_COLLECTOR)
                .entrySet()
                .parallelStream()
                .map(e -> new FatoNormal(e.getKey(), e.getValue()))
                .collect(Collectors.toCollection(ArrayList::new));
            
            fatosNormais.sort(
                (f1,f2) -> {
                    int diff = f1.getDia().compareTo(f2.getDia());
                    if(diff == 0) diff = f1.getId() - f2.getId();
                    return diff;
                });
            
        } finally {
            entityManager.close();
        }

        final GenericEntity<List<FatoNormal>> genericEntity = 
            new GenericEntity<List<FatoNormal>>(fatosNormais){};

        return buildResponse(request, httpHeaders, genericEntity);
    }

    @Override
    protected Fato getEntity(
            final EntityManager entityManager, 
            final Integer id) throws DalException {
        
        final FiltroFatos filtroFatos = new FiltroFatos();
        filtroFatos.setFato(new Fato(id));
        final FiltroLancamentos filtroLancamentos = new FiltroLancamentos();
        filtroLancamentos.setFiltroFatos(filtroFatos);
        
        final List<Lancamento> lancamentos = 
            lancamentosDao.listar(entityManager, filtroLancamentos);
        
        final Fato fato = 
            lancamentos.isEmpty()
                ? dao.find(entityManager, id)
                : lancamentos.get(0).getFato();
        
        return new FatoNormal(fato, lancamentos);
    }
    
    
}
