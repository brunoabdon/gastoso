package br.nom.abdon.gastoso.rest;

import br.nom.abdon.gastoso.Lancamento;
import br.nom.abdon.gastoso.Conta;
import br.nom.abdon.gastoso.Fato;
import br.nom.abdon.gastoso.Fato_;
import br.nom.abdon.gastoso.Lancamento_;
import br.nom.abdon.rest.AbstractRestCrud;
import java.util.List;
import java.util.function.BiFunction;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author bruno
 */
@Path(Lancamentos.PATH)
public class Lancamentos extends AbstractRestCrud<Lancamento, Integer> {

    protected static final String PATH = "lancamentos";
    private static final Response.ResponseBuilder ERRO_CONTA_NULA = 
        Response.status(Response.Status.BAD_REQUEST);
            
    @QueryParam("conta") 
    private Conta conta;

    @QueryParam("fato") 
    private Fato fato;

    public Lancamentos() {
        super(Lancamento.class, PATH);
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public List<Lancamento> listar(){
        
        final List<Lancamento> lancamentos;
        
        final Conta conta = getConta();
        
        final EntityManager entityManager = emf.createEntityManager();

        try {

            if(conta != null){
                lancamentos = filtrar(entityManager,
                    (cb, r) -> {return cb.equal(r.get(Lancamento_.conta), conta);}
                );
            } else {
                final Fato fato = getFato();
                if (fato != null){
                    lancamentos = filtrar(
                        entityManager,
                        (cb, r) -> {
                            return cb.equal(
                                r.get(Lancamento_.fato), 
                                fato);
                        }
                    );
                } else {
                    throw new WebApplicationException(ERRO_CONTA_NULA.build());
                }
            }
        } finally {
            entityManager.close();
        }
            
        return lancamentos;
    }
        
    
    private List<Lancamento> filtrar(
        EntityManager entityManager, 
        BiFunction<CriteriaBuilder, Root<Lancamento>,Predicate> predicateProducer){
        
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
            predicateProducer.apply(criteriaBuilder, rootLancamento);
        
        cq.where(predicado);

        final TypedQuery<Lancamento> tq = entityManager.createQuery(cq);
        
        final List<Lancamento> xis = tq.getResultList();
            
        return xis;
    }

    public Conta getConta() {
        return conta;
    }

    public void setConta(Conta conta) {
        this.conta = conta;
    }
    
    public Fato getFato() {
        return fato;
    }

    public void setFato(Fato fato) {
        this.fato = fato;
    }
}
