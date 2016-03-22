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

import java.time.LocalDate;

/**
 *
 * @author Bruno Abdon
 */
public class FiltroLancamento {
    private Integer fatoId, contaId;
    
    private LocalDate dataMinima, dataMaxima;

    public FiltroLancamento fromFato(int fatoId){
        this.fatoId = fatoId;
        return this;
    }
    
    public FiltroLancamento fromConta(int contaId){
        this.contaId = contaId;
        return this;
    }

    public FiltroLancamento withDataMinima(LocalDate dataMinima){
        this.dataMinima = dataMinima;
        return this;
    }

    public FiltroLancamento withDataMaxima(LocalDate dataMaxima){
        this.dataMaxima = dataMaxima;
        return this;
    }

    
    public Integer getContaId() {
        return contaId;
    }

    public void setContaId(Integer contaId) {
        this.contaId = contaId;
    }
    
    public Integer getFatoId() {
        return fatoId;
    }

    public void setFatoId(Integer fatoId) {
        this.fatoId = fatoId;
    }
    
        public LocalDate getDataMinima() {
        return dataMinima;
    }

    public void setDataMinima(LocalDate dataMinima) {
        this.dataMinima = dataMinima;
    }

    public LocalDate getDataMaxima() {
        return dataMaxima;
    }

    public void setDataMaxima(LocalDate dataMaxima) {
        this.dataMaxima = dataMaxima;
    }


    
}
