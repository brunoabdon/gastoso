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
package br.nom.abdon.gastoso.dal;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;

import br.nom.abdon.gastoso.system.Paginacao;

/**
 *
 * @author Bruno Abdon
 */
public class DalUtil {

    private static void tratarPaginacao(
            final Paginacao paginacao, 
            final TypedQuery query) {

        final Integer inicio = paginacao.getPrimeiro();
        final Integer quantos = paginacao.getQuantidadeMaxima();
        
        if(inicio != null) query.setFirstResult(inicio);
        if(quantos != null)query.setMaxResults(quantos);
    }
    
    private static void trataParams(
            final Map<String, Object> params, 
            final TypedQuery query) {
        
        params.entrySet().stream().forEach(
            (entry) -> query.setParameter(entry.getKey(), entry.getValue())
        );
    }
    
    public static <X> List<X> prepareAndRunQuery(
            final EntityManager em, 
            final CriteriaQuery<X> q, 
            final List<Predicate> where, 
            final Map<String, Object> params,
            final Paginacao paginacao) {

        if(!where.isEmpty()) { q.where(where.toArray(new Predicate[]{}));};
        
        final TypedQuery<X> query = em.createQuery(q);
        
        DalUtil.tratarPaginacao(paginacao, query);

        DalUtil.trataParams(params, query);
        
        return query.getResultList();
    }
}
