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
package com.github.brunoabdon.gastoso.cli.util;

import java.time.DayOfWeek;
import java.time.Month;
import static java.time.temporal.ChronoField.DAY_OF_WEEK;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoUnit.MONTHS;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;

/**
 *
 * @author Bruno Abdon
 */
public class DiaHelper {

    public static TemporalAdjuster diaDaSemanaMaisPerto(
            final DayOfWeek diaDaSemana){

        return (temporal) ->  {
            final int diferenca =
                temporal.get(DAY_OF_WEEK) - diaDaSemana.getValue();

            final TemporalAdjuster adjuster =
                diferenca <= -3 || (diferenca >= 0 && diferenca <= 4)
                    ? TemporalAdjusters.previousOrSame(diaDaSemana)
                    : TemporalAdjusters.next(diaDaSemana);

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
            return temporal.with(adjuster);
        };
    }

    public static TemporalAdjuster previousOrSame(final Month month) {
        return previousMaybeSame(month, true);
    }
    public static TemporalAdjuster previous(final Month month) {
        return previousMaybeSame(month, false);
    }

    private static TemporalAdjuster previousMaybeSame(
            final Month month,
            final boolean allowSame) {

        final int dowValue = month.getValue();
        return (temporal) -> {
            final Temporal adjuster;
            
            int calDow = temporal.get(MONTH_OF_YEAR);
            if (allowSame && calDow == dowValue) {
                adjuster = temporal;
            } else {
                int monthsDiff = dowValue - calDow;
                adjuster = 
                    temporal.minus(
                        monthsDiff >= 0
                            ? 12 - monthsDiff
                            : -monthsDiff
                        , MONTHS);
            }
            return adjuster;
        };
    }

    public static TemporalAdjuster next(final Month month) {
        return (temporal) -> {
            final int monthsDiff = 
                temporal.get(MONTH_OF_YEAR) - month.getValue();

            return temporal.plus(
                monthsDiff >= 0
                    ? 12 - monthsDiff
                    : -monthsDiff
                    , MONTHS);
        };
    }
}