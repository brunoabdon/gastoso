/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.nom.abdon.modelo;

import br.nom.abdon.gastoso.Conta;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 *
 * @author bruno
 */
@MappedSuperclass
public class EntidadeBaseInt implements Entidade<Integer>{
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public static <X extends EntidadeBaseInt> X fromString(Class<X> klass, String str){
        X entidade;

        System.out.println("parsing " + str);        
        try {
            int id = Integer.parseInt(str);
        
            entidade = klass.newInstance();
            entidade.setId(id);
        } catch (InstantiationException | IllegalAccessException | NumberFormatException e){
            throw new RuntimeException(e);
        }

        return entidade;
    }

    

}
