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

import java.util.List;

import com.github.brunoabdon.gastoso.ext.FatoDetalhado;
import com.github.brunoabdon.gastoso.ext.Saldo;
import com.github.brunoabdon.gastoso.system.GastosoSystem;
import com.github.brunoabdon.gastoso.system.GastosoSystemException;
import com.github.brunoabdon.gastoso.system.GastosoSystemRTException;
import com.github.brunoabdon.gastoso.system.NotFoundException;

/**
 *
 * @author Bruno Abdon
 */
public interface GastosoSystemExtended extends GastosoSystem{
    
    public Saldo getSaldo(int id) 
        throws NotFoundException,
                GastosoSystemRTException,
                GastosoSystemException;

    public List<Saldo> getSaldos(FiltroSaldos filtro)
        throws GastosoSystemRTException, GastosoSystemException;
    
    public FatoDetalhado getFatoDetalhado(int id)
        throws NotFoundException,
                GastosoSystemRTException,
                GastosoSystemException;
    
    public List<FatoDetalhado> getFatosDetalhados(FiltroFatosDetalhados filtro)
        throws GastosoSystemRTException, GastosoSystemException;
    
}
