package br.nom.abdon.modelo;

import java.io.Serializable;

/**
 *
 * @author bruno
 * @param <Key>
 */
public interface Entidade<Key> extends Serializable {
    
    public Key getId();
    public void setId(Key id);
}