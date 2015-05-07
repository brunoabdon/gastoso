/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.nom.abdon.gastoso.rest;

import br.nom.abdon.gastoso.Lancamento;
import br.nom.abdon.gastoso.Conta;
import br.nom.abdon.gastoso.Conta_;
import br.nom.abdon.gastoso.Lancamento_;
import br.nom.abdon.gastoso.Movimentacao;
import br.nom.abdon.gastoso.Movimentacao_;
import br.nom.abdon.rest.AbstractRestCrud;
import java.time.LocalDate;
import java.util.List;
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
import javax.ws.rs.core.MediaType;

/**
 *
 * @author bruno
 */
@Path(Lancamentos.PATH)
public class Lancamentos extends AbstractRestCrud<Lancamento, Integer> {

    protected static final String PATH = "lancamentos";
    
    public Lancamentos() {
        super(Lancamento.class, PATH);
    }
    
    @QueryParam("conta") 
    private Conta conta;
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public List<Lancamento> listar(){
        
        Conta conta = getConta();
        
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
        
        final Predicate predicadoConta = 
            criteriaBuilder.equal(rootLancamento.get(Lancamento_.conta), conta);
        
        cq.where(predicadoConta);
        
        cq.orderBy(criteriaBuilder.asc(join.get(Movimentacao_.dia)));
        
        cq.select(rootLancamento);
        
        TypedQuery<Lancamento> tq = entityManager.createQuery(cq);
        
        System.out.println(cq.toString());
        
        System.out.println(tq.toString());
        
        
        List<Lancamento> xis = tq.getResultList();
            
        return xis;
    }

    public Conta getConta() {
        return conta;
    }

    public void setConta(Conta conta) {
        this.conta = conta;
    }
    

    
    
}
