package com.github.brunoabdon.gastoso;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.github.brunoabdon.commons.modelo.EntidadeBaseInt;

/**
 *
 * @author bruno
 */
@Entity
@NamedQueries({
    @NamedQuery(
        name="Lancamento.porFato",
        query = 
            "SELECT l FROM Lancamento l WHERE l.fato = :fato ORDER BY l.conta"
    ),
    @NamedQuery(
        name = "Lancamento.porContaPeriodo",
        query = 
            "SELECT l FROM Lancamento l WHERE "
            + "l.conta = :conta "
            + "AND l.fato.dia BETWEEN :dataMin "
            + "AND :dataMax ORDER BY l.fato.dia, l.id"
    ),
    @NamedQuery(
        name = "Lancamento.deletarPorFato",
        query = "DELETE FROM Lancamento l WHERE l.fato = :fato"
    ),
    @NamedQuery(
        name = "Lancamento.existeDuplicata",
        query = 
            "SELECT COUNT(l.id) > 0 FROM Lancamento l WHERE "
            + "l.fato = :fato "
            + "AND l.conta = :conta"
    )
})
public class Lancamento extends EntidadeBaseInt {

    private static final long serialVersionUID = -3510137276546152596L;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private Fato fato;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private Conta conta;
    
    @Column(precision=11, scale=0, nullable = false)
    private int valor;

    public Lancamento() {
    }

    public Lancamento(final Fato fato, final Conta conta, final int valor) {
        this.fato = fato;
        this.conta = conta;
        this.valor = valor;
    }
    
    public Conta getConta() {
        return conta;
    }

    public void setConta(final Conta conta) {
        this.conta = conta;
    }

    public int getValor() {
        return valor;
    }

    public void setValor(final int valor) {
        this.valor = valor;
    }
    
    public Fato getFato() {
        return fato;
    }

    public void setFato(final Fato fato) {
        this.fato = fato;
    }
    
    @Override
    public boolean equals(final Object obj) {
        boolean equal = obj != null && (obj instanceof Lancamento);
        if(equal){
            final Lancamento lancamento = (Lancamento) obj;
            equal = 
                    Objects.equals(this.getId(), lancamento.getId())
                    && Objects.equals(this.getValor(), lancamento.getValor())
                    && Objects.equals(this.getConta(), lancamento.getConta())
                    && Objects.equals(this.getFato(), lancamento.getFato());
        }
        return equal;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(3, 11)
            .append(getId())
            .append(getValor())
            .append(getFato())
            .append(getConta())
            .toHashCode();
    }
}