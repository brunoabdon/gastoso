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
import java.io.Writer;
import java.time.LocalDate;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;

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
import br.nom.abdon.gastoso.system.GastosoSystem;

/**
 *
 * @author Bruno Abdon
 */
public class GastosoCharacterCommand {

    private final Writer writer;
    private final GastosoSystem gastosoSystem;

    public GastosoCharacterCommand(
            final GastosoSystem gastosoSystem, 
            final Writer writer) {
        this.gastosoSystem = gastosoSystem;
        this.writer = writer;
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

            final LineCommandContext lineCommandCtx = 
                commandCtx.lineCommand();
            
            commandLineCommand(lineCommandCtx);
    
        }
    }
    private void commandLineCommand(
        final LineCommandContext  lineCommandCtx){

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
    
    
    private void commandFato(FatoContext ctx) {
        
        final FatoArgsContext fatoArgsCtx = ctx.fatoArgs();
        
        if(fatoArgsCtx instanceof FatoIdContext){

            final FatoIdContext fatoIdCtx = 
                (FatoIdContext) fatoArgsCtx;
            
            final int fatoId = CtxReader.extractId(fatoIdCtx.id());
            System.out.printf("Exibir fato %d\n",fatoId);
        
        } else if (fatoArgsCtx instanceof FatoSubIdContext){

            final FatoSubIdContext fatoSubIdCtx = (FatoSubIdContext) fatoArgsCtx;
            
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
                int valor = CtxReader.extract(valorCtx);
                
                System.out.printf(
                    "Setar valor do lancamento do fato = %d e conta %d pra %d\n",
                        idFato,
                        idConta,
                        valor);
            }
        } else if(fatoArgsCtx instanceof MkFatoContext){
            
            final MkFatoContext mkFatoCtx =  (MkFatoContext) fatoArgsCtx;
            
            final String descricao = CtxReader.extractText(mkFatoCtx.textArg());
            
            final LocalDate dia = CtxReader.extractDia(mkFatoCtx.dia());
            
            System.out.println(
                "Criar fato '" 
                + descricao 
                + "' no dia " 
                + dia.format(java.time.format.DateTimeFormatter.ISO_DATE));
        }
    }

    private void commandFatos(FatosContext ctx) {
        System.out.println("Listar fatos do periodo setado");
    }

    private void commandContas(ContasContext contasCtx) {
        final ContasArgsContext contaArgsCtx = 
            contasCtx.contasArgs();
        
        if(contaArgsCtx != null){
            
            final String filtro = CtxReader.extractText(contaArgsCtx.textArg());
            
            System.out.println("Listar contas, filtrando por \""+ filtro +"\"");
        } else {
            System.out.println("Listar todas as contas");
        }
    }

    private void commandConta(ContaContext contaCtx) {
        final ContaArgsContext contaArgsCtx = contaCtx.contaArgs();
        if(contaArgsCtx != null){
            final int id = CtxReader.extractId(contaArgsCtx.id());
            final TextArgContext textArgCtx = contaArgsCtx.textArg();
            
            if(textArgCtx != null){
                final String nomeConta = CtxReader.extractText(textArgCtx);
                System.out.printf(
                    "Setar o nome da conta de id %3d pra \"%s\"\n",
                    id,
                    nomeConta);
            } else {
                System.out.printf("Exibir conta de id %3d\n",id);
            }
        }
    }

    private void commandRm(final RmContext rmContext){
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
                System.out.printf("remover conta %4d\n",id);
            } else {
                System.out.printf("remover fato %4d\n",id);
            }
        }
    }
    
    private void commandPeriodo(PeriodoContext ctx){
        final PeriodoDefContext periodoDefCtx = 
            ctx.periodoDef();
        
        final Periodo periodo;
        
        if(periodoDefCtx == null){
            System.out.println("Dizer qual o periodo setado");
        } else {
            periodo = CtxReader.extract(periodoDefCtx);
            
            System.out.println(
                "setando periodo para entre " 
                    + periodo.getDataMinima()
                    + " e "
                    + periodo.getDataMaxima());
        }
    }

}