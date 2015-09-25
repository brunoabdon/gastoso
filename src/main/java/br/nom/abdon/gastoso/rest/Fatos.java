package br.nom.abdon.gastoso.rest;

import br.nom.abdon.rest.AbstractRestCrud;
import br.nom.abdon.gastoso.Fato;
import br.nom.abdon.gastoso.Fato_;
import br.nom.abdon.gastoso.Lancamento;
import br.nom.abdon.gastoso.Lancamento_;
import br.nom.abdon.rest.ExclusaoException;
import br.nom.abdon.rest.ValidacaoException;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
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

    @Override
    protected void deletaDependencias(
        final EntityManager entityManager, 
        final Fato fato) throws ExclusaoException {

        final Metamodel metamodel = entityManager.getMetamodel();
        final EntityType<Lancamento> lancamentoMetamodel = 
            metamodel.entity(Lancamento.class);
        
        final CriteriaBuilder criteriaBuilder = 
            entityManager.getCriteriaBuilder();
        
        final CriteriaQuery<Lancamento> cq = 
            criteriaBuilder.createQuery(Lancamento.class);
        
        final Root<Lancamento> rootLancamento = cq.from(lancamentoMetamodel);
        final Join<Lancamento,Fato> join = 
            rootLancamento.join(Lancamento_.fato);
        
        cq.orderBy(criteriaBuilder.asc(join.get(Fato_.dia)));
        
        cq.select(rootLancamento);

        final Predicate predicado = 
            criteriaBuilder.equal(rootLancamento.get(Lancamento_.fato), fato);
        
        cq.where(predicado);

        final TypedQuery<Lancamento> tq = entityManager.createQuery(cq);
        
        final List<Lancamento> lancamentos = tq.getResultList();
            
        for (Lancamento lancamento : lancamentos) {
            entityManager.remove(lancamento);
        }
    }
}
