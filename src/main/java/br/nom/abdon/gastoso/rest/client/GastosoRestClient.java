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

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import static java.time.format.DateTimeFormatter.ISO_DATE;
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

import pl.touk.throwing.ThrowingFunction;

import br.nom.abdon.gastoso.Conta;
import br.nom.abdon.gastoso.Fato;
import br.nom.abdon.gastoso.Lancamento;

import br.nom.abdon.gastoso.ext.FatoDetalhado;
import br.nom.abdon.gastoso.ext.Saldo;
import br.nom.abdon.gastoso.ext.system.FiltroFatosDetalhados;
import br.nom.abdon.gastoso.ext.system.FiltroSaldos;
import br.nom.abdon.gastoso.ext.system.GastosoSystemExtended;

import br.nom.abdon.gastoso.rest.MediaTypes;
import static br.nom.abdon.gastoso.rest.MediaTypes.APPLICATION_GASTOSO_FULL_TYPE;
import static br.nom.abdon.gastoso.rest.MediaTypes.APPLICATION_GASTOSO_PATCH_TYPE;
import static br.nom.abdon.gastoso.rest.MediaTypes.APPLICATION_GASTOSO_SIMPLES_TYPE;
import br.nom.abdon.gastoso.rest.serial.ContasCacheReaderInterceptor;
import br.nom.abdon.gastoso.rest.serial.FatosMessageBodyReader;
import br.nom.abdon.gastoso.rest.serial.GastosoMessageBodyReader;
import br.nom.abdon.gastoso.rest.serial.GastosoMessageBodyWriter;
import br.nom.abdon.gastoso.rest.serial.LancamentosMessageBodyReader;
import br.nom.abdon.gastoso.rest.serial.SaldosMessageBodyReader;

import br.nom.abdon.gastoso.system.FiltroContas;
import br.nom.abdon.gastoso.system.FiltroFatos;
import br.nom.abdon.gastoso.system.FiltroLancamentos;
import br.nom.abdon.gastoso.system.GastosoSystemException;
import br.nom.abdon.gastoso.system.NotFoundException;



/**
 *
 * @author Bruno Abdon
 */
public class GastosoRestClient extends AbstractRestClient<GastosoSystemException> 
        implements GastosoSystemExtended {

    private static final Logger log = 
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

    private final ContasCacheReaderInterceptor readerInterceptor =
        new ContasCacheReaderInterceptor();
    
    private final Consumer<ClientBuilder> configurator = 
        cb -> {
            cb.register(GastosoMessageBodyReader.class)
            .register(GastosoMessageBodyWriter.class)
            .register(FatosMessageBodyReader.class)
            .register(LancamentosMessageBodyReader.class)
            .register(SaldosMessageBodyReader.class)
            .register(authFilter)
            .register(readerInterceptor)
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
            log.log(Level.WARNING, "Vair dar problema....", ex);
        }
    } 
    
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
    private final List<Conta> ggetContas(final FiltroContas filtro) 
            throws GastosoSystemException {
        
        final List<Conta> contas =
            get((wt,f) -> wt, 
                CONTA_GEN_TYPE,
                APPLICATION_GASTOSO_FULL_TYPE,
                contasWebTarget, 
                filtro);
        readerInterceptor.updateContas(contas);
        return contas;
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
    public Fato getFato(int id)
            throws GastosoSystemException {
        return get(fatoWebTarget,Fato.class,id);
    }

    @Override
    public FatoDetalhado getFatoDetalhado(int id) 
            throws GastosoSystemException {
        return get(fatoWebTarget,FatoDetalhado.class,id);
    }
    
    @Override
    public Conta getConta(int id) throws GastosoSystemException {
        final Conta conta = get(contaWebTarget,Conta.class,id);
        readerInterceptor.updateConta(conta);
        return conta;
    }

    @Override
    public Saldo getSaldo(int id) throws GastosoSystemException {
        return get(saldoWebTarget, Saldo.class, id);
    }

    
    @Override
    public Fato update(Fato fato) throws GastosoSystemException {
        return update(fatoWebTarget, fato, Fato.class);
    }

    @Override
    public Conta update(Conta conta) throws GastosoSystemException {
        final Conta updatedConta = update(contaWebTarget, conta, Conta.class);
        readerInterceptor.updateConta(conta);
        return updatedConta;
    }
    
    @Override
    public Lancamento update(final Lancamento lancamento) 
            throws GastosoSystemException {
        return update(lancamentoWebTarget,lancamento,Lancamento.class);
    }

    @Override
    public void deleteFato(int id) throws GastosoSystemException {
        delete(fatoWebTarget, id);
    }

    @Override
    public void deleteConta(int id) throws GastosoSystemException {
        delete(contaWebTarget, id);
        readerInterceptor.removeConta(id);
    }
    
    @Override
    public void deleteLancamento(int id) throws GastosoSystemException {
        delete(lancamentoWebTarget, id);
    }

    @Override
    public Fato create(final Fato fato) throws GastosoSystemException { 
        return create(fatosWebTarget,fato,fato.getClass());
    }

    @Override
    public Conta create(final Conta conta) 
            throws GastosoSystemException {
        final Conta novaConta = create(contasWebTarget,conta,Conta.class);
        readerInterceptor.updateConta(conta);
        return novaConta;
    }
    
    @Override
    public Lancamento create(final Lancamento lancamento) 
            throws GastosoSystemException {
        return create(lancamentosWebTarget,lancamento,Lancamento.class);
    }
}