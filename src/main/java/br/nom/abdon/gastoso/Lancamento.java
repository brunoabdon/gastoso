/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package br.nom.abdon.gastoso;

import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author bruno
 */

@XmlRootElement
public class Lancamento {
    
    private Integer id;
    private Conta conta;
    private int valor;

    public Lancamento() {
    }
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    
}
