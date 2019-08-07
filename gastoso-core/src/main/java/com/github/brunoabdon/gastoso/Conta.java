package com.github.brunoabdon.gastoso;

import com.github.brunoabdon.commons.modelo.EntidadeBaseInt;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Uma carteia à qual se pode associar e movimentar um valor.
 * 
 * @author bruno
 */
@Entity
@NamedQueries({
	@NamedQuery(
		name="Conta.all", query="SELECT c FROM Conta c ORDER BY c.nome"
	),
	@NamedQuery(
		name="Conta.nomeEmUso",
		query="SELECT COUNT(c.id) > 0 FROM Conta c WHERE c.nome = :nome"
	),
	@NamedQuery(
		name="Conta.temLancamento",
		query="SELECT COUNT(l.id) > 0 FROM Lancamento l WHERE l.conta = :conta"
	)
})
public class Conta extends EntidadeBaseInt {
    
    private static final long serialVersionUID = 7321886996603362113L;

    public static final int NOME_MAX_LEN = 50;

    @Column(length = NOME_MAX_LEN, nullable = false, unique = true)
    private String nome;

    public Conta() {
    }

    public Conta(final Integer id) {
        this(id,null);
    }
    
    public Conta(final String nome) {
        this.nome = nome;
    }

    public Conta(final Integer id, final String nome) {
        this(nome);
        super.setId(id);
    }

    public String getNome() {
        return nome;
    }

    public void setNome(final String nome) {
        this.nome = nome;
    }
 
    public static Conta fromString(final String str){
        return EntidadeBaseInt.fromString(Conta.class, str);
    }

    @Override
    public String toString() {
        return "[Conta: " + nome + "]";
    }

    @Override
    public boolean equals(final Object obj) {
        boolean equal = obj != null && (obj instanceof Conta);
        if(equal){
            final Conta conta = (Conta) obj;
            equal = Objects.equals(this.getId(), conta.getId())
                    && Objects.equals(this.getNome(), conta.getNome());
        }
        return equal;
    }

    @Override
    public int hashCode() {
        return 
            new HashCodeBuilder(3, 11)
            .append(getId())
            .append(getNome())
            .toHashCode();
    }
}