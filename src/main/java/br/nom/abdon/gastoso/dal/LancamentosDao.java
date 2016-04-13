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

import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Parameter;
import javax.persistence.TypedQuery;
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
import br.nom.abdon.gastoso.Fato;
import br.nom.abdon.gastoso.Lancamento;
import br.nom.abdon.gastoso.system.FiltroContas;
import br.nom.abdon.gastoso.system.FiltroFatos;
import br.nom.abdon.gastoso.system.FiltroLancamentos;
import br.nom.abdon.gastoso.system.Paginacao;



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
        
        FiltroContas filtroContas = filtro.getFiltroContas();
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
        
        if(filtroFatos != null){
            
            final Fato fato = filtroFatos.getFato();
            if(fato != null){
                final ParameterExpression<Fato> fatoParameter = 
                    cb.parameter(Fato.class, "fato");
            
                final Path<Fato> fatoDoLancamento = rootLancamento.get("fato");
            
                where.add(cb.equal(fatoParameter, fatoDoLancamento));
                params.put("fato",fato);

            }

            final LocalDate dataMaxima = filtroFatos.getDataMaxima();
            if(dataMaxima != null){
                final Path<LocalDate> diaPath = 
                    rootLancamento.get("fato").get("dia");
                
                final ParameterExpression<LocalDate> dataMaximaParameter =
                    cb.parameter(LocalDate.class, "dataMaxima");
                
                final Predicate menorOuIgualDataMaxima =
                    cb.lessThanOrEqualTo(diaPath, dataMaximaParameter);
                
                where.add(menorOuIgualDataMaxima);
                params.put("dataMaxima", dataMaxima);
                
            }
            
            final LocalDate dataMinima = filtroFatos.getDataMinima();
            if(dataMinima != null){
                final Path<LocalDate> diaPath = 
                    rootLancamento.get("fato").get("dia");

                final ParameterExpression<LocalDate> dataMinimaParameter =
                    cb.parameter(LocalDate.class, "dataMinima");

                final Predicate maiorOuIgualQueDataMinima =
                    cb.greaterThanOrEqualTo(diaPath, dataMinimaParameter);
                
                where.add(maiorOuIgualQueDataMinima);
                params.put("dataMinima", dataMinima);
            }
        
        }
        
        if(!where.isEmpty()) { q.where(where.toArray(new Predicate[]{}));};

        trataOrdenacao(filtro, rootLancamento, cb, q);
        
        final TypedQuery<Lancamento> query = em.createQuery(q);
        
        final Paginacao paginacao = filtro.getPaginacao();
        
        final Integer inicio = paginacao.getPrimeiro();
        final Integer quantos = paginacao.getQuantidadeMaxima();
        
        if(inicio != null) query.setFirstResult(inicio);
        if(quantos != null)query.setMaxResults(quantos);

        params.entrySet().stream().forEach((entry) -> {
            final String paramName = entry.getKey();
            final Object paramValue = entry.getValue();
            
            System.out.printf("filtrando %s: %s\n",paramName,paramValue);
            
            query.setParameter(paramName, paramValue);
        });
        return query.getResultList();
    }

    private void trataOrdenacao(
            final FiltroLancamentos filtro, 
            final Root<Lancamento> rootLancamento, 
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
}