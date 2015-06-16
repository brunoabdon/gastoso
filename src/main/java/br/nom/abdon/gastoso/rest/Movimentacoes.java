package br.nom.abdon.gastoso.rest;

import br.nom.abdon.rest.AbstractRestCrud;
import br.nom.abdon.gastoso.Movimentacao;
import br.nom.abdon.rest.ValidacaoException;
import javax.ws.rs.Path;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author bruno
 */
@Path(Movimentacoes.PATH)
public class Movimentacoes extends AbstractRestCrud<Movimentacao,Integer>{

    protected static final String PATH = "movimentacoes";

    private static final String MSG_DESC_TAMANHO = 
        String.format(
            "Descrição tem que ter no máximo %d letras",
            Movimentacao.DESC_MAX_LEN);
    
    
    public Movimentacoes() {
        super(Movimentacao.class,PATH);
    }

    @Override
    protected void validarCriacao(Movimentacao movimentacao) throws ValidacaoException {
        
        if(StringUtils.isBlank(movimentacao.getDescricao())){
            throw new ValidacaoException(
                "Descrição da movimentação é obrigatória");
        }
        
        if(movimentacao.getDescricao().length() > Movimentacao.DESC_MAX_LEN){
            throw new ValidacaoException(MSG_DESC_TAMANHO);
        }
        
    }
    
    
    
}
