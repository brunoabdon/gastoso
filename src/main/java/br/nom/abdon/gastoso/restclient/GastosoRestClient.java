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
package br.nom.abdon.gastoso.restclient;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import br.nom.abdon.gastoso.Conta;
import br.nom.abdon.gastoso.Fato;
import br.nom.abdon.gastoso.Lancamento;
import br.nom.abdon.gastoso.system.FiltroContas;
import br.nom.abdon.gastoso.system.FiltroFatos;
import br.nom.abdon.gastoso.system.FiltroLancamento;
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

    private final WebTarget rootWebTarget;
    private WebTarget contaWebTarget, 
                        fatoWebTarget, 
                        contasWebTarget, 
                        fatosWebTarget,
                        lancamentosWebTarget,
                        lancamentoWebTarget;
;
    private boolean logado;


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
                    .build();

            this.rootWebTarget = client.target(serverUri);

        } catch (NoSuchAlgorithmException e) {
            throw new GastosoSystemRTException(e);
        }
    }

    @Override
    public boolean login(final String user, final String password)
        throws GastosoSystemRTException {

        final Response response =
            this.rootWebTarget.path("login")
            .request(MediaType.APPLICATION_JSON_TYPE)
            .post(Entity.text(password));

        if(this.logado =
            response.getStatusInfo().getFamily() == 
            Response.Status.Family.SUCCESSFUL){

            final String authToken = response.readEntity(AuthToken.class).token;
            
            final ClientRequestFilter authFilter = 
                (reqContx) -> {
                    if(this.logado)
                    reqContx
                        .getHeaders()
                        .add("X-Abd-auth_token", authToken);
                }
            ;

            this.rootWebTarget.register(authFilter);
            this.contasWebTarget = this.rootWebTarget.path("contas");
            this.fatosWebTarget = this.rootWebTarget.path("fatos");
            this.contaWebTarget = this.contasWebTarget.path("{id}");
            this.fatoWebTarget = this.fatosWebTarget.path("{id}");
            this.lancamentosWebTarget = this.rootWebTarget.path("lancamentos");
            this.lancamentoWebTarget = this.lancamentosWebTarget.path("{id}");

        }

        return this.logado;
    }

    @Override
    public boolean logout()
            throws GastosoSystemRTException, IllegalStateException {

        if(!this.logado) throw new IllegalStateException();

        this.logado = false;

        return true;
    }

    @Override
    public List<Fato> getFatos(FiltroFatos filtro) throws GastosoSystemRTException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Fato getFato(int id)
            throws GastosoSystemRTException, GastosoSystemException {
        return get(fatoWebTarget,Fato.class,id);
    }

    @Override
    public List<Conta> getContas(FiltroContas filtro) throws GastosoSystemRTException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Conta getConta(int id) 
            throws GastosoSystemRTException, GastosoSystemException {
        return get(contaWebTarget,Conta.class,id);
    }
    
    private <E extends Entidade<Integer>> E get(
        final WebTarget baseWebTarget, 
        final Class<E> klass,
        final int id) 
            throws GastosoSystemException{
        return invokeResource(
                resourceBuilder(baseWebTarget,id).buildGet(),klass);
    }

    @Override
    public List<Lancamento> getLancamentos(FiltroLancamento filtro) throws GastosoSystemRTException {
        Integer fatoId = filtro.getFatoId();
        Integer contaId = filtro.getContaId();
        
        WebTarget webTarget;
        
        if(fatoId != null){
            webTarget = lancamentosWebTarget.queryParam("fato", fatoId);
        } else if(contaId != null){
            webTarget = 
                lancamentosWebTarget.queryParam("conta", fatoId)
                .queryParam("dataMin", filtro.getDataMinima())
                .queryParam("dataMax", filtro.getDataMaxima());
            
        } else {
            throw new GastosoSystemRTException("Deu ruim");
        }
        
//        List<Lancamento> lancamentos = 
//            webTarget.request(MediaType.APPLICATION_JSON_TYPE).get();
        ;
        
        return Collections.emptyList();
    }

    @Override
    public void update(Fato fato) throws GastosoSystemException {
        update(fatoWebTarget, fato, Fato.class);
    }

    @Override
    public void update(Conta conta) throws GastosoSystemException {
        update(contaWebTarget, conta, Conta.class);
    }
    
    @Override
    public void update(Lancamento lancamento) throws NotFoundException, GastosoSystemRTException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteFato(int id) 
            throws GastosoSystemRTException, GastosoSystemException {
        this.delete(fatoWebTarget, id);
    }

    @Override
    public void deleteConta(int id) 
            throws GastosoSystemRTException, GastosoSystemException {
        this.delete(contaWebTarget, id);
    }
    
    @Override
    public void deleteLancamento(int id) throws NotFoundException, GastosoSystemRTException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Fato create(Fato fato) throws GastosoSystemRTException, GastosoSystemException {
        return create(fatosWebTarget,fato,Fato.class);
    }

    @Override
    public Conta create(final Conta conta) 
            throws GastosoSystemRTException, GastosoSystemException {
        return create(contasWebTarget,conta,Conta.class);
    }
    
    @Override
    public Lancamento create(Lancamento lancamento) throws GastosoSystemRTException, GastosoSystemException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private <E extends Entidade<Integer>> E update(
            final WebTarget baseWebTarget, 
            final E entity,
            final Class<E> klass) 
                throws GastosoSystemException, GastosoSystemRTException {
        
        final Invocation.Builder resourceBuilder = 
                resourceBuilder(baseWebTarget,entity.getId());
        
        return invokePost(resourceBuilder, klass, entity);
    }

    private <E extends Entidade<Integer>> E create(
            final WebTarget baseWebTarget, 
            final E entity,
            final Class<E> klass)
        throws GastosoSystemRTException, GastosoSystemException {

        return 
            invokePost(
                baseWebTarget
                .request(MediaType.APPLICATION_JSON_TYPE),klass,entity
            );
    }

    private void delete(final WebTarget baseWebTarget, int id) 
            throws GastosoSystemRTException, GastosoSystemException {
        
        final Response response = 
            invokeAndCheck(resourceBuilder(baseWebTarget,id).buildDelete());

        this.dealWith(response);
    }

    private Invocation.Builder resourceBuilder(
            final WebTarget baseWebTarget,
            final int id){
        
        return 
            baseWebTarget
            .resolveTemplate("id", id)
            .request(MediaType.APPLICATION_JSON_TYPE);
    }

    private <E extends Entidade<Integer>> E invokePost(
            final Invocation.Builder resourceBuilder,            
            final Class<E> klass,
            final E entity) 
                throws GastosoSystemException{
        
        return invokeResource(
                    resourceBuilder.buildPost(Entity.json(entity)),klass);
    }

    private <E extends Entidade<Integer>> E invokeResource(
        final Invocation invocation,
        final Class<E> klass) 
            throws GastosoSystemException{
        
        final Response response = invokeAndCheck(invocation);
        
        return response.readEntity(klass);
    }
    
    private Response invokeAndCheck(final Invocation invocation) 
                throws GastosoSystemException{
    
        final Response response = invocation.invoke();
        dealWith(response);
        return response;
    }

    private void successfullResponse(final Response response) throws GastosoSystemException {
        if(response.getStatusInfo().getFamily() !=
                Response.Status.Family.SUCCESSFUL){
            throw new GastosoSystemException(response.readEntity(String.class));
        }
    }
    
    private void dealWith(final Response response) 
            throws GastosoSystemException, NotFoundException { 
        
        final Response.StatusType statusInfo = response.getStatusInfo();
        
        if(statusInfo.getStatusCode() ==
                Response.Status.NOT_FOUND.getStatusCode()){
            throw new NotFoundException();
        }
        
        successfullResponse(response);
    }
}