/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package br.nom.abdon.gastoso;

import br.nom.abdon.modelo.EntidadeBaseInt;
import javax.persistence.Column;
import javax.persistence.Entity;

/**
 *
 * @author bruno
 */
@Entity
public class Conta extends EntidadeBaseInt {
    
    @Column(length = 50, nullable = false, unique = true)
    private String nome;

    public Conta() {
    }

    public Conta(String nome) {
        this.nome = nome;
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
        return nome;
    }

    
    
}
