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
package br.nom.abdon.gastoso.dal;

import br.nom.abdon.dal.AbstractDao;
import br.nom.abdon.dal.DalException;
import br.nom.abdon.gastoso.Conta;
import java.util.List;
import javax.persistence.EntityManager;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author bruno
 */
public class ContasDao extends AbstractDao<Conta,Integer>{
    
    public static final String ERRO_NOME_VAZIO = "br.nom.abdon.gastoso.dal.ContasDao.NOME_VAZIO";
    public static final String ERRO_NOME_GRANDE = "br.nom.abdon.gastoso.dal.ContasDao.NOME_GRANDE";
    public static final String ERRO_NOME_EM_USO = "br.nom.abdon.gastoso.dal.ContasDao.NOME_EM_USO";
    public static final String ERRO_TEM_LANCAMENTOS = "br.nom.abdon.gastoso.dal.ContasDao.TEM_LANCAMENTOS";

    public ContasDao() {
        super(Conta.class);
    }
    
    @Override
    protected void validar(EntityManager em, Conta conta) throws DalException {
        final String nome = conta.getNome();
        
        if(StringUtils.isBlank(nome)){
            throw new DalException(ERRO_NOME_VAZIO);
        }
        
        if(nome.length() > Conta.NOME_MAX_LEN){
            throw new DalException(ERRO_NOME_GRANDE);
        }
        
        final Boolean nomeEmUso =
            em.createNamedQuery("Conta.nomeEmUso", Boolean.class)
            .setParameter("nome", nome)
            .getSingleResult();
        
        if(nomeEmUso){
            throw new DalException(ERRO_NOME_EM_USO,nome);
        }
    }

    @Override
    protected void prepararDelecao(EntityManager em, Conta conta) throws DalException {
        Boolean temLancamentos = 
            em.createNamedQuery("Conta.temLancamento", Boolean.class)
            .setParameter("conta", conta)
            .getSingleResult();
        
        if(temLancamentos){
            throw new DalException(ERRO_TEM_LANCAMENTOS);
        }
    }

    public List<Conta> listar(final EntityManager em) {
        return em.createNamedQuery("Conta.all", Conta.class).getResultList();
    }
}
