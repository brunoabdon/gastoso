package br.nom.abdon.gastoso;

import br.nom.abdon.gastoso.util.LancamentoJsonSerializer;
import br.nom.abdon.modelo.EntidadeBaseInt;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 *
 * @author bruno
 */
@Entity
@NamedQueries({
    @NamedQuery(
        name="Lancamento.porFato",
        query = "SELECT l FROM Lancamento l WHERE l.fato = :fato ORDER BY l.conta"
    ),
    @NamedQuery(
        name = "Lancamento.porContaPeriodo",
        query = "SELECT l FROM Lancamento l WHERE l.conta = :conta AND l.fato.dia BETWEEN :dataMin AND :dataMax ORDER BY l.fato.dia, l.id"
    ),
    
    @NamedQuery(
        name = "Lancamento.deletarPorFato",
        query = "DELETE FROM Lancamento l WHERE l.fato = :fato"
    ),
    @NamedQuery(
        name = "Lancamento.existeDuplicata",
        query = "SELECT COUNT(l.id) > 0 FROM Lancamento l WHERE l.fato = :fato AND l.conta = :conta"
    ),
    @NamedQuery(
        name = "Lancamento.totalDaContaEm",
        query = "SELECT SUM(l.valor) FROM Lancamento l WHERE l.conta = :conta AND l.fato.dia <= :dataFinal"
    ),
    @NamedQuery(
        name = "Lancamento.saldoAnterior",
        query = "SELECT SUM(l2.valor) FROM Lancamento l, Lancamento l2 WHERE l = :lancamento AND l.conta = l2.conta AND (l.fato.dia > l2.fato.dia OR (l.fato.dia = l2.fato.dia AND l.fato.id > l2.fato.id))"
    )
})
@JsonSerialize(using = LancamentoJsonSerializer.class)
public class Lancamento extends EntidadeBaseInt {

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private Fato fato;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private Conta conta;
    
    @Column(precision=11, scale=0, nullable = false)
    private int valor;

    public Lancamento() {
    }

    public Lancamento(Fato fato, Conta conta, int valor) {
        this.fato = fato;
        this.conta = conta;
        this.valor = valor;
    }
    
    public Conta getConta() {
        return conta;
    }

    public void setConta(Conta conta) {
        this.conta = conta;
    }

    public int getValor() {
        return valor;
    }

    public void setValor(int valor) {
        this.valor = valor;
    }
    
    public Fato getFato() {
        return fato;
    }

    public void setFato(Fato fato) {
        this.fato = fato;
    }
    
    @Override
    public boolean equals(Object obj) {
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