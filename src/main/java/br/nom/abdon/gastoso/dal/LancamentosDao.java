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
package br.nom.abdon.gastoso.dal;

import br.nom.abdon.dal.AbstractDao;
import br.nom.abdon.dal.DalException;
import br.nom.abdon.gastoso.Conta;
import br.nom.abdon.gastoso.Fato;
import br.nom.abdon.gastoso.Lancamento;
import java.time.LocalDate;
import java.util.List;
import javax.persistence.EntityManager;

/**
 *
 * @author Bruno Abdon
 */
public class LancamentosDao extends AbstractDao<Lancamento,Integer>{

    public static final String ERRO_FATO_VAZIO = "br.nom.abdon.gastoso.dal.LancamentosDao.FATO_VAZIO";
    public static final String ERRO_CONTA_VAZIA = "br.nom.abdon.gastoso.dal.LancamentosDao.CONTA_VAZIA";
    public static final String ERRO_DUPLICATA = "br.nom.abdon.gastoso.dal.LancamentosDao.DUPLICATA";

    public LancamentosDao() {
        super(Lancamento.class);
    }
    
    @Override
    protected void validarPraCriacao(EntityManager em, Lancamento lancamento) throws DalException {
        validaBasico(lancamento);
        
        final Boolean existeDuplicata = 
            em.createNamedQuery("Lancamento.existeDuplicata",Boolean.class)
            .setParameter("fato", lancamento.getFato())
            .setParameter("conta", lancamento.getConta())
            .getSingleResult();
        
        if(existeDuplicata){
            throw new DalException(ERRO_DUPLICATA);
        }
    }

    @Override
    protected void validarPraAtualizacao(EntityManager em, Lancamento lancamento) throws DalException {
        validaBasico(lancamento);
    }

    private void validaBasico(Lancamento lancamento) throws DalException{
        if(lancamento.getFato() == null){
            throw new DalException(ERRO_FATO_VAZIO);
        }
        
        if(lancamento.getConta() == null){
            throw new DalException(ERRO_CONTA_VAZIA);
        }
    }
    
    public boolean existe(final EntityManager em, final Conta conta){
        return em.createNamedQuery("Conta.temLancamento", Boolean.class)
                .setParameter("conta", conta)
                .getSingleResult();
    }
    
    public List<Lancamento> listar(final EntityManager em, final Fato fato){
        return em.createNamedQuery("Lancamento.porFato", Lancamento.class)
                .setParameter("fato", fato)
                .getResultList();
    }
    
    public long valorTotal(
            final EntityManager em, 
            final Conta conta, 
            final LocalDate dataFinal){
        
        final Long result = 
            em.createNamedQuery("Lancamento.totalDaContaEm", Long.class)
            .setParameter("conta", conta)
            .setParameter("dataFinal", dataFinal)
            .getResultList()
            .get(0);
        
        return result == null ? 0 : result;
    }

    public long valorTotalAte(
            final EntityManager em, 
            final Lancamento lancamento){
        
        final Long result = 
            em.createNamedQuery("Lancamento.saldoAnterior", Long.class)
            .setParameter("lancamento", lancamento)
            .getResultList()
            .get(0);
        
        return result == null ? 0 : result;
    }
    
    
    public List<Lancamento> listar(
            final EntityManager em, 
            final Conta conta, 
            final LocalDate dataMinima, 
            final LocalDate dataMaxima){
        
        return em.createNamedQuery("Lancamento.porContaPeriodo", Lancamento.class)
                .setParameter("conta", conta)
                .setParameter("dataMin", dataMinima)
                .setParameter("dataMax", dataMaxima)
                .getResultList();
    }
}
