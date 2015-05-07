/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.nom.abdon.gastoso.rest;

import br.nom.abdon.rest.AbstractRestCrud;
import br.nom.abdon.gastoso.Movimentacao;
import javax.ws.rs.Path;

/**
 *
 * @author bruno
 */
@Path(Movimentacoes.PATH)
public class Movimentacoes extends AbstractRestCrud<Movimentacao,Integer>{

    protected static final String PATH = "movimentacoes";
    
    public Movimentacoes() {
        super(Movimentacao.class,PATH);
    }
    
}
