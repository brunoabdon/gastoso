package com.github.brunoabdon.gastoso.rest.server;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import com.github.brunoabdon.commons.dal.DalException;
import com.github.brunoabdon.commons.rest.AbstractRestCrud;
import com.github.brunoabdon.gastoso.Fato;
import com.github.brunoabdon.gastoso.Lancamento;
import com.github.brunoabdon.gastoso.dal.FatosDao;
import com.github.brunoabdon.gastoso.dal.LancamentosDao;
import com.github.brunoabdon.gastoso.ext.FatoDetalhado;
import com.github.brunoabdon.gastoso.rest.MediaTypes;
import com.github.brunoabdon.gastoso.rest.server.dal.FatosDetalhadosDao;
import com.github.brunoabdon.gastoso.system.FiltroFatos;
import com.github.brunoabdon.gastoso.system.FiltroLancamentos;


/**
 *
 * @author bruno
 */
@Path(Fatos.PATH)
@Produces({
    MediaTypes.APPLICATION_GASTOSO_FULL,
    MediaTypes.APPLICATION_GASTOSO_SIMPLES
})
@Consumes(MediaTypes.APPLICATION_GASTOSO_PATCH)
public class Fatos extends AbstractRestCrud<Fato,Integer>{

    protected static final String PATH = "fatos";
    
    private static final 
        Collector<Lancamento, ?,ConcurrentMap<Fato, List<Lancamento>>> 
        GROUP_BY_FATO_COLLECTOR = 
            Collectors.groupingByConcurrent(Lancamento::getFato);
 
    private final FatosDao dao;
    private final LancamentosDao lancamentosDao;

    public Fatos() {
        super(PATH);
        this.dao = new FatosDetalhadosDao();
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

        final List<FatoDetalhado> fatosNormais;

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
//        filtroFatos.setQuantos(MAX_RESULTS); sentido nenhum, já que consulta por lancamento

        FiltroLancamentos filtroLancamentos = new FiltroLancamentos();
        filtroLancamentos.setFiltroFatos(filtroFatos);
        
        try {

            fatosNormais = 
                lancamentosDao
                .listar(entityManager, filtroLancamentos)
                .stream()
                .collect(GROUP_BY_FATO_COLLECTOR)
                .entrySet()
                .parallelStream()
                .map(e -> new FatoDetalhado(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
            
            fatosNormais.sort(
                (f1,f2) -> {
                    int diff = f1.getDia().compareTo(f2.getDia());
                    if(diff == 0) diff = f1.getId() - f2.getId();
                    return diff;
                });
            
        } finally {
            entityManager.close();
        }

        return buildResponse(request, httpHeaders, fatosNormais);
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
        
        return new FatoDetalhado(fato, lancamentos);
    }

    @Override
    protected Fato prepararAtualizacao(
            final EntityManager entityManager, 
            final Fato fato, 
            final Integer id) {
        
        final Fato fatoOriginal = entityManager.find(Fato.class, id);
        
        if(fato.getDia() == null) 
            fato.setDia(fatoOriginal.getDia());
        
        if(fato.getDescricao() == null) 
            fato.setDescricao(fatoOriginal.getDescricao());
        
        fato.setId(id);
        
        return fato;
    }    
}