package com.github.brunoabdon.gastoso;

import java.time.LocalDate;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.github.brunoabdon.commons.dal.util.LocalDatePersistenceConverter;
import com.github.brunoabdon.commons.modelo.EntidadeBaseInt;


/**
 *
 * @author bruno
 */
@Entity
@NamedQueries({
    @NamedQuery(
        name="Fato.porPeriodo",
        query = 
            "SELECT f from Fato f where "
            + "f.dia BETWEEN :dataMinima AND :dataMaxima "
            + "ORDER BY f.dia, f.id"
    ),
    @NamedQuery(
        name ="Fato.porContaPeriodo",
        query = 
            "SELECT l.fato from Lancamento l where "
            + "l.conta = :conta "
            + "and l.fato.dia <= :dataMaxima "
            + "ORDER BY l.fato.dia desc, l.fato.id desc"
    )
})
public class Fato extends EntidadeBaseInt {

    private static final long serialVersionUID = 612424688270067621L;

    public static final int DESC_MAX_LEN = 70;
    
    @Column(nullable = false)
    @Convert(converter = LocalDatePersistenceConverter.class)
    private LocalDate dia;
    
    @Column(length = DESC_MAX_LEN, nullable = false, unique = false)
    private String descricao;
    
    public Fato() {
    }

    public Fato(final Integer id) {
        super.setId(id);
    }
    
    public Fato(final LocalDate dia, final String descricao) {
        this.dia = dia;
        this.descricao = descricao;
    }
    
    public LocalDate getDia() {
        return dia;
    }

    public void setDia(LocalDate dia) {
        this.dia = dia;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    
    public static Fato fromString(String str){
        return EntidadeBaseInt.fromString(Fato.class, str);
    }

    @Override
    public String toString() {
        return "[Fato:" + (dia == null? null : dia.toString())
                + " - " + descricao + "]";
    }

    @Override
    public boolean equals(Object obj) {
        boolean equal = obj != null && (obj instanceof Fato);
        if(equal){
            final Fato fato = (Fato) obj;
            equal = 
                Objects.equals(this.getId(), fato.getId())
                && Objects.equals(this.getDescricao(), fato.getDescricao())
                && Objects.equals(this.getDia(), fato.getDia());
        }
        return equal;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(79, 79)
            .append(getId())
            .append(getDia())
            .append(getDescricao())
            .toHashCode();
    }
}