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
package com.github.brunoabdon.gastoso.dal;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.github.brunoabdon.commons.dal.AbstractDao;
import com.github.brunoabdon.commons.dal.DalException;
import com.github.brunoabdon.commons.dal.EntityNotFoundException;
import com.github.brunoabdon.gastoso.Conta;
import com.github.brunoabdon.gastoso.Lancamento;
import com.github.brunoabdon.gastoso.system.FiltroContas;
import com.github.brunoabdon.gastoso.system.FiltroFatos;
import com.github.brunoabdon.gastoso.system.FiltroLancamentos;

/**
 *
 * @author Bruno Abdon
 */
@ApplicationScoped
public class LancamentosDao extends AbstractDao<Lancamento,Lancamento.Id>{

    public static final String ERRO_FATO_VAZIO = 
        "com.github.brunoabdon.gastoso.dal.LancamentosDao.FATO_VAZIO";
    public static final String ERRO_CONTA_VAZIA = 
        "com.github.brunoabdon.gastoso.dal.LancamentosDao.CONTA_VAZIA";
    public static final String ERRO_DUPLICATA = 
        "com.github.brunoabdon.gastoso.dal.LancamentosDao.DUPLICATA";

    @Inject
    private FatosDao fatosDao;
    
    public LancamentosDao() {
        super(Lancamento.class);
    }
    
    @Override
    protected void validarPraCriacao(
            final Lancamento lancamento) throws DalException {

        validaBasico(lancamento);

        final Boolean existeDuplicata = 
            getEntityManager()
            .createNamedQuery("Lancamento.existeDuplicata",Boolean.class)
            .setParameter("fato", lancamento.getFato())
            .setParameter("conta", lancamento.getConta())
            .getSingleResult();

        if(existeDuplicata){
            throw new DalException(ERRO_DUPLICATA);
        }
    }

    private void validaBasico(final Lancamento lancamento) throws DalException{
        if(lancamento.getFato() == null){
            throw new DalException(ERRO_FATO_VAZIO);
        }
        
        if(lancamento.getConta() == null){
            throw new DalException(ERRO_CONTA_VAZIA);
        }
    }
    
    public List<Lancamento> listar(final FiltroLancamentos filtro){

        final EntityManager em = getEntityManager();
        
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        
        final CriteriaQuery<Lancamento> q = cb.createQuery(Lancamento.class);
        
        final Root<Lancamento> rootLancamento = q.from(Lancamento.class);
        
        rootLancamento.fetch("conta");
        rootLancamento.fetch("fato");

        final List<Predicate> where = new LinkedList<>();
        final Map<String,Object> params = new HashMap<>();
        
        final FiltroContas filtroContas = filtro.getFiltroContas();
        final Conta conta = filtroContas.getConta();
        if(conta != null){
            final ParameterExpression<Conta> contaParameter = 
                cb.parameter(Conta.class, "conta");

            final Path<Object> contaDoLancamento = 
                rootLancamento.get("conta");

            where.add(cb.equal(contaParameter, contaDoLancamento));
            params.put("conta",conta);
        }

        final FiltroFatos filtroFatos = filtro.getFiltroFatos();
        fatosDao.buildQuery(
            cb, filtroFatos, rootLancamento.get("fato"), where, params
        );

        trataOrdenacao(filtro, rootLancamento, cb, q);
        
        return 
            DalUtil
                .prepareAndRunQuery(
                    em, q, where, params, filtro.getPaginacao()
            );
    }

    private void trataOrdenacao(
            final FiltroLancamentos filtro, 
            final Root<?> rootLancamento, 
            final CriteriaBuilder cb, 
            final CriteriaQuery<Lancamento> q) {
        
        final List<FiltroLancamentos.ORDEM> ordem = filtro.getOrdem();
        if(ordem != null && !ordem.isEmpty()){
            final List<Order> orders = new LinkedList<>();
            
            for(final FiltroLancamentos.ORDEM itemOrdenacao : ordem) {
                final Path<?> path;
                
                switch(itemOrdenacao){
                    case POR_CONTA_ID_ASC:
                    case POR_CONTA_ID_DESC:
                        path = rootLancamento.get("conta").get("id");
                        break;
                    case POR_CONTA_NOME_ASC:
                    case POR_CONTA_NOME_DESC:
                        path = rootLancamento.get("conta").get("nome");
                        break;
                    case POR_FATO_ID_ASC:
                    case POR_FATO_ID_DESC:
                        path = rootLancamento.get("fato").get("id");
                        break;
                    case POR_DESC_FATO_ASC:
                    case POR_DESC_FATO_DESC:
                        path = rootLancamento.get("fato").get("desc");
                        break;
                    case POR_DIA_ASC:
                    case POR_DIA_DESC:
                        path = rootLancamento.get("fato").get("dia");
                        break;
//                        case POR_VALOR_ASC:
//                        case POR_VALOR_DESC:
                    default:
                        path = rootLancamento.get("valor");
                        break;
                }
                
                final Order order = 
                    itemOrdenacao.isAsc()
                        ? cb.asc(path)
                        : cb.desc(path);
                
                orders.add(order);
            }
            
            q.orderBy(orders);
        }
    }
 
    @Override
    protected void atualizarEntity(
            final Lancamento source, 
            final Lancamento dest) {
        dest.setValor(source.getValor());
    }
    
    public Lancamento findUnique(final FiltroLancamentos filtro) 
            throws DalException{
        
        final Lancamento lancamento;
        
        final List<Lancamento> col = this.listar(filtro);
        if(col.isEmpty()){
            throw new EntityNotFoundException(filtro);
        } else if(col.size() != 1){
            throw new DalException("Not Unique");
        } else {
            lancamento = col.get(0);
        }
        
        return lancamento;
    }
}