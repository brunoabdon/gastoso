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
public interface Dao<E extends Entidade<K>,K> {
    
    public E find(EntityManager em, K key) throws DalException;
    
    public E criar(final EntityManager em, E entity) throws DalException;
    
    public void atualizar(final EntityManager em, E entity) throws DalException;
    
    public void deletar(final EntityManager em, K key) throws DalException;
    
    
}
