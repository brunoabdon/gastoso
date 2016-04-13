/*
 * Copyright (C) 2015 Bruno Abdon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package br.nom.abdon.gastoso.rest;

import br.nom.abdon.gastoso.Conta;
import br.nom.abdon.gastoso.Fato;
import br.nom.abdon.gastoso.dal.ContasDao;
import br.nom.abdon.gastoso.dal.FatosDao;
import br.nom.abdon.gastoso.dal.LancamentosDao;
import br.nom.abdon.gastoso.rest.model.ContaDetalhe;
import br.nom.abdon.gastoso.rest.model.Extrato;
import br.nom.abdon.gastoso.rest.model.FatoDetalhe;

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
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

import br.nom.abdon.gastoso.system.FiltroFatos;
import br.nom.abdon.gastoso.system.FiltroLancamentos;

/**
 * @author Bruno Abdon
 */
@Produces(MediaType.APPLICATION_JSON)
@Path("")
public class MaisRs {

    private static final LocalDate BIG_BANG = LocalDate.of(1979, Month.APRIL, 26);
    
    @PersistenceUnit(unitName = "gastoso_peruni")
    protected EntityManagerFactory emf;

    private final ContasDao contasDao;
    private final FatosDao fatosDao;
    private final LancamentosDao lancamentosDao;

    public MaisRs() {
        this.contasDao = new ContasDao();
        this.fatosDao = new FatosDao();
        this.lancamentosDao = new LancamentosDao();
    }

    @GET
    @Path("extrato")
    public Response extratoConta(
        final @Context Request request,
        final @QueryParam("conta") Conta conta,
        final @QueryParam("mes") YearMonth mes,
        @QueryParam("dataMax") LocalDate dataMaxima){
    
        final List<Fato> fatos;
        Response.ResponseBuilder builder;
        
        if((mes == null && dataMaxima == null)
            || conta == null){
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        
        if(mes != null){
            dataMaxima  = mes.atEndOfMonth();
        }
            
        final EntityManager entityManager = emf.createEntityManager();

        final Function<Fato, FatoDetalhe> detalhaFato = 
            fato -> new FatoDetalhe(
                            fato, lancamentosDao
                                    .listar(entityManager, makeFiltro(fato)));
        try {
            fatos = fatosDao.listar(entityManager, conta, dataMaxima, 30);
            
            final List<FatoDetalhe> fatosDetalhados = 
                fatos.stream().map(detalhaFato).collect(Collectors.toList());

            Collections.reverse(fatosDetalhados);
            
            final LocalDate dataMinima;
            final long saldoInicial;
            if(fatosDetalhados.isEmpty()){
                dataMinima = BIG_BANG;
                saldoInicial = 0;
            } else {
                final FatoDetalhe fatoDetalhadoMaisAntigo = 
                    fatosDetalhados.get(0);

                dataMinima = 
                    fatoDetalhadoMaisAntigo
                    .getFato()
                    .getDia();
                
                saldoInicial = 
                    lancamentosDao
                    .valorTotalAte(
                        entityManager, 
                        fatoDetalhadoMaisAntigo.getLancamento(conta));
            }

            final Extrato extrato = 
                new Extrato(
                    conta,
                    saldoInicial,
                    dataMinima, 
                    dataMaxima,
                    fatosDetalhados);

            final EntityTag tag = 
                new EntityTag(Integer.toString(extrato.hashCode()));
            
            builder = request.evaluatePreconditions(tag);
            if(builder==null){
		//preconditions are not met and the cache is invalid
		//need to send new value with reponse code 200 (OK)
		builder = Response.ok(extrato);
		builder.tag(tag);
            }
            
        } finally {
            entityManager.close();
        }
        
        return builder.build();
    }
    
    @GET
    @Path("fatosDetalhados")
    public Response listaFatos(
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
                            lancamentosDao.listar(entityManager, makeFiltro(fato)));
        
        try {
            fatos = fatosDao.listar(entityManager, dataMinima, dataMaxima);
            
            final List<FatoDetalhe> fatosDetalhados = 
                fatos.stream().map(detalhaFato).collect(Collectors.toList());
            
            final br.nom.abdon.gastoso.rest.model.Extrato fs = 
                new br.nom.abdon.gastoso.rest.model.Extrato(
                    dataMinima, 
                    dataMaxima,
                    fatosDetalhados);

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

    @GET
    @Path("contasDetalhadas")
    public Response listaContas(final @Context Request request){

        Response.ResponseBuilder builder;
        
        final LocalDate hoje = LocalDate.now();

        final EntityManager em = emf.createEntityManager();
        
        final Function<Conta, ContaDetalhe> detalhaConta = 
            conta -> new ContaDetalhe(
                            conta, 
                            lancamentosDao.valorTotal(em, conta, hoje));

        try {
            
            final List<ContaDetalhe> contasDetalhadas = 
                contasDao
                    .listar(em)
                    .stream()
                    .map(detalhaConta)
                    .collect(Collectors.toList());

            final EntityTag tag = 
                new EntityTag(Integer.toString(contasDetalhadas.hashCode()));
            builder = request.evaluatePreconditions(tag);
            if(builder==null){
		//preconditions are not met and the cache is invalid
		//need to send new value with reponse code 200 (OK)
		builder = Response.ok(contasDetalhadas);
		builder.tag(tag);
            }
        } finally {
            em.close();
        }
 
        return builder.build();
    }
    
    private FiltroLancamentos makeFiltro(final Fato fato){
        final FiltroFatos filtroFatos = new FiltroFatos();
        filtroFatos.setFato(fato);
        final FiltroLancamentos filtroLancamentos = new FiltroLancamentos();
        filtroLancamentos.setFiltroFatos(filtroFatos);
        return filtroLancamentos;
        
    }
    
}