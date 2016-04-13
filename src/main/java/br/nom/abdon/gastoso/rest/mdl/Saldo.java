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
package br.nom.abdon.gastoso.rest.mdl;

import java.time.LocalDate;

import br.nom.abdon.gastoso.Conta;

/**
 *
 * @author Bruno Abdon
 */
public class Saldo {

    private Conta conta;
    private LocalDate dia;
    private int valor;

    public Saldo(final LocalDate dia, final Conta conta, final int valor) {
        this.dia = dia;
        this.conta = conta;
        this.valor = valor;
    }

    public LocalDate getDia() {
        return dia;
    }

    public void setDia(LocalDate dia) {
        this.dia = dia;
    }

    public Conta getConta() {
        return conta;
    }

    public void setConta(Conta conta) {
        this.conta = conta;
    }

    public int getValor() {
        return valor;
    }

    public void setValor(int valor) {
        this.valor = valor;
    }
}