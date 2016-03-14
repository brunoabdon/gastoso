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
import java.time.Period;
import java.time.format.DateTimeFormatter;
import static java.time.temporal.ChronoField.DAY_OF_WEEK;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MONTHS;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalAmount;
import java.util.Locale;

/**
 *
 * @author Bruno Abdon
 */
public class DiaHelper {

    public static enum Variante {PASSADO, QUE_VEM};

    final static Locale prBR = new Locale("pt","BR");

    final static DateTimeFormatter formatter =
        DateTimeFormatter.ofPattern("EEEE dd/MMM", prBR);

    final static DateTimeFormatter formatterSemana =
        DateTimeFormatter.ofPattern("E",prBR);
    
    public static TemporalAdjuster diaDaSemanaMaisPerto(final DayOfWeek diaDaSemana){
        
        return (temporal) ->  {
            final int diferenca = 
                temporal.get(DAY_OF_WEEK) - diaDaSemana.getValue();
            
            final TemporalAdjuster adjuster = 
                diferenca <= -3 || (diferenca >= 0 && diferenca <= 4)
                    ? TemporalAdjusters.previousOrSame(diaDaSemana)
                    : TemporalAdjusters.next(diaDaSemana);
            ;
            
            return temporal.with(adjuster);
        };
    }

    public static TemporalAdjuster mesMaisPerto(final Month mes){
        
        return (temporal) ->  {
            final int diferenca = 
                temporal.get(MONTH_OF_YEAR) - mes.getValue();
            
            final TemporalAdjuster adjuster = 
                diferenca <= -6 || (diferenca >= 0 && diferenca <= 6)
                    ? previousOrSame(mes)
                    : next(mes);
            ;
            return temporal.with(adjuster);
        };
    }
    
    public static TemporalAdjuster previousOrSame(Month month) {
        int dowValue = month.getValue();
        return (temporal) -> {
            int calDow = temporal.get(MONTH_OF_YEAR);
            if (calDow == dowValue) {
                return temporal;
            }
            int monthsDiff = dowValue - calDow;
            return temporal.minus(
                    monthsDiff >= 0 
                        ? 12 - monthsDiff 
                        : -monthsDiff
                    , MONTHS);
        };
    }

    public static TemporalAdjuster next(Month month) {
        int dowValue = month.getValue();
        return (temporal) -> {
            int calDow = temporal.get(MONTH_OF_YEAR);
            
            int monthsDiff = calDow - dowValue;
            return temporal.plus(
                monthsDiff >= 0 
                    ? 12 - monthsDiff 
                    : -monthsDiff
                    , MONTHS);
        };
    }

    
    public static void main(String[] args) {
        testaDia();
//        testaDiaVariante();
    }

    private static void testaDiaVariante() {
        final Locale prBR = new Locale("pt","BR");
        
        final DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("EEEE dd/MMM", prBR);
        
        final DateTimeFormatter formatterSemana =
                DateTimeFormatter.ofPattern("EEEE",prBR);
        
        final GastosoCLIListener listener = new GastosoCLIListener();

        LocalDate hoje = LocalDate.now();
        for (int i = 0; i < 7; hoje = hoje.plusDays(1), i++) {
            for (DayOfWeek diaPedido : DayOfWeek.values()) {
                for(Variante v : Variante.values()){

                    final LocalDate diaExtraido =

                    hoje.with(
                        v == Variante.QUE_VEM 
                            ? TemporalAdjusters.next(diaPedido)
                            : TemporalAdjusters.previous(diaPedido));

//                            DiaHelper. extractDia(hoje,diaPedido,v);
                    System.out.println(
                            "Em "
                                    + formatter.format(hoje)
                                    + ", pediu "
                                    + formatterSemana.format(diaPedido)
                                    + " " + v
                                    + ". Deu: "
                                    + formatter.format(diaExtraido));
                }
            }
            System.out.println();
        }
    }
    
    
    private static void testaDia() {
        final Locale prBR = new Locale("pt","BR");
        
        final DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("EEEE dd/MMM", prBR);
        
        final DateTimeFormatter formatterSemana =
                DateTimeFormatter.ofPattern("EEEE",prBR);
        
        final GastosoCLIListener listener = new GastosoCLIListener();

        LocalDate hoje = LocalDate.now();
        for (int i = 0; i < 7; hoje = hoje.plusDays(1), i++) {
            for (DayOfWeek diaPedido : DayOfWeek.values()) {
                final LocalDate diaExtraido =
                    hoje.with(diaDaSemanaMaisPerto(diaPedido));
//                        DiaHelper. extractDia(hoje,diaPedido);
                System.out.println(
                        "Em "
                                + formatter.format(hoje)
                                + ", pediu "
                                + formatterSemana.format(diaPedido)
                                + ". Deu: "
                                + formatter.format(diaExtraido));
            }
            System.out.println();
        }
    }
}
