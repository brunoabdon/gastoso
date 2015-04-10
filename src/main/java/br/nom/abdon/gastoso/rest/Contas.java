/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package br.nom.abdon.gastoso.rest;

import br.nom.abdon.rest.AbstractRestCrud;
import br.nom.abdon.gastoso.Conta;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author bruno
 */
@Path("contas")
@Produces(MediaType.APPLICATION_JSON)
public class Contas extends AbstractRestCrud<Conta,Integer>{

    public Contas() {
        super(Conta.class);
    }

}