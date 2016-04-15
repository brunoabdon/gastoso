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
package br.nom.abdon.gastoso.rest.server.dal;

import java.util.List;

import javax.persistence.EntityManager;

import br.nom.abdon.dal.DalException;
import br.nom.abdon.gastoso.Fato;
import br.nom.abdon.gastoso.Lancamento;
import br.nom.abdon.gastoso.dal.FatosDao;
import br.nom.abdon.gastoso.dal.LancamentosDao;
import br.nom.abdon.gastoso.rest.FatoDetalhado;

/**
 *
 * @author Bruno Abdon
 */
public class FatosDetalhadosDao extends FatosDao{

    private final LancamentosDao lancamentosDao = new LancamentosDao();
    
    @Override
    public Fato criar(final EntityManager em, Fato fato) throws DalException {
        return 
            (fato instanceof  FatoDetalhado)
                ? criar(em,(FatoDetalhado)fato)
                : super.criar(em, fato);
    }

    public FatoDetalhado criar(
            final EntityManager em, 
            final FatoDetalhado fatoDetalhado) throws DalException {
        
        final List<Lancamento> lancamentos = fatoDetalhado.getLancamentos();
        
        final Fato fatoCriado = super.criar(em, fatoDetalhado.asFato());
        
        final List<Lancamento> lancamentosCriados = 
            fatoDetalhado.getLancamentos();
        
        for(Lancamento lancamento : lancamentos) {
            final Lancamento lancamentoCriado = 
                lancamentosDao.criar(em, lancamento);
            lancamentosCriados.add(lancamentoCriado);
        }
        
        fatoDetalhado.setId(fatoCriado.getId());
        lancamentos.clear();
        lancamentos.addAll(lancamentosCriados);
        
        return fatoDetalhado;
    }
    
    @Override
    public void atualizar(
            final EntityManager em, 
            final Fato fato) throws DalException {
        super.atualizar(em, FatoDetalhado.asFato(fato));
    }
}
