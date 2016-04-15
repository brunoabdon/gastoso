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
package br.nom.abdon.gastoso.rest.server;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import br.nom.abdon.dal.DalException;
import br.nom.abdon.dal.EntityNotFoundException;
import br.nom.abdon.gastoso.Conta;
import br.nom.abdon.gastoso.rest.Saldo;
import br.nom.abdon.gastoso.rest.server.dal.AggregateDao;
import br.nom.abdon.gastoso.rest.server.dal.FiltroSaldo;
import br.nom.abdon.gastoso.rest.MediaTypes;


/**
 * @author Bruno Abdon
 */
@Path("")
@Produces({
    MediaTypes.APPLICATION_GASTOSO_FULL,
    MediaTypes.APPLICATION_GASTOSO_NORMAL,
    MediaTypes.APPLICATION_GASTOSO_SIMPLES
})
public class MaisRs {

    private static final LocalDate BIG_BANG = LocalDate.of(1979, Month.APRIL, 26);
    
    @PersistenceUnit(unitName = "gastoso_peruni")
    protected EntityManagerFactory emf;

    private final AggregateDao aggregateDao;

    public MaisRs() {
        this.aggregateDao = new AggregateDao();
    }
    
    @GET
    @Path("/saldos/{id}")
    public Response saldo(
        final @Context Request request,
        final @Context HttpHeaders httpHeaders,
        final @PathParam("id") Integer contaId,
        final @QueryParam("dia") LocalDate dia){
            
        final EntityManager entityManager = emf.createEntityManager();
        
        Response response;
        
        if(contaId == null || dia == null) 
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        
        try {
            
            final Conta conta = new Conta(contaId);
            final Saldo saldo = 
                aggregateDao.findSaldo(entityManager, conta, dia);
            
            response = buildResponse(request, httpHeaders, saldo);
            
        } catch (DalException ex) {
            response = dealWith(ex);
        } finally {
            entityManager.close();
        }
        
        return response;
    }

    @GET
    @Path("/saldos")
    public Response saldos(
        final @Context Request request,
        final @Context HttpHeaders httpHeaders,
        final @QueryParam("dia") LocalDate dia){
        
        final EntityManager entityManager = emf.createEntityManager();
        
        Response response;
        
        if(dia == null) 
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        
        try {

            final FiltroSaldo filtroSaldo = new FiltroSaldo();
            filtroSaldo.setDia(dia);
            filtroSaldo.addOrdem(FiltroSaldo.ORDEM.POR_CONTA);
            
            final List<Saldo> saldos = 
                aggregateDao.list(entityManager, filtroSaldo);
            
            response = 
                buildResponse(
                    request, 
                    httpHeaders, 
                    new GenericEntity<List<Saldo>>(saldos){});
            
        } catch (DalException ex) {
            response = dealWith(ex);
        } finally {
            entityManager.close();
        }
        
        return response;
    }
    
    private Response buildResponse(
            final Request request, 
            final HttpHeaders headers, 
            final Object entity) {

        final EntityTag tag = makeTag(entity,headers);
        
        Response.ResponseBuilder builder = request.evaluatePreconditions(tag);
        if(builder==null){
            //preconditions are not met and the cache is invalid
            //need to send new value with reponse code 200 (OK)
            builder = Response.ok(entity);
            builder.tag(tag);
        }
        return builder.build();
    }

    private EntityTag makeTag(
        final Object thing, 
        final HttpHeaders httpHeaders) {
        
        final String accept = httpHeaders.getHeaderString(HttpHeaders.ACCEPT);

        final int hashCode = 
            new HashCodeBuilder(3,23)
                .append(thing)
                .append(accept)
                .toHashCode();
        
        return new EntityTag(Integer.toString(hashCode));

    }

    private Response dealWith(DalException ex) {
        
        final Response.StatusType status = 
            (ex instanceof EntityNotFoundException)
                ? Response.Status.NOT_FOUND
                : Response.Status.INTERNAL_SERVER_ERROR;
        
        return Response.status(status).entity(ex.getMessage()).build();
    }
}