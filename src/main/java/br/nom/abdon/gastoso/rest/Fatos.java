package br.nom.abdon.gastoso.rest;

import br.nom.abdon.rest.AbstractRestCrud;
import br.nom.abdon.gastoso.Fato;
import br.nom.abdon.rest.ValidacaoException;
import javax.ws.rs.Path;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author bruno
 */
@Path(Fatos.PATH)
public class Fatos extends AbstractRestCrud<Fato,Integer>{

    protected static final String PATH = "fatos";

    private static final String MSG_DESC_TAMANHO = 
        String.format("Descrição tem que ter no máximo %d letras",
            Fato.DESC_MAX_LEN);
    
    
    public Fatos() {
        super(Fato.class,PATH);
    }

    @Override
    protected void validarCriacao(Fato fato) throws ValidacaoException {
        
        if(StringUtils.isBlank(fato.getDescricao())){
            throw new ValidacaoException(
                "Descrição do fato é obrigatório");
        }
        
        if(fato.getDescricao().length() > Fato.DESC_MAX_LEN){
            throw new ValidacaoException(MSG_DESC_TAMANHO);
        }
        
    }
}
