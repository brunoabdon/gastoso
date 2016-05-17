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
package br.nom.abdon.gastoso.rest.server.dal;

import java.util.List;
import java.util.logging.Logger;

import javax.persistence.EntityManager;

import br.nom.abdon.dal.DalException;
import br.nom.abdon.gastoso.Fato;
import br.nom.abdon.gastoso.Lancamento;
import br.nom.abdon.gastoso.dal.FatosDao;
import br.nom.abdon.gastoso.dal.LancamentosDao;

import br.nom.abdon.gastoso.ext.FatoDetalhado;
import br.nom.abdon.gastoso.system.FiltroLancamentos;
import static br.nom.abdon.gastoso.system.FiltroLancamentos.ORDEM.POR_CONTA_ID_ASC;

/**
 *
 * @author Bruno Abdon
 */
public class FatosDetalhadosDao extends FatosDao{

    private static final Logger LOG = 
        Logger.getLogger(FatosDetalhadosDao.class.getName());
    
    private final LancamentosDao lancamentosDao = new LancamentosDao();
    
    @Override
    public void criar(final EntityManager em, Fato fato) throws DalException {
        if(fato instanceof  FatoDetalhado){
            criar(em,(FatoDetalhado)fato);
        } else {
            super.criar(em, fato);
        }
    }

    public void criar(
            final EntityManager em, 
            final FatoDetalhado fatoDetalhado) throws DalException {
        
        final Fato fato = fatoDetalhado.asFato();
        
        super.criar(em, fato);
        
        fatoDetalhado.setId(fato.getId()); //importante
        fatoDetalhado.setDia(fato.getDia()); //just in case
        fatoDetalhado.setDescricao(fato.getDescricao()); //just in case
        
        final List<Lancamento> lancamentos = fatoDetalhado.getLancamentos();
        
        for(Lancamento lancamento : lancamentos) {
            lancamento.setFato(fato);
            lancamentosDao.criar(em, lancamento);
        }
    }
    
    @Override
    public Fato atualizar(
            final EntityManager em, 
            final Fato fato) throws DalException {
        
        if(fato instanceof FatoDetalhado){
            FatoDetalhado fatoDetalhado = (FatoDetalhado)fato;
            
            final FiltroLancamentos flanc = new FiltroLancamentos();
            flanc.getFiltroFatos().setFato(fato);
            flanc.addOrdem(POR_CONTA_ID_ASC);

            final List<Lancamento> lancamentosBanco = lancamentosDao.listar(em, flanc);
            
            final List<Lancamento> lancamentos = fatoDetalhado.getLancamentos();
            lancamentos.sort(
                (l1,l2) -> l1.getConta().getId()
                            .compareTo(l2.getConta().getId()));
            
            int j = 0;
            int i = 0;
            
            while(i < lancamentos.size()
                    || j < lancamentosBanco.size()) {

                if(j >= lancamentosBanco.size()){
                    lancamentosDao.criar(em, lancamentos.get(i++));
                } else if(i >= lancamentos.size()) {
                    lancamentosDao.deletar(em, lancamentosBanco.get(j++).getId());
                } else {
                    final Lancamento l = lancamentos.get(i);
                    final int idConta = l.getConta().getId();

                    final Lancamento lancamentoBanco = lancamentosBanco.get(j);
                    final int idContaBanco = 
                        lancamentoBanco.getConta().getId();

                    LOG.finest(() -> "\tAnalizando " + idContaBanco);

                    if(idConta < idContaBanco){
                        lancamentosDao.criar(em, l);
                        i++;
                    } else if(idConta == idContaBanco){
                        if(l.getValor() != lancamentoBanco.getValor()){
                            lancamentoBanco.setValor(l.getValor());
                            lancamentosDao.atualizar(em, lancamentoBanco);
                        }
                        i++;
                        j++;
                    } else {
                        lancamentosDao.deletar(em, lancamentoBanco.getId());
                        j++;
                    }
                }
            }
        }
        
        return super.atualizar(em, FatoDetalhado.asFato(fato));
    }
}
