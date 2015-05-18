/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package br.nom.abdon.gastoso;

import br.nom.abdon.modelo.EntidadeBaseInt;
import br.nom.abdon.util.LocalDateTimePersistenceConverter;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;

/**
 *
 * @author bruno
 */
@Entity
public class Movimentacao extends EntidadeBaseInt {

    @Column(nullable = false)
    @JsonSerialize(using = LocalDateSerializer.class)
    @Convert(converter = LocalDateTimePersistenceConverter.class)
    private LocalDate dia = LocalDate.now();
    
    @Column(length = 70, nullable = false, unique = false)
    private String descricao;
    
    public Movimentacao() {
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
    
    public static Movimentacao fromString(String str){
        return EntidadeBaseInt.fromString(Movimentacao.class, str);
    }

}
