/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.nom.abdon.modelo;

import java.io.Serializable;
import javax.persistence.Entity;

/**
 *
 * @author bruno
 * @param <Key>
 */
@Entity
public interface Entidade<Key> extends Serializable {
    public Key getId();
    public void setId(Key id);
}
