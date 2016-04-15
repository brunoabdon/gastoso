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
package br.nom.abdon.gastoso.aggregate.dal;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import br.nom.abdon.dal.DalException;
import br.nom.abdon.gastoso.Conta;
import br.nom.abdon.gastoso.Lancamento;
import br.nom.abdon.gastoso.aggregate.Saldo;
import br.nom.abdon.gastoso.dal.DalUtil;

/**
 *
 * @author Bruno Abdon
 */
public class AggregateDao {
    
    private static final Function<LocalDate,Function<Tuple,Saldo>> SALDO_CONS = 
        (dia) -> (t) -> 
            new Saldo(
                new Conta(t.get(0,Integer.class),t.get(1,String.class)), 
                dia, 
                t.get(2,Long.class));
    
    public Saldo findSaldo(
            final EntityManager em, 
            Conta conta, 
            final LocalDate dia) 
        throws DalException{
        
        final List<Tuple> resultList = 
            em.createNamedQuery("Aggregate.SaldoDaContaNoDia", Tuple.class)
                .setParameter("conta", conta)
                .setParameter("dia", dia)
                .getResultList();

        if(resultList.isEmpty()){
            throw new EntityNotFoundException();
        }
        
       final Tuple tupla = resultList.get(0);
        
        final int idConta = tupla.get(0,Integer.class);
        final String nomeConta = tupla.get(1, String.class);
        final long valor = tupla.get(2,Long.class);
        
        return new Saldo(new Conta(idConta,nomeConta), dia, valor);
        
    }
    
    public List<Saldo> list(
            final EntityManager em, 
            final FiltroSaldo filtro)
        throws DalException{
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        
        final CriteriaQuery<Tuple> q = cb.createTupleQuery();
        
        final Root<Lancamento> rootLancamento = q.from(Lancamento.class);
        
        final Path<Object> contaDoLancamento = rootLancamento.get("conta");        
        
        final List<Predicate> where = new LinkedList<>();
        final Map<String,Object> params = new HashMap<>();
        
        final Conta conta = filtro.getConta();
        if(conta != null){
            final ParameterExpression<Conta> contaParameter = 
                cb.parameter(Conta.class, "conta");

            where.add(cb.equal(contaParameter, contaDoLancamento));
            params.put("conta",conta);
        }
        
        
        final LocalDate dia = filtro.getDia();
        if(dia != null){
            final Path<LocalDate> diaPath = 
                rootLancamento.get("fato").get("dia");

            final ParameterExpression<LocalDate> dataMaximaParameter =
                cb.parameter(LocalDate.class, "dia");

            final Predicate menorOuIgualDia =
                cb.lessThanOrEqualTo(diaPath, dataMaximaParameter);

            where.add(menorOuIgualDia);
            params.put("dia", dia);
        }
        
        if(!where.isEmpty()) { q.where(where.toArray(new Predicate[]{}));};

        q.orderBy(buildOrdenacao(filtro, rootLancamento, cb));

        final Path<Object> idConta = contaDoLancamento.get("id");
        final Path<Object> nomeConta = contaDoLancamento.get("nome");
        final Expression<Number> sumValores = 
            cb.sum(rootLancamento.get("valor"));
        
        q.select(cb.tuple(idConta,nomeConta,sumValores));
        
        q.groupBy(idConta,nomeConta);
        
        return
            DalUtil.prepareAndRunQuery(em,q,where,params,filtro.getPaginacao())
            .stream()
            .map(SALDO_CONS.apply(dia))
            .collect(Collectors.toList());
    }

    private List<Order> buildOrdenacao(
            final FiltroSaldo filtro, 
            final Root<?> root, 
            final CriteriaBuilder cb) {
        
        final List<Order> orders;
        
        final List<FiltroSaldo.ORDEM> ordem = filtro.getOrdem();
        if(ordem == null || ordem.isEmpty()){
            orders = Collections.emptyList();
        } else {
            orders = new LinkedList<>();
            
            for(FiltroSaldo.ORDEM itemOrdenacao : ordem) {
                final Path path;
                
                switch(itemOrdenacao){
                    case POR_CONTA:
                        path = root.get("conta").get("nome");
                        break;
                    case POR_DIA:
                        path = root.get("fato").get("dia");
                        break;
                    //case POR_VALOR:
                    default:
                        path = root.get("valor");
                        break;
                }
                
                orders.add(cb.asc(path));
            }
        }
        return orders;
    }
}
