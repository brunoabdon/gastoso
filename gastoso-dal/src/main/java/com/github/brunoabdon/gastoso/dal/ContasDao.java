/*
 * Copyright (C) 2015 bruno
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

import java.util.List;
import static org.apache.commons.lang3.StringUtils.isBlank;



import com.github.brunoabdon.commons.dal.AbstractDao;
import com.github.brunoabdon.commons.dal.DalException;
import com.github.brunoabdon.gastoso.Conta;


/**
 *
 * @author bruno
 */
public class ContasDao extends AbstractDao<Conta,Integer>{
    
    public static final String ERRO_NOME_VAZIO = 
        "com.github.brunoabdon.gastoso.dal.ContasDao.NOME_VAZIO";
    public static final String ERRO_NOME_GRANDE = 
        "com.github.brunoabdon.gastoso.dal.ContasDao.NOME_GRANDE";
    public static final String ERRO_NOME_EM_USO = 
        "com.github.brunoabdon.gastoso.dal.ContasDao.NOME_EM_USO";
    public static final String ERRO_TEM_LANCAMENTOS = 
        "com.github.brunoabdon.gastoso.dal.ContasDao.TEM_LANCAMENTOS";

    public ContasDao() {
        super(Conta.class);
    }
    
    @Override
    protected void validar(final Conta conta) throws DalException {
        final String nome = conta.getNome();
        
        if(isBlank(nome)){
            throw new DalException(ERRO_NOME_VAZIO);
        }
        
        if(nome.length() > Conta.NOME_MAX_LEN){
            throw new DalException(ERRO_NOME_GRANDE);
        }
        
        final Boolean nomeEmUso =
            getEntityManager()
            .createNamedQuery("Conta.nomeEmUso", Boolean.class)
            .setParameter("nome", nome)
            .getSingleResult();
        
        if(nomeEmUso){
            throw new DalException(ERRO_NOME_EM_USO,nome);
        }
    }

    @Override
    protected void atualizarEntity(final Conta source, final Conta dest) {
        dest.setNome(source.getNome());
    }
    
    @Override
    protected void prepararDelecao(final Conta conta) throws DalException {
        
        final Boolean temLancamentos = 
            getEntityManager()
            .createNamedQuery("Conta.temLancamento", Boolean.class)
            .setParameter("conta", conta)
            .getSingleResult();
        
        if(temLancamentos){
            throw new DalException(ERRO_TEM_LANCAMENTOS);
        }
    }

    public List<Conta> listar() {
        return 
            getEntityManager()
                .createNamedQuery("Conta.all", Conta.class)
                .getResultList();
    }
}
