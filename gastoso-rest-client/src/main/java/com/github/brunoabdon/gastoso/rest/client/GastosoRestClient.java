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
package com.github.brunoabdon.gastoso.rest.client;

import static com.github.brunoabdon.gastoso.rest.MediaTypes.APPLICATION_GASTOSO_FULL_TYPE;
import static com.github.brunoabdon.gastoso.rest.MediaTypes.APPLICATION_GASTOSO_PATCH_TYPE;
import static com.github.brunoabdon.gastoso.rest.MediaTypes.APPLICATION_GASTOSO_SIMPLES_TYPE;
import static java.time.format.DateTimeFormatter.ISO_DATE;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.github.brunoabdon.commons.rest.AbstractRestClient;
import com.github.brunoabdon.commons.rest.RESTResponseException;
import com.github.brunoabdon.gastoso.Conta;
import com.github.brunoabdon.gastoso.Fato;
import com.github.brunoabdon.gastoso.Lancamento;
import com.github.brunoabdon.gastoso.ext.FatoDetalhado;
import com.github.brunoabdon.gastoso.ext.Saldo;
import com.github.brunoabdon.gastoso.ext.system.FiltroFatosDetalhados;
import com.github.brunoabdon.gastoso.ext.system.FiltroSaldos;
import com.github.brunoabdon.gastoso.ext.system.GastosoSystemExtended;
import com.github.brunoabdon.gastoso.system.FiltroContas;
import com.github.brunoabdon.gastoso.system.FiltroFatos;
import com.github.brunoabdon.gastoso.system.FiltroLancamentos;
import com.github.brunoabdon.gastoso.system.GastosoSystemException;
import com.github.brunoabdon.gastoso.system.NotFoundException;

import pl.touk.throwing.ThrowingFunction;



/**
 *
 * @author Bruno Abdon
 */
public class GastosoRestClient 
        extends AbstractRestClient<GastosoSystemException> 
        implements GastosoSystemExtended {

    private static final Logger LOG = 
        Logger.getLogger(GastosoRestClient.class.getName());
    
    private static final GenericType<List<Lancamento>> LANCAMENTO_GEN_TYPE = 
        new GenericType<List<Lancamento>>(){};

    private static final GenericType<List<Fato>> FATO_GEN_TYPE = 
        new GenericType<List<Fato>>(){};
    
    private static final GenericType<List<FatoDetalhado>> FATODEL_GEN_TYPE = 
        new GenericType<List<FatoDetalhado>>(){};
    
    private static final GenericType<List<Conta>> CONTA_GEN_TYPE = 
        new GenericType<List<Conta>>(){};
    
    private static final GenericType<List<Saldo>> SALDO_GEN_TYPE = 
        new GenericType<List<Saldo>>(){};

    private static ThrowingFunction<
                        RESTResponseException,
                        Response,
                        GastosoSystemException> EXCEPTION_DEALER =
        e -> {
            if(e.getStatusInfo().getStatusCode() 
                == Response.Status.NOT_FOUND.getStatusCode()){
                throw new NotFoundException(e);
            }
            throw new GastosoSystemException(e);
        };
    
    private final WebTarget rootWebTarget;
    
    private final WebTarget contaWebTarget, contasWebTarget;
    private final WebTarget fatoWebTarget, fatosWebTarget;
    private final WebTarget lancamentoWebTarget, lancamentosWebTarget;
    private final WebTarget saldoWebTarget, saldosWebTarget;

    private String authToken = null;

    private final ClientRequestFilter authFilter = 
        (reqContx) -> reqContx.getHeaders().add("X-Abd-auth_token", authToken);

    private final Consumer<ClientBuilder> configurator = 
        cb -> {
            cb.register(authFilter)
            ;
        }
    ;
    
    public GastosoRestClient(final String serverUri) throws URISyntaxException {
        this(new URI(serverUri));
    }

    public GastosoRestClient(final URI serverUri) {
        super(
            APPLICATION_GASTOSO_PATCH_TYPE, 
            APPLICATION_GASTOSO_FULL_TYPE, 
            EXCEPTION_DEALER);

        this.rootWebTarget = 
            super.start(serverUri, "gastoso-cli", configurator);
        
        this.contasWebTarget = rootWebTarget.path("contas");
        this.fatosWebTarget = rootWebTarget.path("fatos");
        this.lancamentosWebTarget = rootWebTarget.path("lancamentos");
        this.saldosWebTarget = rootWebTarget.path("saldos");

        this.contaWebTarget = this.contasWebTarget.path("{id}");
        this.fatoWebTarget = this.fatosWebTarget.path("{id}");
        this.lancamentoWebTarget = this.lancamentosWebTarget.path("{id}");
        this.saldoWebTarget = this.saldosWebTarget.path("{id}");

        //carregando o cache
        try {
            ggetContas(null);
        } catch (GastosoSystemException ex) {
            LOG.log(Level.WARNING, "Vair dar problema....", ex);
        }
    } 
    
    /**
     * Loga no serviço, pra pegar um token de acesso. 
     * @param user Ignorado 
     * @param password A senha atual.
     * @throws RESTResponseException Se a requisição não terminar em sucesso.
     * @return {@code true} se a requisição retornou um token.
     */
    public boolean login (final String user, final String password) 
            throws RESTResponseException {

        if(this.authToken != null) throw new IllegalStateException();

        final Invocation invocation =
            this.rootWebTarget.path("login")
            .request(MediaType.APPLICATION_JSON_TYPE)
            .buildPost(Entity.text(password));
            
        try {
            
            final Response response = invoke(invocation);

            if(response.getStatusInfo().getFamily() 
                == Response.Status.Family.SUCCESSFUL){

                this.authToken = 
                    readEntity(response, AuthToken.class).token;
            }
        } catch (RESTResponseException e){
            if(e.getStatusInfo().getStatusCode() 
                != Response.Status.UNAUTHORIZED.getStatusCode()){
                throw e;
            }
        }
        return this.authToken != null;
    }

    public boolean logout()
            throws IllegalStateException {

        this.authToken = null;

        return true;
    }

    @Override
    public List<Fato> getFatos(final FiltroFatos filtro) 
            throws GastosoSystemException {
        
        return get(
            FILL_PARAM_FATOS, 
            FATO_GEN_TYPE,
            APPLICATION_GASTOSO_SIMPLES_TYPE,
            fatosWebTarget, 
            filtro);
    }

    @Override
    public List<FatoDetalhado> getFatosDetalhados(
        final FiltroFatosDetalhados filtro) 
            throws GastosoSystemException {
        
        return get(
            FILL_PARAM_FATOS, 
            FATODEL_GEN_TYPE,
            APPLICATION_GASTOSO_SIMPLES_TYPE,
            fatosWebTarget, 
            filtro);
    }
    
    @Override
    public List<Conta> getContas(final FiltroContas filtro) 
            throws GastosoSystemException {
        return ggetContas(filtro);
    }

    
    /**
     * Can't be overriden. Can be called in the constructor.
     * @param filtro
     * @return
     * @throws GastosoSystemException 
     */
    private List<Conta> ggetContas(final FiltroContas filtro) 
            throws GastosoSystemException {
        
        return 
            get((wt,f) -> wt, 
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
            APPLICATION_GASTOSO_SIMPLES_TYPE,
            lancamentosWebTarget, 
            filtro);
    }
    
    @Override
    public List<Saldo> getSaldos(final FiltroSaldos filtro) 
            throws GastosoSystemException {
        
        return get(
            FILL_PARAM_SALDOS, 
            SALDO_GEN_TYPE,
            APPLICATION_GASTOSO_FULL_TYPE,
            saldosWebTarget, 
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
        final WebTarget webTarget, 
        final String paramName, 
        final LocalDate date){

        return date != null 
            ? webTarget.queryParam(paramName, date.format(ISO_DATE))
            : webTarget;
    }

    private static final BiFunction<WebTarget,FiltroSaldos,WebTarget>
        FILL_PARAM_SALDOS = 
        
        (saldosWebTarget,filtro) -> {
            return saldosWebTarget
                .queryParam("dia", filtro.getDia().format(ISO_DATE));
        };
    
    private static final BiFunction<WebTarget, FiltroLancamentos, WebTarget> 
        FILL_PARAM_LANCAMENTOS =

        (lancamentosWebTarget,filtro) -> {

            final FiltroFatos filtroFatos = filtro.getFiltroFatos();

            //"In case a single null value is entered, all parameters with that
            //name are removed..."
            return 
                lancamentosWebTarget
                    .queryParam("fato", filtroFatos.getId())
                    .queryParam("conta", filtro.getFiltroContas().getId())
                    .queryParam("dataMin", filtroFatos.getDataMinima())
                    .queryParam("dataMax", filtroFatos.getDataMaxima());
    };

    @Override
    public Fato getFato(final int id) throws GastosoSystemException {
        return get(fatoWebTarget,Fato.class,id);
    }

    @Override
    public FatoDetalhado getFatoDetalhado(final int id)
            throws GastosoSystemException {
        return get(fatoWebTarget,FatoDetalhado.class,id);
    }
    
    @Override
    public Conta getConta(final int id) throws GastosoSystemException {
        return get(contaWebTarget,Conta.class,id);
    }

    @Override
    public Saldo getSaldo(final int id) throws GastosoSystemException {
        return get(saldoWebTarget, Saldo.class, id);
    }
    
    @Override
    public Fato update(final Fato fato) throws GastosoSystemException {
        return update(fatoWebTarget, fato, Fato.class);
    }

    @Override
    public Conta update(final Conta conta) throws GastosoSystemException {
        return update(contaWebTarget, conta, Conta.class);
    }
    
    @Override
    public Lancamento update(final Lancamento lancamento) 
            throws GastosoSystemException {
        return update(lancamentoWebTarget,lancamento,Lancamento.class);
    }

    @Override
    public void deleteFato(final int id) throws GastosoSystemException {
        delete(fatoWebTarget, id);
    }

    @Override
    public void deleteConta(final int id) throws GastosoSystemException {
        delete(contaWebTarget, id);
    }
    
    @Override
    public void deleteLancamento(final int id) throws GastosoSystemException {
        delete(lancamentoWebTarget, id);
    }

    @Override
    public Fato create(final Fato fato) throws GastosoSystemException { 
        return create(fatosWebTarget,fato,fato.getClass());
    }

    @Override
    public Conta create(final Conta conta) 
            throws GastosoSystemException {
        return create(contasWebTarget,conta,Conta.class);
    }
    
    @Override
    public Lancamento create(final Lancamento lancamento) 
            throws GastosoSystemException {
        return create(lancamentosWebTarget,lancamento,Lancamento.class);
    }
}