package com.github.brunoabdon.gastoso;

import com.github.brunoabdon.commons.modelo.EntidadeBaseInt;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 *
 * @author bruno
 */
@Entity
 @NamedQuery(
    name = "Conta.all",
    query = "SELECT c FROM Conta c ORDER BY c.nome")
public class Conta extends EntidadeBaseInt {
    
    private static final long serialVersionUID = 7321886996603362113L;

    public static final int NOME_MAX_LEN = 50;

    @Column(length = NOME_MAX_LEN, nullable = false, unique = true)
    private String nome;

    public Conta() {
    }

    public Conta(Integer id) {
        this(id,null);
    }
    
    public Conta(String nome) {
        this.nome = nome;
    }

    public Conta(Integer id, String nome) {
        this(nome);
        super.setId(id);
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
 
    public static Conta fromString(String str){
        return EntidadeBaseInt.fromString(Conta.class, str);
    }

    @Override
    public String toString() {
        return "[Conta: " + nome + "]";
    }

    @Override
    public boolean equals(Object obj) {
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
        return new HashCodeBuilder(3, 11)
            .append(getId())
            .append(getNome())
            .toHashCode();
    }
}
