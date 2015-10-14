/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package br.nom.abdon.gastoso;

import br.nom.abdon.modelo.EntidadeBaseInt;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

/**
 *
 * @author bruno
 */
@Entity
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
