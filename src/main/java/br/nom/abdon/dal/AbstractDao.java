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
package br.nom.abdon.dal;

import br.nom.abdon.modelo.Entidade;
import javax.persistence.EntityManager;

/**
 *
 * @author Bruno Abdon
 * @param <E> o tipo da entidade persistida
 * @param <K> o tipo da chave da entidade
 */
public abstract class AbstractDao<E extends Entidade<K>, K> implements Dao<E, K>{

    private final Class<E> klass;

    public AbstractDao(Class<E> klass) {
        this.klass = klass;
    }
    
    @Override
    public E find(EntityManager em, K key) throws DalException{
        E entity = em.find(klass, key);
        if(entity == null){
            throw new EntityNotFoundException(key);
        }
        return entity;
    }

    @Override
    public E criar(EntityManager em, E entity) throws DalException {
        validarPraCriacao(em,entity);
        em.persist(entity);
        return entity;
    }
    
    @Override
    public void atualizar(EntityManager em, E entity) throws DalException {
        validarPraAtualizacao(em,entity);
        em.merge(entity);
    }

    @Override
    public void deletar(EntityManager em, K key) throws DalException{
        final E entity = find(em, key);
        prepararDelecao(em,entity);
        em.remove(entity);
    }

    protected void validarPraCriacao(EntityManager em, E entity) 
            throws DalException{
        validar(em,entity);
    }

    protected void validarPraAtualizacao(EntityManager em, E entity) 
            throws DalException{
        validar(em,entity);
    }

    protected void validar(EntityManager em, E entity) throws DalException{
    };

    protected void prepararDelecao(EntityManager em, E entity) 
            throws DalException{
    };
}
