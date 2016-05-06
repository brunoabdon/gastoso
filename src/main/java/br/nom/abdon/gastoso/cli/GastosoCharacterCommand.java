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
import java.time.temporal.TemporalAdjusters;

import static java.time.format.DateTimeFormatter.ISO_DATE;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import br.nom.abdon.gastoso.Conta;
import br.nom.abdon.gastoso.Fato;
import br.nom.abdon.gastoso.Lancamento;

import br.nom.abdon.gastoso.cli.parser.GastosoCliLexer;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.CommandContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.ContaArgsContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.ContaContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.ContasArgsContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.ContasContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.FatoArgsContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.FatoContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.FatoIdContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.FatoSubIdContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.FatosContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.LineCommandContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.MkFatoContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.PeriodoContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.PeriodoDefContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.RmArgsContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.RmContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.SubIdContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.TextArgContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.ValorContext;
import br.nom.abdon.gastoso.rest.FatoDetalhado;
import br.nom.abdon.gastoso.system.FiltroFatos;
import br.nom.abdon.gastoso.system.FiltroLancamentos;

import br.nom.abdon.gastoso.system.GastosoSystem;
import br.nom.abdon.gastoso.system.GastosoSystemException;
import br.nom.abdon.gastoso.system.GastosoSystemRTException;
import static br.nom.abdon.gastoso.system.GastosoSystemRTException.SERVIDOR_DESNORTEADO;
import br.nom.abdon.gastoso.system.NotFoundException;

import br.nom.abdon.util.Periodo;


/**
 *
 * @author Bruno Abdon
 */
public class GastosoCharacterCommand {

    private static final Logger log  = 
        Logger.getLogger(GastosoCharacterCommand.class.getName());
    
    private final PrintWriter writer;
    private final GastosoSystem gastosoSystem;
    private Periodo periodo;

    public GastosoCharacterCommand(
            final GastosoSystem gastosoSystem, 
            final Writer writer) {

        this.gastosoSystem = gastosoSystem;
        this.writer = new PrintWriter(writer);
        this.periodo = essaSemana();
    }

    public boolean command(final String commandLine) throws IOException{

        boolean ok;

        final CommandContext commandCtx;

        final CharStream cs = new ANTLRInputStream(commandLine);

        final GastosoCliLexer hl = new GastosoCliLexer(cs);

        final CommonTokenStream tokens = new CommonTokenStream(hl);
        
        final GastosoCliParser parser = new GastosoCliParser(tokens);

        parser.setErrorHandler(new BailErrorStrategy());
        parser.removeErrorListeners();

        try {
            commandCtx = parser.command();
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

            final LineCommandContext lineCommandCtx = commandCtx.lineCommand();

            try {
                commandLineCommand(lineCommandCtx);
            } catch (NotFoundException ex){
                writer.println("Não existe");
            } catch (GastosoSystemException | GastosoSystemRTException ex) {
                final String msg = 
                    (ex instanceof GastosoSystemException)
                        ? "Erro" 
                        : (ex instanceof GastosoSystemRTException)
                            ? "Problema"
                            : "Bronca";

                writer.println(msg + ": " + ex.getMessage());
                log.log(Level.FINEST, ex, () -> "Impossível processar.");
            }
        }
    }
    private void commandLineCommand(final LineCommandContext lineCommandCtx) 
            throws GastosoSystemRTException, GastosoSystemException{

        final FatosContext fatosCtx = lineCommandCtx.fatos();
        if(fatosCtx != null){
            commandFatos(fatosCtx);
            return;
        }

        final FatoContext fatoCtx = lineCommandCtx.fato();
        if(fatoCtx != null){
            commandFato(fatoCtx);
            return;
        } 

        final PeriodoContext periodoCtx = 
            lineCommandCtx.periodo();
        if(periodoCtx != null){
            commandPeriodo(periodoCtx);
            return;
        }

        final ContasContext contasCtx = 
            lineCommandCtx.contas();
        if(contasCtx != null){
            commandContas(contasCtx);
            return;
        }

        final ContaContext contaCtx = lineCommandCtx.conta();
        if(contaCtx != null){
            commandConta(contaCtx);
            return;
        }

        final RmContext rmContext = lineCommandCtx.rm();
        if(rmContext != null){
            commandRm(rmContext);
            return;
        }
    }

    private void commandFato(final FatoContext ctx) 
            throws GastosoSystemException {

        final FatoArgsContext fatoArgsCtx = ctx.fatoArgs();

        if(fatoArgsCtx instanceof FatoIdContext){

            final FatoIdContext fatoIdCtx = (FatoIdContext) fatoArgsCtx;

            final int fatoId = CtxReader.extractId(fatoIdCtx.id());
            
            final GastosoCliParser.DiaContext diaCtx = fatoIdCtx.dia();
            final TextArgContext textArgCtx = fatoIdCtx.textArg();
            
            final boolean updateDia = diaCtx != null;
            final boolean updateDescricao = textArgCtx != null;

            Fato fato;
            
            if(updateDia || updateDescricao){
                
                fato = new Fato(fatoId);
                        
                if(updateDia) fato.setDia(CtxReader.extractDia(diaCtx));
                
                if(updateDescricao) 
                    fato.setDescricao(CtxReader.extractText(textArgCtx));
                
                fato = gastosoSystem.update(fato);

            } else {
                fato = gastosoSystem.getFato(fatoId);
            }

            printFato(fato);

        } else if (fatoArgsCtx instanceof FatoSubIdContext){

            final FatoSubIdContext fatoSubIdCtx =
                (FatoSubIdContext) fatoArgsCtx;

            final SubIdContext subIdCtx = fatoSubIdCtx.subId();

            int idFato = CtxReader.extractId(subIdCtx.id(0));
            int idConta = CtxReader.extractId(subIdCtx.id(1));

            final ValorContext valorCtx = fatoSubIdCtx.valor();

            if(valorCtx == null){
                System.out.printf(
                    "Exibir o valor do lancamento do fato = %d e conta %d\n",
                        idFato,
                        idConta);
            } else {
                
                final FiltroLancamentos f = new FiltroLancamentos();
                f.getFiltroContas().setId(idConta);
                f.getFiltroFatos().setId(idFato);
                
                final List<? extends Lancamento> lancamentos = 
                    gastosoSystem.getLancamentos(f);
                
                final int quantos = lancamentos.size();
                if(quantos > 1 ){
                    throw new GastosoSystemRTException(
                        "Mais de um lançamento pra mesma conta no mesmo fato!", 
                        SERVIDOR_DESNORTEADO);
                } else {
                    int valor = CtxReader.extract(valorCtx);

                    Lancamento lancamento;

                    if(quantos == 1) {
                        lancamento = lancamentos.get(0);
                        lancamento.setValor(valor);
                        lancamento = gastosoSystem.update(lancamento);
                        
                    } else {
                        lancamento =
                            new Lancamento(
                                new Fato(idFato),
                                new Conta(idConta), 
                                valor);
                    
                        lancamento = gastosoSystem.create(lancamento);
                    }
                    
                    final Fato fato = lancamento.getFato();

                    writer.printf("%s pra %s em [%d]%s.\n\n",
                        formata(lancamento.getValor()),
                        lancamento.getConta().getNome(),
                        fato.getId(),
                        lancamento.getFato().getDescricao()
                    );
                }
            }
        } else if(fatoArgsCtx instanceof MkFatoContext){

            final MkFatoContext mkFatoCtx = (MkFatoContext) fatoArgsCtx;

            final String descricao = CtxReader.extractText(mkFatoCtx.textArg());

            final LocalDate dia = CtxReader.extractDia(mkFatoCtx.dia());

            final Fato fato = gastosoSystem.create(new Fato(dia, descricao));

            writer.println(
                "Fato criado: [" 
                + fato.getId()
                + "] " + fato.getDia().format(ISO_DATE)
                + " - " + fato.getDescricao());
        }
    }

    private void printFato(final Fato fato) {
        
        final Integer fatoId = fato.getId();
        
        writer.flush();
        writer.println(
                fatoId
                + " - "
                + fato
                    .getDia()
                    .format(java.time.format.DateTimeFormatter.ISO_DATE)
                + " - "
                + fato.getDescricao());
        
        if(fato instanceof FatoDetalhado){
            final List<Lancamento> lancamentos =
                    ((FatoDetalhado)fato).getLancamentos();

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

    private void commandFatos(FatosContext ctx) throws GastosoSystemException {
        final FiltroFatos f = new FiltroFatos();
        f.setDataMinima(periodo.getDataMinima());
        f.setDataMaxima(periodo.getDataMaxima());
        f.addOrdem(FiltroFatos.ORDEM.POR_DIA);
        f.addOrdem(FiltroFatos.ORDEM.POR_DIA);
        
        final List<? extends Fato> fatos = gastosoSystem.getFatos(f);
        
        fatos.forEach(this::printFato);
    }

    private void commandContas(final ContasContext contasCtx) 
            throws GastosoSystemException {
        
        final ContasArgsContext contaArgsCtx = 
            contasCtx.contasArgs();

        final List<? extends Conta> contas;
        if(contaArgsCtx != null){

            final String filtro = CtxReader.extractText(contaArgsCtx.textArg());
            //quando tiver algo, implementar aqui...
            contas = gastosoSystem.getContas(null);
            
        } else {
            contas = gastosoSystem.getContas(null);
        }
        
        contas.forEach(
            c -> writer.printf("%d\t%s\n",c.getId(),c.getNome())
        );
    }

    private void commandConta(ContaContext contaCtx) 
            throws GastosoSystemRTException, GastosoSystemException {

        final ContaArgsContext contaArgsCtx = contaCtx.contaArgs();
        final GastosoCliParser.IdContext idContext = contaArgsCtx.id();
        final TextArgContext textArgCtx = contaArgsCtx.textArg();

        final Integer id = 
            idContext != null
                ? CtxReader.extractId(idContext)
                : null;

        final String nome = 
            textArgCtx != null
                ? CtxReader.extractText(textArgCtx)
                : null;

        final boolean ehCriacao = id == null;
        final boolean ehUpdate = !ehCriacao && nome != null;
        
        final Conta conta = 
            ehCriacao
                ? gastosoSystem.create(new Conta(nome))
                : gastosoSystem.getConta(id);

        if(ehUpdate){ // id e nome nao nulos
            conta.setNome(nome);
            gastosoSystem.update(conta);
        }
            
        if(ehCriacao || ehUpdate){
            writer.print("Conta " + (ehUpdate?"atualizada":"criada")+ ": " );
        }

        writer.println(conta.getId() + " - " + conta.getNome());
    }

    private void commandRm(final RmContext rmContext) 
            throws GastosoSystemRTException, GastosoSystemException{
        final RmArgsContext rmArgsCtx = rmContext.rmArgs();

        if(rmArgsCtx.getChild(0).getText().charAt(0) == 'l'){ //lancamento
            //rm lancamento
            final SubIdContext 
                subIdContext = rmArgsCtx.subId();

            int idFato = CtxReader.extractId(subIdContext.id(0));
            int idConta = CtxReader.extractId(subIdContext.id(1));

            System.out.printf(
                "remover lancamento da conta %4d no fato %4d\n",
                idConta,
                idFato
            );

        } else {
           final int id = CtxReader.extractId(rmArgsCtx.id());

            if(rmArgsCtx.getChild(0).getText().charAt(0) == 'c'){ //conta
                gastosoSystem.deleteConta(id);
                writer.println("Conta deletada");
            } else {
                gastosoSystem.deleteFato(id);
                writer.println("Fato deletado");
            }
        }
    }

    private void commandPeriodo(PeriodoContext ctx){
        final PeriodoDefContext periodoDefCtx = ctx.periodoDef();

        if(periodoDefCtx != null){
            this.periodo = CtxReader.extract(periodoDefCtx);
        }
        printPeriodo();
    }

    private void printPeriodo() {
        writer.printf(
            "De %s a %s\n",
            this.periodo
            .getDataMinima()
            .format(java.time.format.DateTimeFormatter.ISO_DATE),

            this.periodo
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
    
    private String formata(Integer val){
        
        BigDecimal valor = BigDecimal.valueOf(val, 2);
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(valor);
//        return String.format("R$ %d,%2d", );
    }
    
}