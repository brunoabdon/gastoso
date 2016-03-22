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

/**
 *
 * @author Bruno Abdon
 */
public class GastosoRestClient implements GastosoSystem{

    private final WebTarget rootWebTarget;
    private WebTarget contaWebTarget, 
                        fatoWebTarget, 
                        contasWebTarget, 
                        fatosWebTarget;

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
    public boolean login(final String user, String password)
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
    public Fato getFato(int id) throws NotFoundException, GastosoSystemRTException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Conta> getContas(FiltroContas filtro) throws GastosoSystemRTException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Conta getConta(int id) throws NotFoundException, GastosoSystemRTException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Lancamento> getLancamentos(FiltroLancamento fitro) throws GastosoSystemRTException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void update(Fato fato) throws NotFoundException, GastosoSystemRTException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void update(Conta conta) throws GastosoSystemException, GastosoSystemRTException {

        final Response response = this.contaWebTarget
                .resolveTemplate("id", conta.getId())
                .request(MediaType.APPLICATION_JSON_TYPE)
                .buildPost(Entity.json(conta))
                .invoke();

        final Response.StatusType statusInfo = response.getStatusInfo();

        if(statusInfo.getStatusCode() == 
                Response.Status.NOT_FOUND.getStatusCode()){
            throw new NotFoundException();
        }
        
        if(statusInfo.getFamily() != Response.Status.Family.SUCCESSFUL){
            throw new GastosoSystemException(response.readEntity(String.class));
        }
    }

    @Override
    public void update(Lancamento lancamento) throws NotFoundException, GastosoSystemRTException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteFato(int id) throws NotFoundException, GastosoSystemRTException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteConta(int id) throws NotFoundException, GastosoSystemRTException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteLancamento(int id) throws NotFoundException, GastosoSystemRTException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Fato create(Fato fato) throws GastosoSystemRTException, GastosoSystemException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Conta create(final Conta conta) 
        throws GastosoSystemRTException, GastosoSystemException {

        final Invocation buildPost = 
                this.contasWebTarget
                .request(MediaType.APPLICATION_JSON_TYPE)
                .buildPost(Entity.json(conta));

        final Response response = buildPost.invoke();

        if(response.getStatusInfo().getFamily() != 
            Response.Status.Family.SUCCESSFUL){
            throw new GastosoSystemException(response.readEntity(String.class));
        }

        return response.readEntity(Conta.class);

    }

    @Override
    public Lancamento create(Lancamento lancamento) throws GastosoSystemRTException, GastosoSystemException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}