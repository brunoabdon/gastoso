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
package com.github.brunoabdon.gastoso.system;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

import com.github.brunoabdon.gastoso.Fato;

/**
 *
 * @author Bruno Abdon
 */
public class FiltroFatos {
    public static enum ORDEM {POR_DIA, POR_DESCRICAO, POR_CRIACAO}

    private LocalDate dataMinima, dataMaxima;
    private Integer inicio, fim, quantos;
    private Integer id;
    
    private Fato fato;

    private List<ORDEM> ordem;
    private final Paginacao paginacao = new Paginacao();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public FiltroFatos addOrdem(ORDEM ordemItem){
        if(this.ordem == null) this.ordem = new LinkedList<>();
        this.ordem.add(ordemItem);
        return this;
    }

    public List<ORDEM> getOrdem() {
        return ordem;
    }
    
    public Fato getFato() {
        return fato;
    }

    public void setFato(Fato fato) {
        this.fato = fato;
    }

    public Paginacao getPaginacao() {
        return paginacao;
    }
}