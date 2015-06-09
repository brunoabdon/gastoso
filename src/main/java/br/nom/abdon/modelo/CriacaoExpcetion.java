package br.nom.abdon.modelo;

/**
 *
 * @author bruno
 */
public class CriacaoExpcetion extends Exception{

    private static final String BASE_MSG = "Não foi possivel criar %s.";
    
//    private final Entidade entidade;
    
    public CriacaoExpcetion(Throwable causa, Entidade entidade) {
        super(String.format(BASE_MSG, entidade),causa);
//        this.entidade = entidade;
    }
    
}
