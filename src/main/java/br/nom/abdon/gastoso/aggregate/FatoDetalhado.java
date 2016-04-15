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
package br.nom.abdon.gastoso.aggregate;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

import br.nom.abdon.gastoso.Fato;
import br.nom.abdon.gastoso.Lancamento;

/**
 *
 * @author Bruno Abdon
 */
public class FatoDetalhado extends Fato {
    
    private final List<Lancamento> lancamentos;

    public FatoDetalhado(
            final Fato fato, 
            final List<Lancamento> lancamentos) {
        this(fato.getId(), fato.getDia(), fato.getDescricao(), lancamentos);
    }

    public FatoDetalhado() {
        this.lancamentos = new LinkedList<>();
    }

    public FatoDetalhado(
            final Integer id, 
            final LocalDate dia, 
            final String descricao, 
            final List<Lancamento> lancamentos) {
        super(dia,descricao);
        super.setId(id);
        this.lancamentos = lancamentos;
    }

    public List<Lancamento> getLancamentos() {
        return lancamentos;
    }

    public void addLancamento(final Lancamento lancamento) {
        lancamentos.add(lancamento);
    }

    public Fato asFato(){
        Fato fato =  new Fato(getDia(),getDescricao());
        fato.setId(getId());
        return fato;
    }
    
    public static Fato asFato(Fato fato){
        return 
            (fato instanceof FatoDetalhado) 
                ? ((FatoDetalhado)fato).asFato()
                : fato;
    }
}