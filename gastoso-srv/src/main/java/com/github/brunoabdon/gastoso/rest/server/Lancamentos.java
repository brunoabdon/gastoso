package com.github.brunoabdon.gastoso.rest.server;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

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

import com.github.brunoabdon.commons.rest.AbstractRestCrud;
import com.github.brunoabdon.gastoso.Conta;
import com.github.brunoabdon.gastoso.Fato;
import com.github.brunoabdon.gastoso.Lancamento;
import com.github.brunoabdon.gastoso.dal.LancamentosDao;
import com.github.brunoabdon.gastoso.rest.MediaTypes;
import com.github.brunoabdon.gastoso.system.FiltroFatos;
import com.github.brunoabdon.gastoso.system.FiltroLancamentos;


/**
 *
 * @author bruno
 */
@Path(Lancamentos.PATH)
@Produces({
    MediaTypes.APPLICATION_GASTOSO_FULL,
    MediaTypes.APPLICATION_GASTOSO_SIMPLES
})
@Consumes(MediaTypes.APPLICATION_GASTOSO_PATCH)
public class Lancamentos extends AbstractRestCrud<Lancamento, Integer> {

    protected static final String PATH = "lancamentos";
            
    private final LancamentosDao dao;

    public Lancamentos() {
        super(PATH);
        this.dao = new LancamentosDao();
    }

    @Override
    protected LancamentosDao getDao() {
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

        final boolean temFato = fato != null;
        final boolean temConta = conta != null;
        final boolean temMes = mes != null;
        final boolean temDataMinima = dataMinima != null;
        final boolean temDataMaxima = dataMaxima != null;

        final boolean temPeriodoValido = 
            temMes ^ (temDataMinima && temDataMaxima); //xor

        final boolean dahPraConsultar = 
            temFato || (temConta && temPeriodoValido);

        if(!dahPraConsultar){
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        final FiltroLancamentos filtroLancamentos = new FiltroLancamentos();
        final FiltroFatos filtroFatos = filtroLancamentos.getFiltroFatos();

        if(temFato) filtroFatos.setFato(fato);
            
        if(temConta) {
            filtroLancamentos.getFiltroContas().setConta(conta);
            filtroLancamentos.addOrdem(FiltroLancamentos.ORDEM.POR_DIA_ASC);
        }

        filtroLancamentos.addOrdem(FiltroLancamentos.ORDEM.POR_FATO_ID_ASC);
        
        if(temPeriodoValido){
            if(temMes){
                dataMinima = mes.atDay(1);
                dataMaxima = mes.atEndOfMonth();
            }

            filtroFatos.setDataMinima(dataMinima);
            filtroFatos.setDataMaxima(dataMaxima);
        }

        try {
            lancamentos = dao.listar(entityManager, filtroLancamentos);

        } finally {
            entityManager.close();
        }

        return buildResponse(request, httpHeaders, lancamentos);
    }
}
