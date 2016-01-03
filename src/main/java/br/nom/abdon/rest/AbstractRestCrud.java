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
package br.nom.abdon.rest;

import br.nom.abdon.dal.DalException;
import br.nom.abdon.dal.Dao;
import br.nom.abdon.dal.EntityNotFoundException;
import br.nom.abdon.modelo.Entidade;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

/**
 * @param <E>
 * @param <Key>
 *
 * @author Bruno Abdon
 */
public abstract class AbstractRestCrud <E extends Entidade<Key>,Key>{

    private static final Logger LOG = 
        Logger.getLogger(AbstractRestCrud.class.getName());

    private static final Response ERROR_MISSING_ENTITY = 
        Response.status(Response.Status.BAD_REQUEST)
                .entity("br.nom.abdon.rest.MISSING_ENTITY")
                .build();
    
    @PersistenceUnit(unitName = "gastoso_peruni")
    protected EntityManagerFactory emf;

    private final String path;

    public AbstractRestCrud(String path) {
        this.path = path + "/" ;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response criar(E entity) {

        Response response;

        if(entity == null){
            response = ERROR_MISSING_ENTITY;
            
        } else {

            final EntityManager entityManager = emf.createEntityManager();
            try {

                entityManager.getTransaction().begin();

                getDao().criar(entityManager, entity);

                entityManager.getTransaction().commit();

                response = 
                    Response.created(makeURI(entity)).entity(entity).build();

            } catch (DalException e){
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
    
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response pegar(@Context Request request, @PathParam("id") Key id){

        final Response response;
        
        final EntityManager entityManager = emf.createEntityManager();
        try {
            final E entity = getDao().find(entityManager, id);
            
            final EntityTag tag = new EntityTag(Integer.toString(entity.hashCode()));
            Response.ResponseBuilder builder = request.evaluatePreconditions(tag);
            if(builder==null){
		//preconditions are not met and the cache is invalid
		//need to send new value with reponse code 200 (OK)
		builder = Response.ok(entity);
		builder.tag(tag);
            }
            response = builder.build();
            
        } catch ( EntityNotFoundException ex){
            throw new NotFoundException(ex);
        } catch (DalException ex) {
            throw new WebApplicationException(
                ex.getMessage(), 
                Response.Status.BAD_REQUEST);
        } finally {
            entityManager.close();
        }
        return response;
    }

    @POST
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response atualizar(@PathParam("id") Key id, E entity){
        
        Response response;
        
        if(entity == null){
            response = ERROR_MISSING_ENTITY;
            
        } else {

            entity.setId(id);
            EntityManager entityManager = emf.createEntityManager();
            try {
                entityManager.getTransaction().begin();

                getDao().atualizar(entityManager, entity);

                entityManager.getTransaction().commit();

                response = Response.noContent().build();
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
    
    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deletar(@PathParam("id") Key id) {

        Response response;
        
        EntityManager entityManager = emf.createEntityManager();
        try {
            entityManager.getTransaction().begin();

            getDao().deletar(entityManager, id);
            
            entityManager.getTransaction().commit();

            response = Response.noContent().build();
            
        } catch(EntityNotFoundException ex){
            throw new NotFoundException(ex);
        } catch (DalException e) {
            response =
                Response.status(Response.Status.CONFLICT)
                        .entity(e.getMessage())
                        .build();
        } finally {
            entityManager.close();
        }
        
        return response;
    }

    protected Response buildResponse(
            final Request request, final List<?> entidades){
        
        final EntityTag tag = 
            new EntityTag(Integer.toString(entidades.hashCode()));

        Response.ResponseBuilder builder = request.evaluatePreconditions(tag);
        
        if(builder==null){
            //preconditions are not met and the cache is invalid
            //need to send new value with reponse code 200 (OK)
            builder = Response.ok(entidades);
            builder.tag(tag);
        }
        return builder.build();
    }

    protected URI makeURI(E entity) {
        URI uri;
        
        try {
            uri = new URI(path + String.valueOf(entity.getId()));
        } catch (URISyntaxException ex) {
            LOG.log(Level.SEVERE, null, ex);
            uri = null;
        }
        return uri;
    }
    
    protected abstract Dao<E,Key> getDao();
    
}
