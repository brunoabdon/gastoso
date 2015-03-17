package br.nom.abdon;

import br.nom.abdon.gastoso.Conta;
import br.nom.abdon.gastoso.Lancamento;
import br.nom.abdon.gastoso.Movimentacao;
import java.util.LinkedList;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("myresource")
public class MyResource {

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public String getIt() {
        return "Hello, Heroku!";
    }
    
    @GET @Path("/jax")
    @Produces(MediaType.APPLICATION_JSON)
    public Movimentacao getMyBean() {
        
        final int valorSaque = 20;
        
        Conta bb = new Conta();
        bb.setNome("Banco do Brasil");
        bb.setId(1);
        Lancamento l1 = new Lancamento();
        l1.setId(0);
        l1.setConta(bb);
        l1.setValor(-valorSaque);
        

        Conta carteira = new Conta();
        carteira.setNome("Carteira");
        carteira.setId(2);
        Lancamento l2 = new Lancamento();
        l2.setId(1);
        l2.setConta(bb);
        l2.setValor(+valorSaque);

        LinkedList<Lancamento> lancamentos = new LinkedList<>();
        lancamentos.add(l1);
        lancamentos.add(l2);
        
        Movimentacao saque = new Movimentacao();
//        saque.setData(LocalDate.now());
        saque.setDescricao("Saque");
        saque.setLancamentos(lancamentos);
        
        return saque;
    }
}
