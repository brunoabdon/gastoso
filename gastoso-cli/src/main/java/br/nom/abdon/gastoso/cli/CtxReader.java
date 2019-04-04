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

import java.time.DateTimeException;

import br.nom.abdon.util.Periodo;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.function.Supplier;

import org.antlr.v4.runtime.tree.TerminalNode;

import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.AnoContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.DiaContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.IdContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.MesContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.MesISOContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.NomeDePeriodoContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.PeridoComplexoContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.PeriodoContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.PeriodoReferenciadoContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.PeriodoSemanaContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.PeriodoSimplesContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.TextArgContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.ValorContext;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.VarianteFemContext;
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


    public static String extractText(
        final TextArgContext textArg, final String fallback) {
        return textArg == null ? fallback : extractText(textArg);
    }

    public static String extractText(
        final TextArgContext textArg, final Supplier<String> fallback) {
        return textArg == null ? fallback.get() : extractText(textArg);
    }

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

    public static int extract(final ValorContext valorCtx)  {
        int valor = asInt(valorCtx.INT()) * 100;

        final TerminalNode centavosTerm = valorCtx.CENTAVOS();

        if(centavosTerm != null){
            valor += Integer.parseInt(centavosTerm.getText().substring(1));
        }

        if(valorCtx.MENOS() != null) valor *= -1;

        return valor;
    }

    public static LocalDate extractDia(
        final DiaContext diaCtx, final LocalDate fallback)
            throws CLIException {
        return diaCtx == null ? fallback : extractDia(diaCtx);
    }

    public static LocalDate extractDia(
        final DiaContext diaCtx,
        final Supplier<LocalDate> fallback)
            throws CLIException {
        return diaCtx == null ? fallback.get() : extractDia(diaCtx);
    }

    public static LocalDate extractDia(final DiaContext diaCtx)
            throws CLIException {

        final LocalDate hoje = LocalDate.now();
        return diaCtx.mesISO() != null
            ? extractDiaIso(diaCtx)
            : diaCtx.HOJE() != null
                ? hoje
                : diaCtx.AMANHA() != null
                    ? hoje.plusDays(1)
                    : diaCtx.ONTEM() != null
                        ?  hoje.minusDays(1)
                        : diaCtx.ANTE_ONTEM() != null
                            ? hoje.minusDays(2)
                            :  diaCtx.DEPOIS_DE_AMANHA() != null
                                ? hoje.plusDays(2)
                                : diaCtx.DE_HOJE_A_OITO() != null
                                    ? hoje.plusDays(7)
                                    : diaCtx.DE_HOJE_A_QUINZE() != null
                                        ? hoje.plusDays(14)
                                        : diaCtx.varianteMasc() != null
                                            ? extractDiaDaSemanaRef(diaCtx)
                                            : LocalDate.now().with(
                                                DiaHelper.diaDaSemanaMaisPerto(
                                                    extractDiaDaSemana(diaCtx)))
        ;
    }

    private static LocalDate extractDiaIso(final DiaContext diaCtx)
            throws CLIException {
        final LocalDate dia;
        try {
            dia = extract(diaCtx.mesISO()).atDay(asInt(diaCtx.INT()));
        } catch (DateTimeException e){
            throw new CLIException("Essa data não existe", e);
        }
        return dia;
    }

    private static DayOfWeek extractDiaDaSemana(final DiaContext diaCtx) {

        return pick(DayOfWeek.values(),
                diaCtx.SEG(),
                diaCtx.TER(),
                diaCtx.QUA(),
                diaCtx.QUI(),
                diaCtx.SEX(),
                diaCtx.SAB(),
                diaCtx.DOM());
    }

    private static LocalDate extractDiaDaSemanaRef(final DiaContext diaCtx) {

        final DayOfWeek diaDaSemana = extractDiaDaSemana(diaCtx);

        final Variante variante =
            extractVariante(diaCtx.varianteMasc());

        return
            LocalDate.now().with(
                variante == Variante.QUE_VEM
                    ? TemporalAdjusters.next(diaDaSemana)
                    : TemporalAdjusters.previous(diaDaSemana));
    }

    private static Variante extractVariante(
            final VarianteMascContext varianteMascCtx) {

        return varianteMascCtx.PASSADO() != null
                ? Variante.PASSADO
                : Variante.QUE_VEM;
    }

    public static Periodo extract(
        final PeriodoContext periodoDefCtx, final Periodo fallback)
            throws CLIException {
        return periodoDefCtx == null ? fallback : extract(periodoDefCtx);
    }

    public static Periodo extract(
        final PeriodoContext periodoDefCtx, 
        final Supplier<Periodo> fallback)
            throws CLIException {
        return periodoDefCtx == null ? fallback.get() : extract(periodoDefCtx);

    }

    public static Periodo extract(final PeriodoContext periodoDefCtx)
            throws CLIException {

        final PeriodoSimplesContext periodoSimplesCtx =
                periodoDefCtx.periodoSimples();

        return periodoSimplesCtx != null
                ? CtxReader.extractPeriodo(periodoSimplesCtx)
                : CtxReader.extractPeriodo(periodoDefCtx.peridoComplexo());
    }

    private static Periodo extractPeriodo(
        final PeridoComplexoContext peridoComplexo) throws CLIException {

        final LocalDate inicio =
            extractPeriodo(peridoComplexo.periodoSimples(0)).getDataMinima();

        final LocalDate fim =
            extractPeriodo(peridoComplexo.periodoSimples(1)).getDataMaxima();

        return new Periodo(inicio, fim);
    }

    private static Periodo extractPeriodo(
            final PeriodoSimplesContext periodoSimplesCtx) throws CLIException {

        final Periodo periodo;

        final AnoContext anoCtx = periodoSimplesCtx.ano();
        if(anoCtx != null){
            periodo = extractPeriodo(anoCtx);
        } else {
            final DiaContext diaCtx = periodoSimplesCtx.dia();
            if(diaCtx != null){
                periodo = extractPeriodo(diaCtx);
            } else {
                final MesContext mesCtx = periodoSimplesCtx.mes();

                if(mesCtx != null){
                    periodo = extractPeriodo(mesCtx);
                } else {
                    final PeriodoReferenciadoContext periodoReferenciadoCtx =
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

    private static Periodo extractPeriodo(final DiaContext diaCtx)
            throws CLIException {
        final LocalDate dia = extractDia(diaCtx);
        return new Periodo(dia, dia);
    }

    private static Periodo extractPeriodo(final AnoContext anoCtx) {

        final LocalDate inicio;

        final TerminalNode INT = anoCtx.INT();

        if(INT != null){
            final int ano = Integer.parseInt(INT.getText());
            inicio = LocalDate.ofYearDay(ano, 1);
        } else {
            final Variante variante = extractVariante(anoCtx.varianteMasc());

            inicio =
                LocalDate.now()
                .withDayOfYear(1)
                .plusYears(variante == Variante.PASSADO ? -1 : 1);
        }

        return
            new Periodo(inicio, inicio.with(TemporalAdjusters.lastDayOfYear()));
    }

    private static Periodo extractPeriodo(final MesContext mesCtx)
            throws CLIException {

        final YearMonth mes = extractMes(mesCtx);

        return
            new Periodo(
                mes.atDay(1),
                mes.atEndOfMonth()
            );
    }

    private static Periodo extractPeriodo(
            final PeriodoReferenciadoContext periodoReferenciadoCtx) {

        final NomeDePeriodoContext nomeDePeriodoCtx =
            periodoReferenciadoCtx.nomeDePeriodo();

        final Variante variante =
            periodoReferenciadoCtx.varianteMasc() != null
                ? extractVariante(periodoReferenciadoCtx.varianteMasc())
                : null;

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

        final VarianteFemContext varianteFemCtx =
            periodoSemanaCtx.varianteFem();

        final LocalDate inicio = LocalDate.now().with(
            varianteFemCtx == null
                ? TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)
                : varianteFemCtx.QUE_VEM() != null
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

    private static YearMonth extractMes(final MesContext mesCtx)
            throws CLIException {

        final YearMonth mesDoAno;

        final MesISOContext mesISOCtx = mesCtx.mesISO();

        if(mesISOCtx != null){
            mesDoAno = extract(mesISOCtx);
        } else {

            final Month mes = pick(
                Month.values(),
                mesCtx.JAN(),
                mesCtx.FEV(),
                mesCtx.MAR(),
                mesCtx.ABR(),
                mesCtx.MAI(),
                mesCtx.JUN(),
                mesCtx.JUL(),
                mesCtx.AGO(),
                mesCtx.SET(),
                mesCtx.OUT(),
                mesCtx.NOV(),
                mesCtx.DEZ());

            final VarianteMascContext varianteMascCtx = mesCtx.varianteMasc();

             final TemporalAdjuster adjuster =
                varianteMascCtx == null
                    ? DiaHelper.mesMaisPerto(mes)
                    : extractVariante(varianteMascCtx) == Variante.QUE_VEM
                        ? DiaHelper.next(mes)
                        : DiaHelper.previous(mes);

            mesDoAno = YearMonth.now().with(adjuster);
        }
        return mesDoAno;
    }

    private static YearMonth extract(final MesISOContext mesISOCtx)
            throws CLIException {
        int ano = asInt(mesISOCtx.INT(0));
        int mes = asInt(mesISOCtx.INT(1));

        final YearMonth mesDoAno;

        try{
            mesDoAno = YearMonth.of(ano, mes);
        } catch (final DateTimeException e){
            throw new CLIException("Disse quando?", e);
        }

        return mesDoAno;
    }

    private static <E> E pick (final E[] values, final Object... es){
        int i ;
        for (i = 0; i < es.length; i++) {
            if(es[i] != null) break;
        }
        return values[i];
    }


    private static int asInt(final TerminalNode tn){
        return Integer.parseInt(tn.getText());
    }
}