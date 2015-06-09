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
