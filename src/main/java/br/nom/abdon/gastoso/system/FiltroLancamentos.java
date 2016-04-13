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

import java.util.LinkedList;
import java.util.List;


/**
 *
 * @author Bruno Abdon
 */
public class FiltroLancamentos {

    public static enum ORDEM {
        POR_DIA_ASC(true), POR_DESC_FATO_ASC(true), 
        POR_CONTA_ID_ASC(true), POR_CONTA_NOME_ASC(true), 
        POR_VALOR_ASC(true),
        
        POR_DIA_DESC(false), POR_DESC_FATO_DESC(false), 
        POR_CONTA_ID_DESC(false), POR_CONTA_NOME_DESC(false), 
        POR_VALOR_DESC(false);
    
        private final boolean isAsc;
        
        ORDEM(boolean isAsc){
            this.isAsc = isAsc;
        }
        
        public boolean isAsc(){
            return isAsc;
        }
    };
    
    private FiltroFatos filtroFatos;
    private FiltroContas filtroContas;
    
    private List<ORDEM> ordem;
    
    private final Paginacao paginacao = new Paginacao();
    
    //private Integer valorMinimo, valorMaximo;

    public FiltroFatos getFiltroFatos() {
        return filtroFatos;
    }

    public FiltroContas getFiltroContas() {
        return filtroContas;
    }

    public FiltroLancamentos addOrdem(ORDEM ordem){
        if(ordem == null) this.ordem = new LinkedList<>();
        this.ordem.add(ordem);
        return this;
    }

    public List<ORDEM> getOrdem() {
        return ordem;
    }

    public Paginacao getPaginacao() {
        return paginacao;
    }

    public void setFiltroFatos(FiltroFatos filtroFatos) {
        this.filtroFatos = filtroFatos;
    }

    public void setFiltroContas(FiltroContas filtroContas) {
        this.filtroContas = filtroContas;
    }
}