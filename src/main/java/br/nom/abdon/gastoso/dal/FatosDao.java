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

import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;

import br.nom.abdon.gastoso.system.FiltroFatos;

/**
 *
 * @author Bruno Abdon
 */
public class FatosDao extends AbstractDao<Fato,Integer>{

    public static final String ERRO_DIA_VAZIO = "br.nom.abdon.gastoso.dal.FatosDao.DIA_VAZIO";
    public static final String ERRO_DESCRICAO_VAZIA = "br.nom.abdon.gastoso.dal.FatosDao.DESCRICAO_VAZIA";
    public static final String ERRO_DESCRICAO_GRANDE = "br.nom.abdon.gastoso.dal.FatosDao.DESCRICAO_GRANDE";

    public FatosDao() {
        super(Fato.class);
    }
    
    @Override
    protected void validar(EntityManager em, Fato fato) throws DalException {
        if(fato.getDia() == null){
            throw new DalException(ERRO_DIA_VAZIO);
        }
        final String descricao = fato.getDescricao();
        
        if(StringUtils.isBlank(descricao)){
            throw new DalException(ERRO_DESCRICAO_VAZIA);
        }
        
        if(descricao.length() > Fato.DESC_MAX_LEN){
            throw new DalException(ERRO_DESCRICAO_GRANDE,descricao);
        }
    }

    @Override
    protected void prepararDelecao(EntityManager em, Fato fato) throws DalException {
        em.createNamedQuery("Lancamento.deletarPorFato")
            .setParameter("fato", fato)
            .executeUpdate();
    }
    
    public List<Fato> listar(
        final EntityManager em, 
        final FiltroFatos filtroFatos){

        final CriteriaBuilder cb = em.getCriteriaBuilder();
        
        final CriteriaQuery<Fato> q = cb.createQuery(Fato.class);
        
        final Root<Fato> root = q.from(Fato.class);

        final List<Predicate> where = new LinkedList<>();
        final Map<String,Object> params = new HashMap<>();
        
        buildQuery(cb, filtroFatos, root, where, params);
        
        trataOrdenacao(filtroFatos, root, cb, q);
        
        if(!where.isEmpty()) { q.where(where.toArray(new Predicate[]{}));};

        final TypedQuery<Fato> query = em.createQuery(q);
        
        DalUtil.tratarPaginacao(filtroFatos.getPaginacao(), query);

        DalUtil.trataParams(params, query);
        
        return query.getResultList();
    }

    protected static void buildQuery(
            final CriteriaBuilder cb, 
            final FiltroFatos filtroFatos,
            final Path<Fato> fatoPath,
            final List<Predicate> where,
            final Map<String, Object> params) {
        
        final Fato fato = filtroFatos.getFato();
        if(fato != null){
            final ParameterExpression<Fato> fatoParameter = 
                cb.parameter(Fato.class, "fato");

            where.add(cb.equal(fatoParameter, fatoPath));
            params.put("fato",fato);

        }

        final LocalDate dataMaxima = filtroFatos.getDataMaxima();
        if(dataMaxima != null){
            final Path<LocalDate> diaPath = fatoPath.get("dia");

            final ParameterExpression<LocalDate> dataMaximaParameter =
                cb.parameter(LocalDate.class, "dataMaxima");

            final Predicate menorOuIgualDataMaxima =
                cb.lessThanOrEqualTo(diaPath, dataMaximaParameter);

            where.add(menorOuIgualDataMaxima);
            params.put("dataMaxima", dataMaxima);
        }

        final LocalDate dataMinima = filtroFatos.getDataMinima();
        if(dataMinima != null){
            final Path<LocalDate> diaPath = fatoPath.get("dia");

            final ParameterExpression<LocalDate> dataMinimaParameter =
                cb.parameter(LocalDate.class, "dataMinima");

            final Predicate maiorOuIgualQueDataMinima =
                cb.greaterThanOrEqualTo(diaPath, dataMinimaParameter);

            where.add(maiorOuIgualQueDataMinima);
            params.put("dataMinima", dataMinima);
        }
    }
 
    public List<Fato> listar(
        final EntityManager em, 
        final Conta conta, 
        final LocalDate dataMaxima,
        final int quantidadeMaxima){
        
        return em.createNamedQuery("Fato.porContaPeriodo",Fato.class)
                .setParameter("conta", conta)
                .setParameter("dataMaxima", dataMaxima)
                .setMaxResults(quantidadeMaxima)
                .getResultList();
    }

    private void trataOrdenacao(FiltroFatos filtroFatos, Root<Fato> root, CriteriaBuilder cb, CriteriaQuery<Fato> q) {
        //...
    } 
}