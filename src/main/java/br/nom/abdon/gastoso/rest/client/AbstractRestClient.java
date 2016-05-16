/*
 * Copyright (C) 2016 Bruno Abdon
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
package br.nom.abdon.gastoso.rest.client;

import java.io.Closeable;
import java.net.ConnectException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.net.ssl.SSLContext;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import pl.touk.throwing.ThrowingFunction;

import static br.nom.abdon.gastoso.rest.client.RESTClientRTException.SERVIDOR_FORA;
import br.nom.abdon.util.Identifiable;

/**
 * 
 * @author Bruno Abdon
 * @param <T> o tipo de excecao que os metodos vao levantar.
 */
class AbstractRestClient <T extends Throwable> implements Closeable{

    private final ThrowingFunction<RESTResponseException,Response,T> 
        exceptionDealer;
    
    private final MediaType sendMediaType, receiveMediaType;
    
    private Client client;

    public AbstractRestClient(
        
        final MediaType sendMediaType, 
        final MediaType receiveMediaType,
        final ThrowingFunction<RESTResponseException,Response,T> exDealer) {
        
        this.exceptionDealer = exDealer;
        this.sendMediaType = sendMediaType;
        this.receiveMediaType = receiveMediaType;
    }
    
    protected WebTarget start(
        final URI serverUri,
        final String userAgent,
        final Consumer<ClientBuilder> clientConfigurator){
        
        final WebTarget rootWebTarget;
        
        try {
            
            final SSLContext sslContext = SSLContext.getDefault();
            
            final ClientRequestFilter uaFilter = 
                (reqContx) -> {
                    reqContx
                    .getHeaders()
                    .add(HttpHeaders.USER_AGENT, userAgent);
            };
            
            final ClientBuilder cb = 
                ClientBuilder
                    .newBuilder()
                    .sslContext(sslContext)
                    .register(uaFilter);
            
            clientConfigurator.accept(cb);
            
            this.client = cb.build();
            
            rootWebTarget = client.target(serverUri);

        } catch (NoSuchAlgorithmException e) {
            throw new RESTClientRTException(e);
        }
        
        return rootWebTarget;
    }

    protected <E,F> List<E> get(
            final BiFunction<WebTarget, F, WebTarget> fillParams,
            final GenericType<List<E>> genericType,
            final MediaType mediaType,
            final WebTarget webTarget,
            final F filtro) 
                throws T {
        
        final List<E> entities;
        
        final Response response = 
            requestOperation(
                fillParams.apply(webTarget, filtro), 
                Invocation.Builder::buildGet,
                mediaType);
        
        try {
            entities = response.readEntity(genericType);
        } catch (ProcessingException pe){
            throw new RESTClientRTException(pe);
        }
        
        return entities;
    }
    
    protected <E extends Identifiable<? extends Object>> E get(
        final WebTarget baseWebTarget, 
        final Class<E> klass,
        final int id) 
            throws T{

        final Response response = 
            requestOperation(
                baseWebTarget.resolveTemplate("id", id), 
                Invocation.Builder::buildGet,
                receiveMediaType);
        
        return readEntity(response, klass);
    }
    
    protected <E extends Identifiable<? extends Object>> E update(
            final WebTarget baseWebTarget, 
            final E entidade,
            final Class<E> klass) 
                throws T {

        return post(
            baseWebTarget.resolveTemplate("id", entidade.getId()), 
            entidade, 
            klass);
    }

    protected <E extends Identifiable<? extends Object>> E create(
            final WebTarget baseWebTarget, 
            final E entidade,
            final Class<? extends E> klass) 
                throws T {

        return post(baseWebTarget,entidade,klass);
    }

    protected void delete(
            final WebTarget baseWebTarget,
            int id) 
            throws T {
        
        requestOperation(
            baseWebTarget.resolveTemplate("id", id), 
            Invocation.Builder::buildDelete,
            MediaType.WILDCARD_TYPE);
    }
    
    protected <E extends Identifiable<? extends Object>> E post(
        final WebTarget webTarget,
        final E entidade,
        final Class<? extends E> klass) 
            throws T {
        
        final Entity<E> entity = 
            Entity.entity(entidade, sendMediaType);
    
        final Response response = 
            requestOperation(
                webTarget, 
                b -> b.buildPost(entity), 
                receiveMediaType);
        
        return readEntity(response, klass);
    }

    protected Response requestOperation(
            final WebTarget webTarget,
            final Function<Invocation.Builder,Invocation> invocFunc,
            final MediaType acceptedMediaType)
                throws T{

        final Invocation.Builder resourceBuilder = 
                webTarget
                    .request()
                    .accept(acceptedMediaType);
        
        final Invocation invocation = invocFunc.apply(resourceBuilder);
        
        Response response;
                
        try {
            response = invoke(invocation);
        } catch (RESTResponseException e){
            response = exceptionDealer.apply(e);
        }
        return response;
    }

    /**
     * Chama o método {@link Invocation#invoke() invoke} do Invocation 
     * passado, tratando as exceções.
     * 
     * @param invocation Uma instância qualquer de {@link Invocation}.
     * @return A resposta da requisição.
     */
    protected Response invoke(final Invocation invocation) 
                throws RESTResponseException {
    
        final Response response; 
        try {
            response = invocation.invoke();
        } catch (ProcessingException pe){
            Throwable cause = pe.getCause();
            if(cause instanceof ConnectException){
                throw 
                    new RESTClientRTException(
                            "Servidor fora do ar.",
                            pe,
                            SERVIDOR_FORA);
            }
            throw new RESTClientRTException("Impossível lidar.",pe);
        }
        
        final Response.StatusType statusInfo = response.getStatusInfo();

        if(statusInfo.getFamily() != Response.Status.Family.SUCCESSFUL){
            throw new RESTResponseException(statusInfo);
        }
        
        return response;
    }
    
    protected <E> E readEntity(
            final Response response, 
            final Class<E> klass) {

        final E entity;
        
        try {
            entity = response.readEntity(klass);
        } catch (ProcessingException pe){
            throw new RESTClientRTException(pe);
        }
        
        return entity;
    }   
    
    @Override
    public void close() {
        this.client.close();
    }
}
