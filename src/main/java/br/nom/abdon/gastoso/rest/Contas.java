package br.nom.abdon.gastoso.rest;

import br.nom.abdon.rest.AbstractRestCrud;
import br.nom.abdon.gastoso.Conta;
import br.nom.abdon.gastoso.Lancamento;
import br.nom.abdon.gastoso.Lancamento_;
import br.nom.abdon.rest.ExclusaoException;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author bruno
 */
@Path(Contas.PATH)
@Produces(MediaType.APPLICATION_JSON)
public class Contas extends AbstractRestCrud<Conta,Integer>{

    static final String PATH = "contas";
    
    public Contas() {
        super(Conta.class,PATH);
    }

    @Override
    protected void validarExclusao(EntityManager entityManager, Conta conta) 
        throws ExclusaoException {

        if(existemLancamentosPraAConta(entityManager, conta)){
            throw new ExclusaoException("Existem contas");
        }
        
        
    }

    private boolean existemLancamentosPraAConta(EntityManager entityManager, Conta conta) {

        final Metamodel metamodel = entityManager.getMetamodel();
        final EntityType<Lancamento> lancamentoMetamodel =
            metamodel.entity(Lancamento.class);
        
        final CriteriaBuilder criteriaBuilder =
            entityManager.getCriteriaBuilder();
        
        final CriteriaQuery<Long> cq =
            criteriaBuilder.createQuery(Long.class);
        
        final Root<Lancamento> rootLancamento = cq.from(lancamentoMetamodel);
        
        cq.select(criteriaBuilder.count(rootLancamento));
        
        final Predicate predicado =
            criteriaBuilder.equal(
                rootLancamento.get(Lancamento_.conta), conta);
        
        cq.where(predicado);
        
        final TypedQuery<Long> tq = entityManager.createQuery(cq);
        
        Long quantos = tq.getSingleResult();
        
        return quantos != 0;
    }
    
}