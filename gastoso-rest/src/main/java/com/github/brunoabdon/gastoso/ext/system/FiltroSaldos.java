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
package com.github.brunoabdon.gastoso.ext.system;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

import com.github.brunoabdon.gastoso.Conta;
import com.github.brunoabdon.gastoso.system.Paginacao;

/**
 *
 * @author Bruno Abdon
 */
public class FiltroSaldos {

    public static enum ORDEM {POR_CONTA, POR_DIA, POR_VALOR};
    
    private Conta conta;
    private LocalDate dia;

    private Paginacao paginacao = new Paginacao();
    private List<FiltroSaldos.ORDEM> ordem;


    public Conta getConta() {
        return conta;
    }

    public void setConta(Conta conta) {
        this.conta = conta;
    }

    public LocalDate getDia() {
        return dia;
    }

    public void setDia(LocalDate dia) {
        this.dia = dia;
    }

    public Paginacao getPaginacao() {
        return paginacao;
    }

    public void setPaginacao(Paginacao paginacao) {
        this.paginacao = paginacao;
    }
    
    public FiltroSaldos addOrdem(ORDEM ordem){
        if(this.ordem == null) this.ordem = new LinkedList<>();
        this.ordem.add(ordem);
        return this;
    }

    public List<FiltroSaldos.ORDEM> getOrdem() {
        return ordem;
    }
}