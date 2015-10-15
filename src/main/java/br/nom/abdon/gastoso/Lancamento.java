package br.nom.abdon.gastoso;

import br.nom.abdon.modelo.EntidadeBaseInt;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

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
    )
})
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
    
}
