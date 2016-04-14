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
package br.nom.abdon.gastoso.rest.model;

import java.util.List;

import br.nom.abdon.gastoso.Fato;
import br.nom.abdon.gastoso.Lancamento;

/**
 *
 * @author Bruno Abdon
 */
public class FatoNormal extends Fato {
    
    private List<Lancamento> lancamentos;

    public FatoNormal(
            final Fato fato, 
            final List<Lancamento> lancamentos) {
        super(fato.getDia(), fato.getDescricao());
        super.setId(fato.getId());
        this.lancamentos = lancamentos;
    }

    public List<Lancamento> getLancamentos() {
        return lancamentos;
    }

    public void setLancamentos(List<Lancamento> lancamentos) {
        this.lancamentos = lancamentos;
    }
    /*
    public class Lancamento {
    
        private Conta conta;
        private int valor;


        public Lancamento(Conta conta, int valor) {
            this.conta = conta;
            this.valor = valor;
        }

        public int getValor() {
            return valor;
        }

        public void setValor(int valor) {
            this.valor = valor;
        }

        public Conta getConta() {
            return conta;
        }

        public void setConta(Conta conta) {
            this.conta = conta;
        }

    }*/
}