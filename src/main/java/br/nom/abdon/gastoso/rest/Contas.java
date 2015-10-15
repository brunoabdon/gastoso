package br.nom.abdon.gastoso.rest;

import br.nom.abdon.gastoso.Conta;
import br.nom.abdon.gastoso.dal.ContasDao;
import br.nom.abdon.rest.AbstractRestCrud;
import br.nom.abdon.util.LocalDateISO8601Deserializer;
import br.nom.abdon.util.LocalDateISO8601Serializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.time.LocalDate;
import java.util.List;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author bruno
 */
@Path(Contas.PATH)
@Produces(MediaType.APPLICATION_JSON)
public class Contas extends AbstractRestCrud<Conta,Integer>{

    static final String PATH = "contas";
    private final ContasDao dao;
    
    public Contas() {
        super(PATH);
        this.dao = new ContasDao();
    }

    @Override
    protected ContasDao getDao() {
        return this.dao;
    }
    
    @GET
    public List<Conta> listar() {
        final List<Conta> contas;
        
        EntityManager em = emf.createEntityManager();
        
        try {
            contas = dao.listar(em);
        } finally {
            em.close();
        }
        
        return contas;
    }
 }