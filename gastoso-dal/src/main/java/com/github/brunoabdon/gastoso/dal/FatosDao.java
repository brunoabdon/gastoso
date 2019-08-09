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

import com.github.brunoabdon.commons.dal.AbstractDao;
import com.github.brunoabdon.commons.dal.DalException;
import com.github.brunoabdon.gastoso.Fato;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;

import com.github.brunoabdon.gastoso.system.FiltroFatos;

/**
 *
 * @author Bruno Abdon
 */
@ApplicationScoped
public class FatosDao extends AbstractDao<Fato,Integer>{

    public static final String ERRO_DIA_VAZIO = 
        "com.github.brunoabdon.gastoso.dal.FatosDao.DIA_VAZIO";
    public static final String ERRO_DESCRICAO_VAZIA = 
        "com.github.brunoabdon.gastoso.dal.FatosDao.DESCRICAO_VAZIA";
    public static final String ERRO_DESCRICAO_GRANDE = 
        "com.github.brunoabdon.gastoso.dal.FatosDao.DESCRICAO_GRANDE";

    public FatosDao() {
        super(Fato.class);
    }

    /**
     * Considera válido um {@link Fato} que denha um {@linkplain Fato#getDia() 
     * dia} não {@code null}, uma {@linkplain Fato#getDescricao() descrição}
     * não {@linkplain StringUtils#isBlank(CharSequence) nula nem vazia} e com
     * menos de que {@linkplain Fato#DESC_MAX_LEN a quantidade máxima de 
     * caracteres permitida}.
     * 
     * @param fato O {@link Fato} a ser validado.
     * @throws DalException caso o fato não seja válido.  
     */
    @Override
    protected void validar(final Fato fato) throws DalException {
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
    protected void atualizarEntity(final Fato source, final Fato dest) {
        dest.setDescricao(source.getDescricao());
        dest.setDia(source.getDia());
    }
    
    @Override
    protected void prepararDelecao(final Fato fato) throws DalException {
        this.getEntityManager()
            .createNamedQuery("Lancamento.deletarPorFato")
            .setParameter("fato", fato)
            .executeUpdate();
    }
    
    public List<Fato> listar(
        final FiltroFatos filtroFatos){

        final EntityManager em = getEntityManager();
        
        final CriteriaBuilder cb = em .getCriteriaBuilder();
        
        final CriteriaQuery<Fato> q = cb.createQuery(Fato.class);
        
        final Root<Fato> root = q.from(Fato.class);

        final List<Predicate> where = new LinkedList<>();
        final Map<String,Object> params = new HashMap<>();
        
        buildQuery(cb, filtroFatos, root, where, params);
        
        trataOrdenacao(filtroFatos, root, cb, q);
        
        return DalUtil
                .prepareAndRunQuery(
                    em, 
                    q, 
                    where, 
                    params, 
                    filtroFatos.getPaginacao());
    }

    protected void buildQuery(
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
 
    private void trataOrdenacao(
        final FiltroFatos filtro, 
        final Root<Fato> root, 
        final CriteriaBuilder cb, 
        final CriteriaQuery<Fato> q) {

        final List<FiltroFatos.ORDEM> ordem = filtro.getOrdem();
        if(ordem != null && !ordem.isEmpty()){
            List<Order> orders = new LinkedList<>();
            
            for(FiltroFatos.ORDEM itemOrdenacao : ordem) {
                final Path<?> path;
                
                switch(itemOrdenacao){
                    case POR_CRIACAO:
                        path = root.get("id");
                        break;
                    case POR_DESCRICAO:
                        path = root.get("descricao");
                        break;
                    case POR_DIA:
                    default:
                        path = root.get("dia");
                        break;
                }
                
                orders.add(cb.asc(path));
            }
            
            q.orderBy(orders);
        }
    } 
}