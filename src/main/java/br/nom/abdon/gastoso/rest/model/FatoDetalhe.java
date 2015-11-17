/*
 * Copyright (C) 2015 Bruno Abdon
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

import br.nom.abdon.gastoso.Fato;
import br.nom.abdon.gastoso.Lancamento;
import java.util.List;
import java.util.Objects;
import javax.persistence.NamedQuery;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 *
 * @author Bruno Abdon
 */
public class FatoDetalhe {
    
    private Fato fato;
    private List<Lancamento> lancamentos;

    public FatoDetalhe(Fato fato, List<Lancamento> lancamentos){
        this.fato = fato;
        this.lancamentos = lancamentos;
    }

    public Fato getFato() {
        return fato;
    }

    public void setFato(Fato fato) {
        this.fato = fato;
    }

    public List<Lancamento> getLancamentos() {
        return lancamentos;
    }

    public void setLancamentos(List<Lancamento> lancamentos) {
        this.lancamentos = lancamentos;
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder(37, 11)
            .append(getFato())
            .append(getLancamentos())
            .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        boolean equal = obj != null && (obj instanceof FatoDetalhe);
        if(equal){
            final FatoDetalhe fatoDetalhe = (FatoDetalhe) obj;
            equal = 
                Objects.equals(this.getFato(), fatoDetalhe.getFato())
                && Objects.equals(this.getLancamentos(), fatoDetalhe.getLancamentos());
        }
        return equal;
    }
    
    
}
