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
package br.nom.abdon.gastoso.system;

import br.nom.abdon.gastoso.Conta;

/**
 *
 * @author Bruno Abdon
 */
public class FiltroContas {

    public static enum ORDEM {ID, NOME};
    
    private Integer id;
    
    private Conta conta;
    
    private ORDEM ordem;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ORDEM getOrdem() {
        return ordem;
    }

    public void setOrdem(ORDEM ordem) {
        this.ordem = ordem;
    }
    
    
    public Conta getConta() {
        return conta;
    }

    public void setConta(Conta conta) {
        this.conta = conta;
    }

}
