/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.nom.abdon.gastoso.rest;

import br.nom.abdon.gastoso.Lancamento;
import br.nom.abdon.rest.AbstractRestCrud;
import javax.ws.rs.Path;

/**
 *
 * @author bruno
 */
@Path("movimentacoes/{idMov}/lancamentos")
public class Lancamentos extends AbstractRestCrud<Lancamento, Integer>{

    public Lancamentos() {
        super(Lancamento.class);
    }
}
