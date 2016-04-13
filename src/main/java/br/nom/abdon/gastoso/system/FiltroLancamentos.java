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

/**
 *
 * @author Bruno Abdon
 */
public class FiltroLancamentos {
    
    private FiltroFatos filtroFatos;
    private FiltroContas filtroContas;
    
    //private Integer valorMinimo, valorMaximo;

    public FiltroFatos getFiltroFatos() {
        if(this.filtroFatos == null) this.filtroFatos = new FiltroFatos();
        return filtroFatos;
    }

    public FiltroContas getFiltroContas() {
        if(this.filtroContas == null) this.filtroContas = new FiltroContas();
        return filtroContas;
    }

    
}
