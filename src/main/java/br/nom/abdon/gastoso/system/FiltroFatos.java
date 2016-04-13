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
public class FiltroFatos {
    public static enum ORDEM {POR_DIA, POR_DESCRICAO}

    private LocalDate dataMinima, dataMaxima;
    private Integer inicio, fim, quantos;
    private Integer id;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    private ORDEM ordem;

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

    public Integer getInicio() {
        return inicio;
    }

    public void setInicio(Integer inicio) {
        this.inicio = inicio;
    }

    public Integer getFim() {
        return fim;
    }

    public void setFim(Integer fim) {
        this.fim = fim;
    }

    public Integer getQuantos() {
        return quantos;
    }

    public void setQuantos(Integer quantos) {
        this.quantos = quantos;
    }

    public ORDEM getOrdem() {
        return ordem;
    }

    public void setOrdem(ORDEM ordem) {
        this.ordem = ordem;
    }    
}
