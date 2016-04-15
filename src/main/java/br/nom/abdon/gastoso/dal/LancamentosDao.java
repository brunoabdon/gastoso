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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import br.nom.abdon.dal.AbstractDao;
import br.nom.abdon.dal.DalException;
import br.nom.abdon.gastoso.Conta;
import br.nom.abdon.gastoso.Lancamento;
import br.nom.abdon.gastoso.system.FiltroContas;
import br.nom.abdon.gastoso.system.FiltroFatos;
import br.nom.abdon.gastoso.system.FiltroLancamentos;



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
    
    public List<Lancamento> listar(
        final EntityManager em, 
        final FiltroLancamentos filtro){

        final CriteriaBuilder cb = em.getCriteriaBuilder();
        
        final CriteriaQuery<Lancamento> q = cb.createQuery(Lancamento.class);
        
        final Root<Lancamento> rootLancamento = q.from(Lancamento.class);
        
        rootLancamento.fetch("conta");
        rootLancamento.fetch("fato");

        final List<Predicate> where = new LinkedList<>();
        final Map<String,Object> params = new HashMap<>();
        
        final FiltroContas filtroContas = filtro.getFiltroContas();
        if(filtroContas != null){
            
            final Conta conta = filtroContas.getConta();
            if(conta != null){
                final ParameterExpression<Conta> contaParameter = 
                    cb.parameter(Conta.class, "conta");
                
                final Path<Object> contaDoLancamento = 
                    rootLancamento.get("conta");
                
                where.add(cb.equal(contaParameter, contaDoLancamento));
                params.put("conta",conta);
            }
        }

        final FiltroFatos filtroFatos = filtro.getFiltroFatos();
        FatosDao.buildQuery(
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
            final Root rootLancamento, 
            final CriteriaBuilder cb, 
            final CriteriaQuery<Lancamento> q) {
        
        final List<FiltroLancamentos.ORDEM> ordem = filtro.getOrdem();
        if(ordem != null && !ordem.isEmpty()){
            List<Order> orders = new LinkedList<>();
            
            for(FiltroLancamentos.ORDEM itemOrdenacao : ordem) {
                final Path path;
                
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
}