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

import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import static java.time.format.DateTimeFormatter.ISO_DATE;
import java.util.List;
import java.util.function.BiFunction;
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

import br.nom.abdon.gastoso.Conta;
import br.nom.abdon.gastoso.Fato;
import br.nom.abdon.gastoso.Lancamento;
import br.nom.abdon.gastoso.rest.FatoDetalhado;
import static br.nom.abdon.gastoso.rest.MediaTypes.APPLICATION_GASTOSO_FULL_TYPE;
import static br.nom.abdon.gastoso.rest.MediaTypes.APPLICATION_GASTOSO_PATCH_TYPE;
import static br.nom.abdon.gastoso.rest.MediaTypes.APPLICATION_GASTOSO_SIMPLES_TYPE;
import br.nom.abdon.gastoso.rest.Saldo;
import br.nom.abdon.gastoso.rest.serial.GastosoMessageBodyReader;
import br.nom.abdon.gastoso.rest.serial.GastosoMessageBodyWriter;
import br.nom.abdon.gastoso.system.FiltroContas;
import br.nom.abdon.gastoso.system.FiltroFatos;
import br.nom.abdon.gastoso.system.FiltroLancamentos;
import br.nom.abdon.gastoso.system.GastosoSystem;
import br.nom.abdon.gastoso.system.GastosoSystemException;
import br.nom.abdon.gastoso.system.GastosoSystemRTException;
import br.nom.abdon.gastoso.system.NotFoundException;
import br.nom.abdon.modelo.Entidade;


/**
 *
 * @author Bruno Abdon
 */
public class GastosoRestClient implements GastosoSystem{

    private static final GenericType<List<Lancamento>> LANCAMENTO_GEN_TYPE = 
        new GenericType<List<Lancamento>>(){};

    private static final GenericType<List<FatoDetalhado>> FATO_GEN_TYPE = 
        new GenericType<List<FatoDetalhado>>(){};
    
    private static final GenericType<List<Conta>> CONTA_GEN_TYPE = 
        new GenericType<List<Conta>>(){};
    
    private static final GenericType<List<Saldo>> SALDO_GEN_TYPE = 
        new GenericType<List<Saldo>>(){};

    private static final ClientRequestFilter USER_AGENT_FILTER = 
        (reqContx) -> {
            reqContx
            .getHeaders()
            .add(HttpHeaders.USER_AGENT, "gastoso-cli"); //pegar de um resource
    };

    
    private final WebTarget rootWebTarget;
    
    private final WebTarget contaWebTarget, contasWebTarget;
    private final WebTarget fatoWebTarget, fatosWebTarget;
    private final WebTarget lancamentoWebTarget, lancamentosWebTarget;

    private boolean logado = false;

    public GastosoRestClient(final String serverUri) throws URISyntaxException {
        this(new URI(serverUri));
    }

    public GastosoRestClient(final URI serverUri) {

        try {
            final SSLContext sslContext = SSLContext.getDefault();
            final Client client =
                ClientBuilder
                    .newBuilder()
                    .sslContext(sslContext)
                    .register(GastosoMessageBodyReader.class)
                    .register(GastosoMessageBodyWriter.class)
                    .build();

            rootWebTarget = client.target(serverUri);

        } catch (NoSuchAlgorithmException e) {
            throw new GastosoSystemRTException(e);
        }
        
        rootWebTarget.register(USER_AGENT_FILTER);

        this.contasWebTarget = rootWebTarget.path("contas");
        this.fatosWebTarget = rootWebTarget.path("fatos");
        this.lancamentosWebTarget = rootWebTarget.path("lancamentos");

        this.contaWebTarget = this.contasWebTarget.path("{id}");
        this.fatoWebTarget = this.fatosWebTarget.path("{id}");
        this.lancamentoWebTarget = this.lancamentosWebTarget.path("{id}");
    } 
    
    public boolean login (
        final String user, 
        final String password) throws GastosoSystemException {
        /*
        esse método nao devria lancar GastosoSystemException.
        
        ele está lançando pq invoke(invocation) lança, mas não 
        deveria.
        
        idealmente, invoke deveria lancar algum tipo de 
        InvocationException e essa exceção seria transformada
        em loginException ou restclient exception aqui, e 
        transformada em GastosoException nos outros métodos, 
        que implementam realmente coisas do gastosoSystem.
        
        */

        if(this.logado) throw new IllegalStateException();

        final Invocation invocation =
            this.rootWebTarget.path("login")
            .request(MediaType.APPLICATION_JSON_TYPE)
            .buildPost(Entity.text(password));
            
        final Response response = invoke(invocation);

        if(this.logado =
            response.getStatusInfo().getFamily() == 
            Response.Status.Family.SUCCESSFUL){

            final String authToken = 
                readEntity(response, AuthToken.class).token;
            
            final ClientRequestFilter authFilter = 
                (reqContx) -> {
                    if(this.logado)
                    reqContx
                        .getHeaders()
                        .add("X-Abd-auth_token", authToken);
                }
            ;

            rootWebTarget.register(authFilter);
        }
        return this.logado;
    }

    public boolean logout()
            throws GastosoSystemRTException, IllegalStateException {

        if(!this.logado) throw new IllegalStateException();

        this.logado = false;

        return true;
    }

    @Override
    public List<FatoDetalhado> getFatos(final FiltroFatos filtro) 
            throws GastosoSystemException {
        
        return get(
            FILL_PARAM_FATOS, 
            FATO_GEN_TYPE,
            APPLICATION_GASTOSO_SIMPLES_TYPE,
            fatoWebTarget, 
            filtro);
    }

    @Override
    public List<Conta> getContas(final FiltroContas filtro) 
            throws GastosoSystemException {
        
        return get(
            (wt,f) -> wt, 
            CONTA_GEN_TYPE,
            APPLICATION_GASTOSO_FULL_TYPE,
            contasWebTarget, 
            filtro);
    }

    @Override
    public List<Lancamento> getLancamentos(final FiltroLancamentos filtro) 
            throws GastosoSystemException {
        
        return get(
            FILL_PARAM_LANCAMENTOS, 
            LANCAMENTO_GEN_TYPE,
            APPLICATION_GASTOSO_FULL_TYPE,
            lancamentosWebTarget, 
            filtro);
    }
    

    private static final BiFunction<WebTarget, FiltroFatos, WebTarget> 
        FILL_PARAM_FATOS =
        (wt,f) -> {
            wt = setData(wt, "dataMin", f.getDataMinima());
            wt = setData(wt, "dataMax", f.getDataMaxima());
            return wt;
    };

    //helper for FILL_PARAM_FATOS
    private static WebTarget setData(
        WebTarget webTarget, 
        final String paramName, 
        final LocalDate date){

        return date != null 
            ? webTarget.queryParam(paramName, date.format(ISO_DATE))
            : webTarget;
    }

    private static final BiFunction<WebTarget, FiltroLancamentos, WebTarget> 
        FILL_PARAM_LANCAMENTOS =

        (lancamentosWebTarget,filtro) -> {
            final FiltroFatos filtroFatos = filtro.getFiltroFatos();
            final FiltroContas filtroContas = filtro.getFiltroContas();

            final Integer fatoId = filtroFatos.getId();
            final Integer contaId = filtroContas.getId();

            final WebTarget webTarget;

            if(fatoId != null){
                webTarget = lancamentosWebTarget.queryParam("fato", fatoId);
            } else if(contaId != null){
                webTarget = 
                    lancamentosWebTarget.queryParam("conta", fatoId)
                    .queryParam("dataMin", filtroFatos.getDataMinima())
                    .queryParam("dataMax", filtroFatos.getDataMaxima());
            } else {
                throw new GastosoSystemRTException("Deu ruim");
            }

            return webTarget;
    };

    @Override
    public Fato getFato(int id)
            throws GastosoSystemRTException, GastosoSystemException {
        return get(fatoWebTarget,Fato.class,id);
    }

    @Override
    public Conta getConta(int id) 
            throws GastosoSystemRTException, GastosoSystemException {
        return get(contaWebTarget,Conta.class,id);
    }

    @Override
    public Fato update(Fato fato) throws GastosoSystemException {
        return update(fatoWebTarget, fato, Fato.class);
    }

    @Override
    public Conta update(Conta conta) throws GastosoSystemException {
        return update(contaWebTarget, conta, Conta.class);
    }
    
    @Override
    public Lancamento update(Lancamento lancamento) throws GastosoSystemException {
        return update(lancamentoWebTarget,lancamento,Lancamento.class);
    }

    @Override
    public void deleteFato(int id) throws GastosoSystemException {
        delete(fatoWebTarget, id);
    }

    @Override
    public void deleteConta(int id) throws GastosoSystemException {
        delete(contaWebTarget, id);
    }
    
    @Override
    public void deleteLancamento(int id) throws GastosoSystemException {
        delete(lancamentoWebTarget, id);
    }

    @Override
    public Fato create(Fato fato) throws GastosoSystemRTException, GastosoSystemException { 
        return create(
            fatosWebTarget,
            fato,
            (fato instanceof FatoDetalhado) ? FatoDetalhado.class : Fato.class);
    }

    @Override
    public Conta create(final Conta conta) 
            throws GastosoSystemRTException, GastosoSystemException {
        return create(contasWebTarget,conta,Conta.class);
    }
    
    @Override
    public Lancamento create(Lancamento lancamento) throws GastosoSystemRTException, GastosoSystemException {
        return create(contasWebTarget,lancamento,Lancamento.class);
    }

    private static <E,F> List<E> get(
            final BiFunction<WebTarget, F, WebTarget> fillParams,
            final GenericType<List<E>> genericType,
            final MediaType mediaType,
            final WebTarget webTarget,
            final F filtro) 
                throws GastosoSystemException {
        
        final List<E> entities;
        
        final Response response = 
            requestOperation(
                fillParams.apply(webTarget, filtro), 
                Invocation.Builder::buildGet,
                mediaType);
        
        try {
            entities = response.readEntity(genericType);
        } catch (ProcessingException pe){
            throw new GastosoSystemRTException(pe);
        }
        
        return entities;
    }
    
    private <E extends Entidade<Integer>> E get(
        final WebTarget baseWebTarget, 
        final Class<E> klass,
        final int id) 
            throws GastosoSystemException{

        final Response response = 
            requestOperation(
                baseWebTarget.resolveTemplate("id", id), 
                Invocation.Builder::buildGet,
                APPLICATION_GASTOSO_FULL_TYPE);
        
        return readEntity(response, klass);
    }
    
    private static <E extends Entidade<Integer>> E update(
            final WebTarget baseWebTarget, 
            final E entidade,
            final Class<E> klass) 
                throws GastosoSystemException {

        return post(
            baseWebTarget.resolveTemplate("id", entidade.getId()), 
            entidade, 
            klass);
    }

    private static <E extends Entidade<Integer>> E create(
            final WebTarget baseWebTarget, 
            final E entidade,
            final Class<? extends E> klass)
        throws GastosoSystemRTException, GastosoSystemException {

        return post(baseWebTarget,entidade,klass);
    }

    private static void delete(final WebTarget baseWebTarget, int id) 
            throws GastosoSystemRTException, GastosoSystemException {
        
        requestOperation(
            baseWebTarget.resolveTemplate("id", id), 
            Invocation.Builder::buildDelete,
            MediaType.WILDCARD_TYPE);
    }
    
    private static<E extends Entidade<Integer>> E post(
        final WebTarget webTarget,
        final E entidade,
        final Class<? extends E> klass) 
            throws GastosoSystemException{
        
        final Entity<E> entity = 
            Entity.entity(entidade, APPLICATION_GASTOSO_PATCH_TYPE);
    
        final Response response = 
            requestOperation(
                webTarget, 
                b -> b.buildPost(entity), 
                APPLICATION_GASTOSO_FULL_TYPE);
        
        return readEntity(response, klass);
    }

    private static Response requestOperation(
            final WebTarget webTarget,
            final Function<Invocation.Builder,Invocation> invocFunc,
            final MediaType acceptedMediaType) 
                throws GastosoSystemException{

        final Invocation.Builder resourceBuilder = webTarget.request();
        
        resourceBuilder.accept(acceptedMediaType);
        
        final Invocation invocation = invocFunc.apply(resourceBuilder);
        
        return invoke(invocation);
    }
    
    /**
     * Chama o método {@link Invocation#invoke() invoke} do Invocation 
     * passado, tratando as exceções.
     * 
     * @param invocation Uma instância qualquer de {@link Invocation}.
     * @return A resposta da requisição.
     */
    private static Response invoke(
            final Invocation invocation) throws GastosoSystemException{
    
        final Response response; 
        try {
            response = invocation.invoke();
        } catch (ProcessingException pe){
            Throwable cause = pe.getCause();
            if(cause instanceof ConnectException){
                throw new GastosoSystemRTException("Servidor fora do ar.",pe);
            }
            throw new GastosoSystemRTException("Impossível lidar.",pe);
        }
        
        final Response.StatusType statusInfo = response.getStatusInfo();

        if(statusInfo.getStatusCode() ==
                Response.Status.NOT_FOUND.getStatusCode()){
            throw new NotFoundException();
        }
        //todo: separar erros 500 de 400 (erro do cliente)
        if(statusInfo.getFamily() != Response.Status.Family.SUCCESSFUL){
            throw new GastosoSystemException(
                String.format("%d - %s [%s]", 
                        statusInfo.getStatusCode(),
                        statusInfo.getReasonPhrase(),
                        response.readEntity(String.class))
            );
                
        }
        
        return response;
    }
    
    private static <E> E readEntity(
            final Response response, 
            final Class<E> klass) {
        final E entity;
        
        try {
            entity = response.readEntity(klass);
        } catch (ProcessingException pe){
            throw new GastosoSystemRTException(pe);
        }
        
        return entity;
    }   
}