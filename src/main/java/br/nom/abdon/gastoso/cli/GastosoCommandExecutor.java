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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;

import org.antlr.v4.runtime.tree.TerminalNode;

import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.AnoContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.AnoPorReferenciaContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.AnoSimplesContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.CommandContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.ContaArgsContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.ContaContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.ContasArgsContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.ContasContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.DiaContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.DiaDaSemanaContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.DiaDaSemanaPorReferenciaContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.DiaSimplesContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.FatoArgsContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.FatoContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.FatoIdContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.FatoSubIdContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.FatosContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.IdContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.LineCommandContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.MesContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.MesPorReferenciaContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.MesSimplesContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.MkFatoContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.NomeDePeriodoContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.OutraSemanaContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.OutroPeriodoContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.PeridoComplexoContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.PeriodoContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.PeriodoDefContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.PeriodoReferenciadoContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.PeriodoSemanaContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.PeriodoSimplesContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.RmArgsContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.RmContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.SubIdContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.TextArgContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.ValorContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.VarianteFemContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.VarianteMascContext;
import br.nom.abdon.gastoso.cli.util.DiaHelper;

/**
 *
 * @author Bruno Abdon
 */
public class GastosoCommandExecutor {

    public static enum Variante {PASSADO, QUE_VEM};
    
    private static final TemporalAdjuster COMECO_DO_MES_ADJSTR = 
        TemporalAdjusters.firstDayOfMonth();

    private static final TemporalAdjuster COMECO_DO_ANO_ADJSTR = 
        TemporalAdjusters.firstDayOfYear();
    

    public void processCommand(CommandContext commandCtx) {
        
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
            
            final int fatoId = extractId(fatoIdCtx.id());
            System.out.printf("Exibir fato %d\n",fatoId);
        
        } else if (fatoArgsCtx instanceof FatoSubIdContext){

            final FatoSubIdContext fatoSubIdCtx = 
                (FatoSubIdContext) fatoArgsCtx;
            
            final SubIdContext subIdCtx = 
                fatoSubIdCtx.subId();
            
            final int fatoId = Integer.valueOf(subIdCtx.id(0).getText());
            
            final int contaId = Integer.valueOf(subIdCtx.id(1).getText());
            
            final ValorContext valorCtx = fatoSubIdCtx.valor();
            
            if(valorCtx == null){
                System.out.printf(
                    "Exibir o valor do lancamento do fato = %d e conta %d\n",
                        fatoId,
                        contaId);
            } else {
                
                int valor = (Integer.parseInt(valorCtx.INT().getText()) * 100);
                
                final TerminalNode centavosTerm = valorCtx.CENTAVOS();
                if(centavosTerm != null){
                    valor += 
                        Integer.parseInt(centavosTerm.getText().substring(1));
                }

                if(valorCtx.MENOS() != null) valor *= -1;
                
                System.out.printf(
                    "Setar valor do lancamento do fato = %d e conta %d pra %d\n",
                        fatoId,
                        contaId,
                        valor);
            }
        } else if(fatoArgsCtx instanceof MkFatoContext){
            final MkFatoContext mkFatoCtx = 
                (MkFatoContext) fatoArgsCtx;
            
            final String descricao = mkFatoCtx.textArg().getText();
            
            final DiaContext diaCtx = mkFatoCtx.dia();
            final LocalDate dia = extractDia(diaCtx);
            
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
            
            final String filtro = extractText(contaArgsCtx.textArg());
            
            System.out.println("Listar contas, filtrando por \""+ filtro +"\"");
        } else {
            System.out.println("Listar todas as contas");
        }
    }

    private String extractText(final TextArgContext textArg) {
        String text;
        final TerminalNode textNode = textArg.TEXT();
        if(textNode != null){
            text = textNode.getText();
            text = text.substring(1,text.length()-1);
        } else {
            text = textArg.WORD().getText();
        }
        return text;
    }

    private void commandConta(ContaContext contaCtx) {
        final ContaArgsContext contaArgsCtx = contaCtx.contaArgs();
        if(contaArgsCtx != null){
            final int id = extractId(contaArgsCtx.id());
            final TextArgContext textArgCtx = contaArgsCtx.textArg();
            
            if(textArgCtx != null){
                final String nomeConta = extractText(textArgCtx);
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
            
            int idFato = extractId(subIdContext.id(0));
            int idConta = extractId(subIdContext.id(1));
            
            System.out.printf(
                "remover lancamento da conta %4d no fato %4d\n",
                idConta,
                idFato
            );
            
        } else {

            final int id = extractId(rmArgsCtx.id());

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
            final PeriodoSimplesContext periodoSimplesCtx = 
                periodoDefCtx.periodoSimples();
            periodo = periodoSimplesCtx != null
                ? extractPeriodo(periodoSimplesCtx)
                : extractPeriodo(periodoDefCtx.peridoComplexo());
            
            System.out.println(
                "setando periodo para entre " 
                    + periodo.getDataMinima()
                    + " e "
                    + periodo.getDataMaxima());
            
        }
    }
    

    private LocalDate extractDia(DiaContext diaCtx) {
        final LocalDate dia;
    
        if(diaCtx == null){
            dia = LocalDate.now();
            
        } else {
        
            final DiaSimplesContext diaSimplesCtx = 
                diaCtx.diaSimples();
        
            if(diaSimplesCtx != null){
                dia = extractDia(diaSimplesCtx);
            } else {
                
                final DiaDaSemanaContext diaDaSemanaCtx = 
                    diaCtx.diaDaSemana();

                if(diaDaSemanaCtx != null){
                    dia = extractDia(diaDaSemanaCtx);
                } else {
                    final DiaDaSemanaPorReferenciaContext 
                        diaDaSemanaRefCtx = diaCtx.diaDaSemanaPorReferencia();

                        dia = extractDia(diaDaSemanaRefCtx);
                }
            }
        }
        
        return dia;
    }

    private LocalDate extractDia(DiaSimplesContext diaSimplesCtx) {

        final LocalDate hoje = LocalDate.now();
        
        return diaSimplesCtx.HOJE() != null
            ? hoje
            : diaSimplesCtx.AMANHA() != null
                ? hoje.plusDays(1)
                : diaSimplesCtx.ONTEM() != null
                    ?  hoje.minusDays(1)
                    : diaSimplesCtx.ANTE_ONTEM() != null
                        ? hoje.minusDays(2)
                        :  diaSimplesCtx.DEPOIS_DE_AMANHA() != null
                            ? hoje.plusDays(2)
                            : diaSimplesCtx.DE_HOJE_A_OITO() != null
                                ? hoje.plusDays(7)
                                : hoje.plusDays(14);
    }

    private LocalDate extractDia(
            final DiaDaSemanaContext diaDaSemanaCtx) {

        return 
            LocalDate.now().with(
                DiaHelper.diaDaSemanaMaisPerto(
                    extractDiaDaSemana(diaDaSemanaCtx)));
        
    }

    private DayOfWeek extractDiaDaSemana(
            final  DiaDaSemanaContext diaDaSemanaCtx) {
        
        return pick(DayOfWeek.values(), 
                diaDaSemanaCtx.SEG(),
                diaDaSemanaCtx.TER(),
                diaDaSemanaCtx.QUA(),
                diaDaSemanaCtx.QUI(),
                diaDaSemanaCtx.SEX(),
                diaDaSemanaCtx.SAB(),
                diaDaSemanaCtx.DOM());
    }

    private LocalDate extractDia(
        DiaDaSemanaPorReferenciaContext diaDaSemanaRefCtx) {

        final DayOfWeek diaDaSemana = 
            extractDiaDaSemana(diaDaSemanaRefCtx.diaDaSemana());

        final Variante variante = 
            extractVariante(diaDaSemanaRefCtx.varianteMasc());
        
        return  
            LocalDate.now().with(
                variante == Variante.QUE_VEM 
                    ? TemporalAdjusters.next(diaDaSemana)
                    : TemporalAdjusters.previous(diaDaSemana));       
    }

    private Variante extractVariante(
            VarianteMascContext varianteMascCtx) {
        
        return varianteMascCtx.PASSADO() != null
                ? Variante.PASSADO 
                : Variante.QUE_VEM;
    }

    private Variante extractVariante(
            VarianteFemContext varianteFemCtx) {

        return varianteFemCtx.PASSADA() != null
                ? Variante.PASSADO 
                : Variante.QUE_VEM;
    }    

    private Periodo extractPeriodo(
            final PeridoComplexoContext peridoComplexo) {
        final LocalDate inicio = 
            extractPeriodo(peridoComplexo.periodoSimples(0)).getDataMinima();

        final LocalDate fim = 
            extractPeriodo(peridoComplexo.periodoSimples(1)).getDataMaxima();
        
        return new Periodo(inicio, fim);
    }

    private Periodo extractPeriodo(
            final PeriodoSimplesContext periodoSimplesCtx) {
        
        final Periodo periodo;
        
        final AnoContext anoCtx = periodoSimplesCtx.ano();
        if(anoCtx != null){
            periodo = extractPeriodo(anoCtx);
        } else {
            final DiaContext diaCtx = periodoSimplesCtx.dia();
            if(diaCtx != null){
                periodo = extractPeriodo(diaCtx);
            } else {
                final MesContext mesCtx = 
                    periodoSimplesCtx.mes();
                
                if(mesCtx != null){
                    periodo = extractPeriodo(mesCtx);
                } else {
                    final PeriodoReferenciadoContext 
                            periodoReferenciadoCtx = 
                                periodoSimplesCtx.periodoReferenciado();
                    if(periodoReferenciadoCtx != null){
                        periodo = extractPeriodo(periodoReferenciadoCtx);
                    } else {
                        periodo = 
                            extractPeriodo(periodoSimplesCtx.periodoSemana());
                    }
                }
            }
        }
        return periodo;
    }

    private Periodo extractPeriodo(final DiaContext diaCtx) {
        final LocalDate dia = extractDia(diaCtx);
        return new Periodo(dia, dia);
    }

    private Periodo extractPeriodo(AnoContext anoCtx) {
        
        final AnoPorReferenciaContext anoRefCtx = 
            anoCtx.anoPorReferencia();
        
        final AnoSimplesContext anoSimplesContext;
        final Variante variante;
        
        if(anoRefCtx != null){
            anoSimplesContext = anoRefCtx.anoSimples();
            variante = extractVariante(anoRefCtx.varianteMasc());
        } else {
            anoSimplesContext = anoCtx.anoSimples();
            variante = null;
        }
        
        final int ano = Integer.parseInt(anoSimplesContext.INT().getText());
        
        LocalDate inicio = LocalDate.ofYearDay(ano, 1);
        if(variante == Variante.PASSADO) {
            inicio.minusYears(1);
        } else if (variante == Variante.QUE_VEM){
            inicio.plusYears(1);
        }
        
        return 
            new Periodo(inicio, inicio.with(TemporalAdjusters.lastDayOfYear()));
    }

    private Periodo extractPeriodo(MesContext mesCtx) {
        final MesPorReferenciaContext mesRefCtx = 
            mesCtx.mesPorReferencia();
        
        final MesSimplesContext mesSimplesCtx;
        final Variante variante;
        
        if(mesRefCtx != null){
            mesSimplesCtx = mesRefCtx.mesSimples();
            variante = extractVariante(mesRefCtx.varianteMasc());
        } else {
            mesSimplesCtx = mesCtx.mesSimples();
            variante = null;
        }
        
        final Month mesBase = extractMes(mesSimplesCtx);
        
        final LocalDate inicio = 
            LocalDate
                .now()
                .withDayOfMonth(1)
                .with(DiaHelper.mesMaisPerto(mesBase));
        
        return 
            new Periodo(
                inicio, 
                inicio.with(TemporalAdjusters.lastDayOfMonth())
            );
        
    }

    private Periodo extractPeriodo(
            final PeriodoReferenciadoContext 
                    periodoReferenciadoCtx) {

        final OutroPeriodoContext outroPeriodoCtx = 
            periodoReferenciadoCtx.outroPeriodo();
        
        final NomeDePeriodoContext nomeDePeriodoCtx;
        final Variante variante;

        if(outroPeriodoCtx != null){
            nomeDePeriodoCtx = outroPeriodoCtx.nomeDePeriodo();
            variante = extractVariante(outroPeriodoCtx.varianteMasc());
        } else {
            nomeDePeriodoCtx = 
                periodoReferenciadoCtx.essePeriodo().nomeDePeriodo();
            variante = null;
        }
        
        return extractPeriodo(nomeDePeriodoCtx,variante);
    }

    private Periodo extractPeriodo(
            final NomeDePeriodoContext nomeDePeriodoCtx,
            final Variante variante) {
        
        final LocalDate inicio;
        final LocalDate fim;

        final LocalDate hoje = LocalDate.now();

        if(nomeDePeriodoCtx.MES() != null){
            inicio = 
                variante == Variante.QUE_VEM
                    ? hoje.with(TemporalAdjusters.firstDayOfNextMonth())
                    : variante == Variante.PASSADO
                        ? hoje.with(COMECO_DO_MES_ADJSTR).minusMonths(1)
                        : hoje.with(TemporalAdjusters.firstDayOfMonth());
            
            fim = inicio.with(TemporalAdjusters.lastDayOfMonth());
            
        } else if(nomeDePeriodoCtx.ANO() != null){
            inicio = 
                variante == Variante.QUE_VEM
                    ? hoje.with(TemporalAdjusters.firstDayOfNextYear())
                    : variante == Variante.PASSADO
                        ? hoje.with(COMECO_DO_ANO_ADJSTR).minusYears(1)
                        : hoje.with(COMECO_DO_ANO_ADJSTR);
                    
            fim = inicio.with(TemporalAdjusters.lastDayOfYear());
        //semestre
        } else if(hoje.getMonth().compareTo(Month.JUNE) <= 0){ 
            inicio = hoje.with(TemporalAdjusters.firstDayOfYear());

            fim = hoje
                    .withMonth(6)
                    .with(TemporalAdjusters.lastDayOfMonth());
        } else {
            inicio = hoje
                        .withMonth(7)
                        .with(COMECO_DO_MES_ADJSTR);

            fim = hoje.with(TemporalAdjusters.lastDayOfYear());
        }
        
        return new Periodo(inicio, fim);
    }

    private Periodo extractPeriodo(
        final PeriodoSemanaContext periodoSemanaCtx) {
        
        final OutraSemanaContext outraSemanaCtx = 
            periodoSemanaCtx.outraSemana();
        
        final LocalDate inicio = LocalDate.now().with(
            outraSemanaCtx == null
            ? TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)
            : outraSemanaCtx.varianteFem().QUE_VEM() != null
                ? TemporalAdjusters.next(DayOfWeek.SUNDAY)
                : date -> date
                    .with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
                    .with(TemporalAdjusters.previous(DayOfWeek.SUNDAY))
        );
        
        return
            new Periodo(
                inicio, 
                inicio.with(TemporalAdjusters.next(DayOfWeek.SATURDAY)));
    }

    private int extractId(final IdContext idContext){
        return Integer.valueOf(idContext.getText());
    }
    
    private Month extractMes(
            final MesSimplesContext mesSimplesCtx) {
        return pick(
            Month.values(),
            mesSimplesCtx.JAN(),
            mesSimplesCtx.FEV(),
            mesSimplesCtx.MAR(),
            mesSimplesCtx.ABR(),
            mesSimplesCtx.MAI(),
            mesSimplesCtx.JUN(),
            mesSimplesCtx.JUL(),
            mesSimplesCtx.AGO(),
            mesSimplesCtx.SET(),
            mesSimplesCtx.OUT(),
            mesSimplesCtx.NOV(),
            mesSimplesCtx.DEZ());
    }
    
    private <E> E pick (E[] values, Object... es){
        int i ;
        for (i = 0; i < es.length; i++) {
            if(es[i] != null) break;
        }
        return values[i];
    }
}