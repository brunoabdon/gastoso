/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package br.nom.abdon.gastoso;

import br.nom.abdon.modelo.EntidadeBaseInt;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import java.time.LocalDate;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

/**
 *
 * @author bruno
 */
@Entity
public class Movimentacao extends EntidadeBaseInt {

    @Column(nullable = false)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate dia;
    
    @Column(length = 70, nullable = false, unique = true)
    private String descricao;
    
    @OneToMany(orphanRemoval = true)
    private List<Lancamento> lancamentos;
    

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

    public List<Lancamento> getLancamentos() {
        return lancamentos;
    }

    public void setLancamentos(List<Lancamento> lancamentos) {
        this.lancamentos = lancamentos;
    }
}
