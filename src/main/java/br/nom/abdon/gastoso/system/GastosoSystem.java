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
import br.nom.abdon.gastoso.Fato;
import br.nom.abdon.gastoso.Lancamento;

import java.util.List;

/**
 *
 * @author Bruno Abdon
 */
public interface GastosoSystem {

    public boolean login(final String user, String password)
        throws GastosoSystemRTException;

    public boolean logout() throws GastosoSystemRTException, IllegalAccessError;

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

    public List<Lancamento> getLancamentos(FiltroLancamento fitro)
        throws GastosoSystemRTException, GastosoSystemException;

    public void update(Fato fato)
        throws NotFoundException,
                GastosoSystemRTException,
                GastosoSystemException;

    public void update(Conta conta)
        throws NotFoundException,
                GastosoSystemRTException,
                GastosoSystemException;

    public void update(Lancamento lancamento)
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