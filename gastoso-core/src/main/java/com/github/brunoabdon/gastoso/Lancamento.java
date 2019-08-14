package com.github.brunoabdon.gastoso;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.github.brunoabdon.commons.util.modelo.Identifiable;

/**
 * A manifestação de um {@link #getValor() valor} numa {@link Conta} decorrente
 * de um {@link Fato}.
 * 
 * @author bruno
 */
@Entity
@NamedQueries({
    @NamedQuery(
        name="Lancamento.porFato",
        query = 
            "SELECT l FROM Lancamento l WHERE "
            + "l.fato = :fato ORDER BY l.conta"
    ),
    @NamedQuery(
        name = "Lancamento.porContaPeriodo",
        query = 
            "SELECT l FROM Lancamento l WHERE "
            + "l.conta = :conta "
            + "AND l.fato.dia BETWEEN :dataMin "
            + "AND :dataMax ORDER BY l.fato.dia"
    ),
    @NamedQuery(
        name = "Lancamento.deletarPorFato",
        query = "DELETE FROM Lancamento l WHERE l.fato = :fato"
    )
})
public class Lancamento implements Identifiable<Lancamento.Id>, Serializable{

    private static final long serialVersionUID = -3510137276546152596L;

    @Embeddable
    public static class Id implements Serializable {
    	
		private static final long serialVersionUID = -1734494257092861534L;
		
		@Column(name="fato_id")
		private Integer fatoId;
		
		@Column(name="conta_id")
    	private Integer contaId;

		public Id() {
		}
		
		public Id(final Integer fatoId, final Integer contaId) {
			super();
			this.fatoId = fatoId;
			this.contaId = contaId;
		}

		@Override
		public int hashCode() {
			return Objects.hash(contaId, fatoId);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			final Id other = (Id) obj;
			return 
				Objects.equals(contaId, other.contaId) 
				&& Objects.equals(fatoId, other.fatoId);
		}
    }
    
    @EmbeddedId
    private Id id;

    @ManyToOne
    @JoinColumn(insertable = false, updatable = false)
    private Fato fato;

    @ManyToOne
    @JoinColumn(insertable = false, updatable = false)
    private Conta conta;
    
    @Column(precision=11, scale=0, nullable = false)
    private int valor;

    public Lancamento() {
    }

    public Lancamento(final Fato fato, final Conta conta, final int valor) {
    	this(valor);
        this.id = new Lancamento.Id(fato.getId(),conta.getId());
        this.fato = fato;
        this.conta = conta;
    }

    public Lancamento(final int valor) {
        this.valor = valor;
    }
    
	@Override
	public Id getId() {
		return id;
	}

    public Conta getConta() {
        return conta;
    }

    public int getValor() {
        return valor;
    }

    public void setId(final Id id) {
		this.id = id;
		if(id == null) {
			this.fato = null;
			this.conta = null;
		} else {
			this.fato = new Fato(id.fatoId);
			this.conta = new Conta(id.contaId);
		}
	}

    public void setValor(final int valor) {
        this.valor = valor;
    }
    
    public Fato getFato() {
        return fato;
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