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
package br.nom.abdon.gastoso.cli;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import static java.time.format.DateTimeFormatter.ISO_DATE;
import java.time.temporal.TemporalAdjusters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import pl.touk.throwing.ThrowingUnaryOperator;

import br.nom.abdon.gastoso.Conta;
import br.nom.abdon.gastoso.Fato;
import br.nom.abdon.gastoso.Lancamento;

import br.nom.abdon.gastoso.cli.parser.GastosoCliLexer;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.CommandContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.ContaContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.IdContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.LineCommandContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.PeriodoContext;

import br.nom.abdon.gastoso.ext.FatoDetalhado;
import br.nom.abdon.gastoso.ext.Saldo;
import br.nom.abdon.gastoso.ext.system.FiltroSaldos;
import static br.nom.abdon.gastoso.ext.system.FiltroSaldos.ORDEM.POR_CONTA;
import br.nom.abdon.gastoso.ext.system.GastosoSystemExtended;

import br.nom.abdon.gastoso.system.FiltroFatos;
import br.nom.abdon.gastoso.system.FiltroLancamentos;

import br.nom.abdon.gastoso.system.GastosoSystem;
import br.nom.abdon.gastoso.system.GastosoSystemException;
import br.nom.abdon.gastoso.system.GastosoSystemRTException;
import br.nom.abdon.gastoso.system.NotFoundException;

import br.nom.abdon.util.Periodo;


/**
 *
 * @author Bruno Abdon
 */
public class GastosoCharacterCommand {

    private static final Logger log  = 
        Logger.getLogger(GastosoCharacterCommand.class.getName());

    private static final String NO_FATO_NO_CONTEXTO = "Nenhum fato no contexto";
    private static final String FATO_NO_CONTEXTO = "Já existe um fato";
    
    private final PrintWriter writer;
    private final GastosoSystem gastosoSystem;
    private final GastosoSystemExtended gastosoSystemExt;
    
    private Periodo periodo;

    private Fato fato;
    private Map<Integer,Lancamento> mapContaLancamento = new HashMap<>();

    private final Map<String,Integer> nicks = new HashMap();
    
    private final boolean extSupported;
    
    public GastosoCharacterCommand(
            final GastosoSystem gastosoSystem, 
            final Writer writer) {

        this.gastosoSystem = gastosoSystem;
        this.writer = new PrintWriter(writer);
        this.periodo = essaSemana();
        
        this.gastosoSystemExt = 
            (this.extSupported = gastosoSystem instanceof GastosoSystemExtended)
                ? (GastosoSystemExtended) gastosoSystem
                : null;
    }

    public boolean command(final String commandLine) throws IOException{

        boolean ok;

        final GastosoCliParser.CommandContext commandCtx;

        final CharStream cs = new ANTLRInputStream(commandLine);

        final GastosoCliLexer hl = new GastosoCliLexer(cs);

        final CommonTokenStream tokens = new CommonTokenStream(hl);
        
        final GastosoCliParser parser = new GastosoCliParser(tokens);

        parser.setErrorHandler(new BailErrorStrategy());
        parser.removeErrorListeners();

        try {
            commandCtx = parser.command();  //reusavel?
            processCommand(commandCtx);
            ok = true;

        } catch (final ParseCancellationException e){
            writer.append("Não entendi.\n");
            ok = false;
        }

        return ok;
    }

    private void processCommand(CommandContext commandCtx){

        if(commandCtx.exception == null){

            final GastosoCliParser.LineCommandContext lineCommandCtx = 
                commandCtx.lineCommand();

            try {
                commandLineCommand(lineCommandCtx);
            } catch (CLIException ex){
                writer.printf("Opa: %s\n",ex.getMessage());
            } catch (NotFoundException ex){
                writer.println("Não existe");
            } catch (GastosoSystemException | GastosoSystemRTException ex) {
                final String msg = 
                    (ex instanceof GastosoSystemException)
                        ? "Erro" : "Problema";

                writer.println(msg + ": " + ex.getMessage());
                log.log(Level.FINEST, ex, () -> "Impossível processar.");
            }
        }
    }
    private void commandLineCommand(final LineCommandContext lineCommandCtx) 
            throws GastosoSystemException, CLIException{

        if(lineCommandCtx.CD() != null){
            
            if(lineCommandCtx.PARENT() != null){
                dispensaFato();
            } else {
                final int id = CtxReader.extractId(lineCommandCtx.id());
                
                this.fato = gastosoSystem.getFato(id);
                this.mapContaLancamento = new HashMap<>();
                
                final List<Lancamento> lancamentos;
                
                if(this.fato instanceof FatoDetalhado){
                    lancamentos = ((FatoDetalhado)this.fato).getLancamentos();
                    
                } else {
                    final FiltroLancamentos filtrol = new FiltroLancamentos();
                    filtrol.getFiltroFatos().setId(id);
                
                    lancamentos = gastosoSystem.getLancamentos(filtrol);
                }
                
                lancamentos.forEach(
                    l -> this.mapContaLancamento.put(l.getConta().getId(),l)
                );
                
                printFato();
            }
        
        } else if (lineCommandCtx.MKC() != null){
            final String nome = CtxReader.extractText(lineCommandCtx.textArg());
            Conta conta = new Conta(nome);
            conta = this.gastosoSystem.create(conta);
            writer.printf(
                "Conta criada %d - %s\n",
                conta.getId(),
                conta.getNome());

        } else if (lineCommandCtx.RMC() != null){
            final ContaContext contaCtx = lineCommandCtx.conta(0);
            final Integer id = extractContaId(contaCtx);
            gastosoSystem.deleteConta(id);
            writer.println("Conta deletada");
            
        } else if (lineCommandCtx.MVC() != null){
            final int id = CtxReader.extractId(lineCommandCtx.id());
            final String nome = CtxReader.extractText(lineCommandCtx.textArg());
            
            Conta conta = new Conta(id, nome);
            
            conta = gastosoSystem.update(conta);
            writer.printf(
                "Conta atualizada %d - %s\n",
                conta.getId(),
                conta.getNome());
        
        } else if(lineCommandCtx.LS() != null){

            if(lineCommandCtx.CONTAS() != null){
                if(extSupported){
                    final FiltroSaldos filtroSaldos = new FiltroSaldos();
                    filtroSaldos.setDia(LocalDate.now());
                    filtroSaldos.addOrdem(POR_CONTA);

                    final List<Saldo> saldos = 
                        gastosoSystemExt.getSaldos(filtroSaldos);

                    writer.printf("Saldos em %s.\n\n",
                        filtroSaldos.getDia().format(ISO_DATE));
                    
                    saldos.forEach(
                        s -> writer.printf(
                            "%s\t%s\n",
                            formata(s.getConta()),
                            formata(s.getValor())));
                    
                } else {
                    Collection<Conta> contas = gastosoSystem.getContas(null);

                    contas.forEach(c -> writer.println(formata(c)));
                    
                }
            } else {
                final Periodo periodo = 
                    CtxReader.extract(lineCommandCtx.periodo(),this.periodo);
                
                final ContaContext contaCtx = lineCommandCtx.conta(0);
                if(contaCtx != null){
                    
                    final Integer idConta = extractContaId(contaCtx);
                        
                    final FiltroLancamentos f = new FiltroLancamentos();

                    final FiltroFatos filtroFatos = f.getFiltroFatos();
                    filtroFatos.setDataMinima(periodo.getDataMinima());
                    filtroFatos.setDataMaxima(periodo.getDataMaxima());

                    f.getFiltroContas().setId(idConta);

                    final List<Lancamento> lancamentos = 
                        gastosoSystem.getLancamentos(f);

                    //usar cache...
                    final Conta conta = gastosoSystem.getConta(idConta);

                    writer.printf("\n%s\n\n",formata(conta));

                    lancamentos.forEach(
                        l -> writer.printf("%s - %s \t\t%s\n",
                                l.getFato().getDia(),
                                l.getFato().getDescricao(),
                                    formata(l.getValor()))
                    );
                } else {
                    final FiltroFatos f = new FiltroFatos();
                    f.setDataMinima(periodo.getDataMinima());
                    f.setDataMaxima(periodo.getDataMaxima());
                    f.addOrdem(FiltroFatos.ORDEM.POR_DIA);

                    final List<? extends Fato> fatos = gastosoSystem.getFatos(f);
                    fatos.forEach(this::printFato);
                }
            }
        } else if (lineCommandCtx.MKF() != null){
            
            assertNoFato();
            
            this.fato = 
                new Fato( //dia (default hoje), descricao
                    CtxReader.extractDia(lineCommandCtx.dia(), LocalDate::now),
                    CtxReader.extractText(lineCommandCtx.textArg()));
            
            this.mapContaLancamento = new HashMap<>();
            
            final ContaContext conta0Ctx = lineCommandCtx.conta(0);
            if(conta0Ctx != null){

                final Integer idConta0 = extractContaId(conta0Ctx);
                final Conta conta0 = new Conta(idConta0);
                int valor = CtxReader.extract(lineCommandCtx.valor());

                final ContaContext conta1Ctx = lineCommandCtx.conta(1);
                if(conta1Ctx != null){

                    final Integer idConta1 = extractContaId(conta1Ctx);
                    final Conta conta1 = new Conta(idConta1);
                    final Lancamento saida = 
                        new Lancamento(null, conta0, -valor);
                    final Lancamento entrada = 
                        new Lancamento(null, conta1, valor);

                    mapContaLancamento.put(idConta0, entrada);
                    mapContaLancamento.put(idConta1, saida);

                } else {
                    Lancamento lanc = new Lancamento(null, conta0, -valor);
                    mapContaLancamento.put(idConta0, lanc);
                }
            }
            printFato();
        } else if (lineCommandCtx.ENTRADA() != null 
                || lineCommandCtx.SAIDA() != null){
            
            assertHasFato();
            
            final Integer idConta = extractContaId(lineCommandCtx.conta(0));
            final Conta conta = new Conta(idConta);
            final int valor = 
                CtxReader.extract(lineCommandCtx.valor())
                * ((lineCommandCtx.ENTRADA() != null) ? 1:-1);
            final Lancamento lancamento  = new Lancamento(this.fato, conta, valor);
            mapContaLancamento.put(idConta, lancamento);

            printFato();

        } else if(lineCommandCtx.NULL() != null){
            
            assertHasFato();
            
            mapContaLancamento.remove(extractContaId(lineCommandCtx.conta(0)));
            
            printFato();

        } else if(lineCommandCtx.DIA() != null){
            assertHasFato();

            this.fato.setDia(
                    CtxReader.extractDia(lineCommandCtx.dia()));

            printFato();

        } else if(lineCommandCtx.DESC() != null){
            assertHasFato();

            this.fato.setDescricao(
                    CtxReader.extractText(lineCommandCtx.textArg()));

            printFato();
        } else if(lineCommandCtx.CANCEL() != null){
            
            dispensaFato();
            
        } else if (lineCommandCtx.RMF() != null){
            final Integer id = CtxReader.extractId(lineCommandCtx.id());
            gastosoSystem.deleteFato(id);
            writer.println("Fato deletado");
        } else if(lineCommandCtx.SAVE() != null){
            
            assertHasFato();
            
            final ThrowingUnaryOperator<Fato,GastosoSystemException> op;
            
            final String verbo;
            if(this.fato.getId() == null){
                op = gastosoSystem::create;
                verbo = "criado"; //vai ser f... internacionalizar isso...
            } else {
                op = gastosoSystem::update;
                verbo = "atualizado";
            }
            
            final FatoDetalhado fatoDetalhado =
                new FatoDetalhado(
                    this.fato,
                    new ArrayList<>(this.mapContaLancamento.values()));

            this.fato = null;
            this.mapContaLancamento = null;
            
            final Fato fatoAtualizado = op.apply(fatoDetalhado);
            
            writer.println(
                String.format("Fato %s: %s", verbo,formata(fatoAtualizado)));
            
        } else if(lineCommandCtx.LN() != null){
            final int id = CtxReader.extractId(lineCommandCtx.id());
            
            final Conta conta = gastosoSystem.getConta(id);
            
            final String nick = lineCommandCtx.WORD().getText();
            nicks.put(nick, id);
            writer.printf(
                    "\"%s\" setado como apelido de \"%s\".\n", 
                    nick, 
                    conta.getNome());
            
        } else if(lineCommandCtx.RML() != null){
            final String nick = lineCommandCtx.WORD().getText();
            final Integer id  = nicks.remove(nick);
            writer.printf(
                    id == null 
                        ? "Não sei que é \"%s\".\n"
                        : "Apelido \"%s\" removido\n.", 
                    nick);
            
        } else if (lineCommandCtx.PERIODO() != null){
            final PeriodoContext periodoCtx = lineCommandCtx.periodo();
            
            if(periodoCtx != null) this.periodo = CtxReader.extract(periodoCtx);
            
            printPeriodo();
        
        } else if(lineCommandCtx.MVF() != null){

            final LocalDate dia = 
                CtxReader.extractDia(lineCommandCtx.dia(),(LocalDate)null);
            
            final String desc = 
                CtxReader.extractText(lineCommandCtx.textArg(),(String)null);
            
            if(dia == null && desc == null){
                throw new CLIException("Diga um dia ou uma descrição.");
            }

            final int id = CtxReader.extractId(lineCommandCtx.id());
            
            Fato fato = gastosoSystem.getFato(id);

            fato.setDia(dia);
            fato.setDescricao(desc);
            
            fato = gastosoSystem.update(fato);

            printFato(fato);
        }
    }

    private void dispensaFato() throws CLIException {
        assertHasFato();
        
        final Fato fatoCancelado = this.fato;
        
        this.fato = null;
        this.mapContaLancamento = null;
        
        writer.printf("%s de \"%s\" cancelada.\n",
                fatoCancelado.getId() != null? "Edição":"Criação",
                fatoCancelado.getDescricao()
        );
    }

    private void printFato(final Fato fato) {
        
        final Integer fatoId = fato.getId();
        
        writer.println(formata(fato));
        
        if(fato instanceof FatoDetalhado){
            final List<Lancamento> lancamentos =
                    ((FatoDetalhado)fato).getLancamentos();

            if(lancamentos == null){
                writer.println("<sem lancamentos>");
            } else {
                lancamentos.forEach(
                        l -> {
                            writer.printf(
                                "  [%d/%d] - %s %s\n", 
                                fatoId,
                                l.getConta().getId(),
                                l.getConta().getNome(),
                                formata(l.getValor())
                            );
                        }
                );
            }
        }
    }

    private static String formata(final Fato fato) {
        return fato.getId() 
                + " - " + fato.getDia().format(DateTimeFormatter.ISO_DATE) 
                + " - " + fato.getDescricao();
    }
    
    private static String formata(final Conta conta){
        return String.format("%2d - %s",conta.getId(),conta.getNome());
    }
    

    private void printPeriodo() {
        this.printPeriodo(this.periodo);
    }
    private void printPeriodo(final Periodo periodo) {
        writer.printf(
            "De %s a %s\n",
            periodo
            .getDataMinima()
            .format(java.time.format.DateTimeFormatter.ISO_DATE),

            periodo
            .getDataMaxima()
            .format(java.time.format.DateTimeFormatter.ISO_DATE)
        );
    }

    private Periodo essaSemana() {
        final LocalDate domingoPassado =
                LocalDate
                    .now()
                    .with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));

        final LocalDate proximoSabado =
                domingoPassado
                .with(TemporalAdjusters.next(DayOfWeek.SATURDAY));

        return new Periodo(domingoPassado, proximoSabado);
    }
    
    private String formata(final Integer val){
        return formata((long)val);
    }

    private String formata(final Long val){
        final BigDecimal valor = BigDecimal.valueOf(val, 2);
        return NumberFormat
                .getCurrencyInstance(Locale.forLanguageTag("pt-BR"))
                .format(valor);
    }

    private void printFato() {
        printFato(this.fato);
    }

    private Integer extractContaId(final ContaContext contaCtx) 
            throws CLIException {
        
        final Integer id;
        
        final IdContext idContext = contaCtx.id();

        if(idContext != null){
            id = CtxReader.extractId(idContext);
        } else{
            final String nick = contaCtx.WORD().getText();
            id = nicks.get(nick);
            if(id == null){
                throw new CLIException("Não sei o que é '" + nick + "'.");
            }
        }
        return id;
    }

    private void assertHasFato() throws CLIException{
        assertFato(true,NO_FATO_NO_CONTEXTO);
    }
    
    private void assertNoFato() throws CLIException{
        assertFato(false,FATO_NO_CONTEXTO);
    }
    
    private void assertFato(
            final boolean deveTerFato,
            final String errMsg) 
            throws CLIException{
        
        if((this.fato == null) == deveTerFato) throw new CLIException(errMsg);
    }   
}