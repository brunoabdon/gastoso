package br.nom.abdon.gastoso;

import br.nom.abdon.modelo.EntidadeBaseInt;
import br.nom.abdon.util.LocalDateISO8601Deserializer;
import br.nom.abdon.util.LocalDateISO8601Serializer;
import br.nom.abdon.util.LocalDateTimePersistenceConverter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 *
 * @author bruno
 */
@Entity
@NamedQueries({
    @NamedQuery(
        name="Fato.porPeriodo",
        query = "SELECT f from Fato f where f.dia BETWEEN :dataMinima AND :dataMaxima ORDER BY f.dia, f.id"
    )
})
public class Fato extends EntidadeBaseInt {

    public static final int DESC_MAX_LEN = 70;
    
    @Column(nullable = false)
    @JsonSerialize(using = LocalDateISO8601Serializer.class)
    @JsonDeserialize(using = LocalDateISO8601Deserializer.class)
    @Convert(converter = LocalDateTimePersistenceConverter.class)
    private LocalDate dia;
    
    @Column(length = DESC_MAX_LEN, nullable = false, unique = false)
    private String descricao;
    
    public Fato() {
    }


    public Fato(LocalDate dia, String descricao) {
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
    
    

}
