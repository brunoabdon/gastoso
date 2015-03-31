/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package br.nom.abdon.gastoso.rest;

import br.nom.abdon.gastoso.Conta;
import java.util.Collection;
import java.util.LinkedList;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author bruno
 */
@Path("gastoso")
@Produces(MediaType.APPLICATION_JSON)
public class GastosoRest {

    
    public static Collection<Conta> contas = new LinkedList<>();
    private static int inc = 0;
    
    
    @POST
    @Path("/contas")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Conta criarConta(Conta conta){
        System.out.println(conta);
        conta.setId(inc++);
        contas.add(conta);
        return conta;
    }
    
    
    @GET
    @Path("/contas")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Conta> contas(){
        return contas;
    }
}
