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
package br.nom.abdon.gastoso.cli.command;

import java.time.LocalDate;

/**
 *
 * @author Bruno Abdon
 */
public class Periodo {

    private LocalDate dataMinima;
    private LocalDate dataMaxima;

    public Periodo(final LocalDate dataMinima, final LocalDate dataMaxima) {
        this.dataMinima = dataMinima;
        this.dataMaxima = dataMaxima;
    }

    public LocalDate getDataMinima() {
        return dataMinima;
    }

    public void setDataMinima(final LocalDate dataMinima) {
        this.dataMinima = dataMinima;
    }

    public LocalDate getDataMaxima() {
        return dataMaxima;
    }

    public void setDataMaxima(final LocalDate dataMaxima) {
        this.dataMaxima = dataMaxima;
    }

}
