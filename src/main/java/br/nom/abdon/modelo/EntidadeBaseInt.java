package br.nom.abdon.modelo;

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
        final X entidade;

        try {
            final int id = Integer.parseInt(str);
        
            entidade = klass.newInstance();
            entidade.setId(id);
        } catch (InstantiationException 
                | IllegalAccessException 
                | NumberFormatException e){
            throw new RuntimeException(e);
        }

        return entidade;
    }
}