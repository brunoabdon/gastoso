/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package br.nom.abdon.gastoso;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import java.time.LocalDate;
import java.util.List;

/**
 *
 * @author bruno
 */
public class Movimentacao {

//    @JsonSerialize(using = LocalDateSerializer.class)
//    private LocalDate data;
    private String descricao;
    private List<Lancamento> lancamentos;
    

    public Movimentacao() {
    }

//    public LocalDate getData() {
//        return data;
//    }

//    public void setData(LocalDate data) {
//        this.data = data;
//    }

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
