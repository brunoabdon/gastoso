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

import br.nom.abdon.util.Periodo;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;

import org.antlr.v4.runtime.tree.TerminalNode;

import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.AnoContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.AnoPorReferenciaContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.AnoSimplesContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.DiaContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.DiaDaSemanaContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.DiaDaSemanaPorReferenciaContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.DiaSimplesContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.IdContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.MesContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.MesPorReferenciaContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.MesSimplesContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.NomeDePeriodoContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.OutraSemanaContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.OutroPeriodoContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.PeridoComplexoContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.PeriodoDefContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.PeriodoReferenciadoContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.PeriodoSemanaContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.PeriodoSimplesContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.TextArgContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.ValorContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.VarianteMascContext;
import br.nom.abdon.gastoso.cli.util.DiaHelper;

/**
 *
 * @author Bruno Abdon
 */
class CtxReader {
     static enum Variante {PASSADO, QUE_VEM};
    
    private static final TemporalAdjuster COMECO_DO_MES_ADJSTR = 
        TemporalAdjusters.firstDayOfMonth();

    private static final TemporalAdjuster COMECO_DO_ANO_ADJSTR = 
        TemporalAdjusters.firstDayOfYear();
    
    public static String extractText(final TextArgContext textArg) {
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
    
    public static LocalDate extractDia(DiaContext diaCtx) {
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

    public static int extract(final ValorContext valorCtx)  {
        int valor = (Integer.parseInt(valorCtx.INT().getText()) * 100);
        
        final TerminalNode centavosTerm = valorCtx.CENTAVOS();
        
        if(centavosTerm != null){
            valor += Integer.parseInt(centavosTerm.getText().substring(1));
        }
        
        if(valorCtx.MENOS() != null) valor *= -1;
        
        return valor;
    }
    
    private static LocalDate extractDia(DiaSimplesContext diaSimplesCtx) {

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

    private static LocalDate extractDia(
            final DiaDaSemanaContext diaDaSemanaCtx) {

        return 
            LocalDate.now().with(
                DiaHelper.diaDaSemanaMaisPerto(
                    extractDiaDaSemana(diaDaSemanaCtx)));
        
    }

    private static DayOfWeek extractDiaDaSemana(
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

    private static LocalDate extractDia(
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

    private static Variante extractVariante(
            VarianteMascContext varianteMascCtx) {
        
        return varianteMascCtx.PASSADO() != null
                ? Variante.PASSADO 
                : Variante.QUE_VEM;
    }

    public static Periodo extract(final PeriodoDefContext periodoDefCtx) {
        
        final PeriodoSimplesContext periodoSimplesCtx =
                periodoDefCtx.periodoSimples();
        
        return periodoSimplesCtx != null
                ? CtxReader.extractPeriodo(periodoSimplesCtx)
                : CtxReader.extractPeriodo(periodoDefCtx.peridoComplexo());
    }
    
    private static Periodo extractPeriodo(
            final PeridoComplexoContext peridoComplexo) {
        final LocalDate inicio = 
            extractPeriodo(peridoComplexo.periodoSimples(0)).getDataMinima();

        final LocalDate fim = 
            extractPeriodo(peridoComplexo.periodoSimples(1)).getDataMaxima();
        
        return new Periodo(inicio, fim);
    }

    private static Periodo extractPeriodo(
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

    private static Periodo extractPeriodo(final DiaContext diaCtx) {
        final LocalDate dia = extractDia(diaCtx);
        return new Periodo(dia, dia);
    }

    private static Periodo extractPeriodo(AnoContext anoCtx) {
        
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

    private static Periodo extractPeriodo(MesContext mesCtx) {
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

    private static Periodo extractPeriodo(
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

    private static Periodo extractPeriodo(
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

    private static Periodo extractPeriodo(
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

    public static int extractId(final IdContext idContext){
        return Integer.valueOf(idContext.getText());
    }
    
    private static Month extractMes(
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
    
    private static <E> E pick (E[] values, Object... es){
        int i ;
        for (i = 0; i < es.length; i++) {
            if(es[i] != null) break;
        }
        return values[i];
    }    
}