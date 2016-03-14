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

import br.nom.abdon.gastoso.cli.parser.GastosoCliBaseListener;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.antlr.v4.runtime.tree.TerminalNode;

import br.nom.abdon.gastoso.cli.DiaHelper.Variante;
import br.nom.abdon.gastoso.cli.command.Periodo;
import java.time.Month;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;

/**
 *
 * @author Bruno Abdon
 */
public class GastosoCLIListener extends GastosoCliBaseListener{

    private static final TemporalAdjuster COMECO_DO_MES_ADJSTR = 
        TemporalAdjusters.firstDayOfMonth();

    private static final TemporalAdjuster COMECO_DO_ANO_ADJSTR = 
        TemporalAdjusters.firstDayOfYear();

    

    @Override
    public void exitCommand(GastosoCliParser.CommandContext commandCtx) {
        final GastosoCliParser.LineCommandContext lineCommandCtx = 
            commandCtx.lineCommand();
        
        final GastosoCliParser.FatosContext fatosCtx = lineCommandCtx.fatos();
        if(fatosCtx != null){
            commandFatos(fatosCtx);

        }  else {
            final GastosoCliParser.FatoContext fatoCtx = lineCommandCtx.fato();
            if(fatoCtx != null){
                commandFato(fatoCtx);
            } else {
                final GastosoCliParser.PeriodoContext periodoCtx = 
                    lineCommandCtx.periodo();
                
                if(periodoCtx != null){
                    commandPeriodo(periodoCtx);
                }
                
            }
        }
    }
    
    
    private void commandFato(GastosoCliParser.FatoContext ctx) {
        
        final GastosoCliParser.FatoArgsContext fatoArgsCtx = ctx.fatoArgs();
        
        if(fatoArgsCtx instanceof GastosoCliParser.FatoIdContext){

            final GastosoCliParser.FatoIdContext fatoIdCtx = 
                (GastosoCliParser.FatoIdContext) fatoArgsCtx;
            
            final int fatoId = Integer.valueOf(fatoIdCtx.id().getText());
            System.out.printf("Exibir fato %d\n",fatoId);
        
        } else if (fatoArgsCtx instanceof GastosoCliParser.FatoSubIdContext){

            final GastosoCliParser.FatoSubIdContext fatoSubIdCtx = 
                (GastosoCliParser.FatoSubIdContext) fatoArgsCtx;
            
            final GastosoCliParser.SubIdContext subIdCtx = 
                fatoSubIdCtx.subId();
            
            final int fatoId = Integer.valueOf(subIdCtx.id(0).getText());
            
            final int contaId = Integer.valueOf(subIdCtx.id(1).getText());
            
            final GastosoCliParser.ValorContext valorCtx = fatoSubIdCtx.valor();
            
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
        } else if(fatoArgsCtx instanceof GastosoCliParser.MkFatoContext){
            final GastosoCliParser.MkFatoContext mkFatoCtx = 
                (GastosoCliParser.MkFatoContext) fatoArgsCtx;
            
            final String descricao = mkFatoCtx.textArg().getText();
            
            final GastosoCliParser.DiaContext diaCtx = mkFatoCtx.dia();
            final LocalDate dia = extractDia(diaCtx);
            
            System.out.println(
                "Criar fato '" 
                + descricao 
                + "' no dia " 
                + dia.format(DateTimeFormatter.ISO_DATE));
        }
    }
    
    private void commandFatos(GastosoCliParser.FatosContext ctx) {
        System.out.println("Listar fatos do periodo setado");
    }
    
    private void commandPeriodo(GastosoCliParser.PeriodoContext ctx){
        final GastosoCliParser.PeriodoDefContext periodoDefCtx = 
            ctx.periodoDef();
        
        final Periodo periodo;
        
        if(periodoDefCtx == null){
            System.out.println("Dizer qual o periodo setado");
        } else {
            final GastosoCliParser.PeriodoSimplesContext periodoSimplesCtx = 
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
    

    private LocalDate extractDia(GastosoCliParser.DiaContext diaCtx) {
        final LocalDate dia;
    
        if(diaCtx == null){
            dia = LocalDate.now();
            
        } else {
        
            final GastosoCliParser.DiaSimplesContext diaSimplesCtx = 
                diaCtx.diaSimples();
        
            if(diaSimplesCtx != null){
                dia = extractDia(diaSimplesCtx);
            } else {
                
                final GastosoCliParser.DiaDaSemanaContext diaDaSemanaCtx = 
                    diaCtx.diaDaSemana();

                if(diaDaSemanaCtx != null){
                    dia = extractDia(diaDaSemanaCtx);
                } else {
                    final GastosoCliParser.DiaDaSemanaPorReferenciaContext 
                        diaDaSemanaRefCtx = diaCtx.diaDaSemanaPorReferencia();

                        dia = extractDia(diaDaSemanaRefCtx);
                }
            }
        }
        
        return dia;
    }

    private LocalDate extractDia(GastosoCliParser.DiaSimplesContext diaSimplesCtx) {

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
            final GastosoCliParser.DiaDaSemanaContext diaDaSemanaCtx) {

        return 
            LocalDate.now().with(
                DiaHelper.diaDaSemanaMaisPerto(
                    extractDiaDaSemana(diaDaSemanaCtx)));
        
    }

    private DayOfWeek extractDiaDaSemana(
            final  GastosoCliParser.DiaDaSemanaContext diaDaSemanaCtx) {
        
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
        GastosoCliParser.DiaDaSemanaPorReferenciaContext diaDaSemanaRefCtx) {

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
            GastosoCliParser.VarianteMascContext varianteMascCtx) {
        
        return varianteMascCtx.PASSADO() != null
                ? Variante.PASSADO 
                : Variante.QUE_VEM;
    }

    private Variante extractVariante(
            GastosoCliParser.VarianteFemContext varianteFemCtx) {

        return varianteFemCtx.PASSADA() != null
                ? Variante.PASSADO 
                : Variante.QUE_VEM;
    }    

    private Periodo extractPeriodo(
            final GastosoCliParser.PeridoComplexoContext peridoComplexo) {
        final LocalDate inicio = 
            extractPeriodo(peridoComplexo.periodoSimples(0)).getDataMinima();

        final LocalDate fim = 
            extractPeriodo(peridoComplexo.periodoSimples(1)).getDataMaxima();
        
        return new Periodo(inicio, fim);
    }

    private Periodo extractPeriodo(
            final GastosoCliParser.PeriodoSimplesContext periodoSimplesCtx) {
        
        final Periodo periodo;
        
        final GastosoCliParser.AnoContext anoCtx = periodoSimplesCtx.ano();
        if(anoCtx != null){
            periodo = extractPeriodo(anoCtx);
        } else {
            final GastosoCliParser.DiaContext diaCtx = periodoSimplesCtx.dia();
            if(diaCtx != null){
                periodo = extractPeriodo(diaCtx);
            } else {
                final GastosoCliParser.MesContext mesCtx = 
                    periodoSimplesCtx.mes();
                
                if(mesCtx != null){
                    periodo = extractPeriodo(mesCtx);
                } else {
                    final GastosoCliParser.PeriodoReferenciadoContext 
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

    private Periodo extractPeriodo(final GastosoCliParser.DiaContext diaCtx) {
        final LocalDate dia = extractDia(diaCtx);
        return new Periodo(dia, dia);
    }

    private Periodo extractPeriodo(GastosoCliParser.AnoContext anoCtx) {
        
        final GastosoCliParser.AnoPorReferenciaContext anoRefCtx = 
            anoCtx.anoPorReferencia();
        
        final GastosoCliParser.AnoSimplesContext anoSimplesContext;
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

    private Periodo extractPeriodo(GastosoCliParser.MesContext mesCtx) {
        final GastosoCliParser.MesPorReferenciaContext mesRefCtx = 
            mesCtx.mesPorReferencia();
        
        final GastosoCliParser.MesSimplesContext mesSimplesCtx;
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
            final GastosoCliParser.PeriodoReferenciadoContext 
                    periodoReferenciadoCtx) {

        final GastosoCliParser.OutroPeriodoContext outroPeriodoCtx = 
            periodoReferenciadoCtx.outroPeriodo();
        
        final GastosoCliParser.NomeDePeriodoContext nomeDePeriodoCtx;
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
            final GastosoCliParser.NomeDePeriodoContext nomeDePeriodoCtx,
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
        final GastosoCliParser.PeriodoSemanaContext periodoSemanaCtx) {
        
        final GastosoCliParser.OutraSemanaContext outraSemanaCtx = 
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

    private Month extractMes(
            final GastosoCliParser.MesSimplesContext mesSimplesCtx) {
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


