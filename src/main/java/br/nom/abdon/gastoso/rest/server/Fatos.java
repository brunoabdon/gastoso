package br.nom.abdon.gastoso.rest.server;

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
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import br.nom.abdon.dal.DalException;
import br.nom.abdon.dal.EntityNotFoundException;
import br.nom.abdon.gastoso.Conta;
import br.nom.abdon.gastoso.Lancamento;
import br.nom.abdon.gastoso.rest.server.dal.FatosDetalhadosDao;
import br.nom.abdon.gastoso.rest.FatoDetalhado;
import br.nom.abdon.gastoso.rest.MediaTypes;
import br.nom.abdon.gastoso.system.FiltroContas;
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
@Consumes(MediaTypes.APPLICATION_GASTOSO_PATCH)
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
        
        final EntityManager entityManager = emf.createEntityManager();
        
        try {

            fatosNormais = 
                lancamentosDao
                .listar(entityManager, filtroLancamentos)
                .stream()
                .collect(GROUP_BY_FATO_COLLECTOR)
                .entrySet()
                .parallelStream()
                .map(e -> new FatoDetalhado(e.getKey(), e.getValue()))
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

        final GenericEntity<List<FatoDetalhado>> genericEntity = 
            new GenericEntity<List<FatoDetalhado>>(fatosNormais){};

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
        
        return new FatoDetalhado(fato, lancamentos);
    }

    @POST
    @Path("{id}/{contaId}")
    public Response atualizar(
            final @PathParam("id") Integer id, 
            final @PathParam("contaId") Integer contaId, 
            final Lancamento lancamento){
        
        Response response;
        
        if(lancamento == null){
            response = ERROR_MISSING_ENTITY;
            
        } else {

            EntityManager entityManager = emf.createEntityManager();
            try {
                entityManager.getTransaction().begin();

                FiltroFatos filtroFatos = new FiltroFatos();
                filtroFatos.setFato(new Fato(id));
                
                FiltroContas filtroContas = new FiltroContas();
                filtroContas.setConta(new Conta(contaId));
                
                FiltroLancamentos filtroLancamentos = new FiltroLancamentos();
                filtroLancamentos.setFiltroFatos(filtroFatos);
                filtroLancamentos.setFiltroContas(filtroContas);
                
                final Lancamento lancamentoOriginal = 
                    lancamentosDao.findUnique(entityManager, filtroLancamentos);
                
                lancamentoOriginal.setValor(lancamento.getValor());
                Conta conta = lancamento.getConta();
                if(conta != null) lancamentoOriginal.setConta(conta);

                lancamentosDao.atualizar(entityManager, lancamentoOriginal);
                
                entityManager.getTransaction().commit();

                response = Response.noContent().build();

            } catch ( EntityNotFoundException ex){
                throw new NotFoundException(ex);
            } catch (DalException e) {
                response =
                    Response.status(Response.Status.CONFLICT)
                            .entity(e.getMessage())
                            .build();
            } finally {
                entityManager.close();
            }
        }
        return response;
    }

    @Override
    protected Fato prepararAtualizacao(
            final EntityManager entityManager, 
            final Fato fato, 
            final Integer id) {
        
        final Fato fatoOriginal = entityManager.find(Fato.class, id);
        
        final LocalDate dia = fato.getDia();
        final String descricao = fato.getDescricao();
        
        if(dia != null) fatoOriginal.setDia(fato.getDia());
        if(descricao != null) fatoOriginal.setDescricao(descricao);
        
        return fatoOriginal;
        
    }
}
