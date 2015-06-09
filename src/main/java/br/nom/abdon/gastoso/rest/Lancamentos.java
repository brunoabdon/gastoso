package br.nom.abdon.gastoso.rest;

import br.nom.abdon.gastoso.Lancamento;
import br.nom.abdon.gastoso.Conta;
import br.nom.abdon.gastoso.Lancamento_;
import br.nom.abdon.gastoso.Movimentacao;
import br.nom.abdon.gastoso.Movimentacao_;
import br.nom.abdon.rest.AbstractRestCrud;
import java.util.List;
import java.util.function.BiFunction;
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

    @QueryParam("movimentacao") 
    private Movimentacao movimentacao;

    public Lancamentos() {
        super(Lancamento.class, PATH);
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public List<Lancamento> listar(){
        
        List<Lancamento> lancamentos;
        
        Conta conta = getConta();
       
        if(conta != null){
            lancamentos = filtrar(
                (cb, r) -> {return cb.equal(r.get(Lancamento_.conta), conta);}
            );
        } else {
            Movimentacao movimentacao = getMovimentacao();
            if (movimentacao != null){
                lancamentos = filtrar(
                    (cb, r) -> {
                        return cb.equal(
                            r.get(Lancamento_.movimentacao), 
                            movimentacao);
                    }
                );
            } else {
                throw new WebApplicationException(ERRO_CONTA_NULA.build());
            }
        }
            
        return lancamentos;
    }
        
    
    private List<Lancamento> filtrar(BiFunction<CriteriaBuilder, Root<Lancamento>,Predicate> predicateProducer){
        
        System.out.println("conta:" + conta);
        
        Metamodel metamodel = entityManager.getMetamodel();
        EntityType<Lancamento> lancamentoMetamodel = 
            metamodel.entity(Lancamento.class);
        
        CriteriaBuilder criteriaBuilder = 
            entityManager.getCriteriaBuilder();
        
        CriteriaQuery<Lancamento> cq = 
            criteriaBuilder.createQuery(Lancamento.class);
        
        Root<Lancamento> rootLancamento = cq.from(lancamentoMetamodel);
        Join<Lancamento,Movimentacao> join = 
            rootLancamento.join(Lancamento_.movimentacao);
        
        cq.orderBy(criteriaBuilder.asc(join.get(Movimentacao_.dia)));
        
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
    
    public Movimentacao getMovimentacao() {
        return movimentacao;
    }

    public void setMovimentacao(Movimentacao movimentacao) {
        this.movimentacao = movimentacao;
    }
}
