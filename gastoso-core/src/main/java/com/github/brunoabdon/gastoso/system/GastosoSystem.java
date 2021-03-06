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

import com.github.brunoabdon.gastoso.Conta;
import com.github.brunoabdon.gastoso.Fato;
import com.github.brunoabdon.gastoso.Lancamento;

import java.util.List;

/**
 *
 * @author Bruno Abdon
 */
public interface GastosoSystem {

    public List<Fato> getFatos(FiltroFatos filtro)
        throws GastosoSystemRTException, GastosoSystemException;

    public Fato getFato(int id)
        throws NotFoundException,
                GastosoSystemRTException,
                GastosoSystemException;

    public List<Conta> getContas(FiltroContas filtro)
        throws GastosoSystemRTException, GastosoSystemException;

    public Conta getConta(int id)
        throws NotFoundException,
                GastosoSystemRTException,
                GastosoSystemException;

    public List<Lancamento> getLancamentos(FiltroLancamentos fitro)
        throws GastosoSystemRTException, GastosoSystemException;

    public Fato update(Fato fato)
        throws NotFoundException,
                GastosoSystemRTException,
                GastosoSystemException;

    public Conta update(Conta conta)
        throws NotFoundException,
                GastosoSystemRTException,
                GastosoSystemException;

    public Lancamento update(Lancamento lancamento)
        throws NotFoundException,
                GastosoSystemRTException,
                GastosoSystemException;

    public void deleteFato(int id)
        throws NotFoundException,
                GastosoSystemRTException,
                GastosoSystemException;

    public void deleteConta(int id)
        throws NotFoundException,
                GastosoSystemRTException,
                GastosoSystemException;

    public void deleteLancamento(int id)
        throws NotFoundException,
                GastosoSystemRTException,
                GastosoSystemException;

    public Fato create(Fato fato)
        throws GastosoSystemRTException, GastosoSystemException;

    public Conta create(Conta conta)
        throws GastosoSystemRTException, GastosoSystemException;

    public Lancamento create(Lancamento lancamento)
        throws GastosoSystemRTException, GastosoSystemException;
}