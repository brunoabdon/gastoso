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
package br.nom.abdon.gastoso.ext;

import java.time.LocalDate;

import br.nom.abdon.gastoso.Conta;
import br.nom.abdon.util.Identifiable;

/**
 *
 * @author Bruno Abdon
 */
public class Saldo implements Identifiable<Integer>{

    private Conta conta;
    private LocalDate dia;
    private long valor;

    public Saldo(final Conta conta, final LocalDate dia, final long valor) {
        this.dia = dia;
        this.conta = conta;
        this.valor = valor;
    }

    @Override
    public Integer getId(){
        return conta == null? 0 : conta.getId();
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

    public long getValor() {
        return valor;
    }

    public void setValor(long valor) {
        this.valor = valor;
    }
}