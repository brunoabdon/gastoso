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
import br.nom.abdon.gastoso.Fato;
import java.time.LocalDate;
import java.util.List;
import javax.persistence.EntityManager;
import org.apache.commons.lang3.StringUtils;

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
        final LocalDate dataMinima, 
        final LocalDate dataMaxima){
        
        return em.createNamedQuery("Fato.porPeriodo",Fato.class)
                .setParameter("dataMinima", dataMinima)
                .setParameter("dataMaxima", dataMaxima)
                .getResultList();
    }
}