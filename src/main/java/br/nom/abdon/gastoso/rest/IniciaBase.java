/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package br.nom.abdon.gastoso.rest;

import br.nom.abdon.gastoso.Conta;
import br.nom.abdon.gastoso.Lancamento;
import br.nom.abdon.gastoso.Fato;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author bruno
 */
@Produces(MediaType.TEXT_PLAIN)
@Path("init")
public class IniciaBase {
    
  private static final EntityManagerFactory emf;
    
    static {
        final String databaseUrl = System.getenv("DATABASE_URL");
        final StringTokenizer st = new StringTokenizer(databaseUrl, ":@/");
        
        final String dbVendor = st.nextToken(); //if DATABASE_URL is set
        final String userName = st.nextToken();
        final String password = st.nextToken();
        final String host = st.nextToken();
        final String port = st.nextToken();
        final String databaseName = st.nextToken();

        final String jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory", host, port, databaseName);

//        System.out.println(jdbcUrl);
        
        final Map<String, String> properties = new HashMap<>();
        properties.put("javax.persistence.jdbc.url", jdbcUrl );
        properties.put("javax.persistence.jdbc.user", userName );
        properties.put("javax.persistence.jdbc.password", password );
        properties.put("javax.persistence.jdbc.driver", "org.postgresql.Driver");
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");

        emf = Persistence.createEntityManagerFactory("gastoso_peruni", properties);        
    }
    
    
    @GET
    public String incializar(){    
        Fato saque = new Fato();
        saque.setDescricao("Saque");
        saque.setDia(LocalDate.now());
        

        Fato almoco = new Fato();
        almoco.setDescricao("Almoço");
        almoco.setDia(LocalDate.now());

        Conta bb, santander, cielo, carteira, rioCard;
        
        bb = new Conta();
        bb.setNome("Banco do Brasil");
        
        santander = new Conta();
        santander.setNome("Santander");
        
        carteira = new Conta();
        carteira.setNome("Carteira");
        
        cielo = new Conta();
        cielo.setNome("Cielo");
        
        rioCard = new Conta();
        rioCard.setNome("Rio Card");
        
        
        Lancamento l1 = new Lancamento();
        l1.setFato(saque);
        l1.setConta(bb);
        l1.setValor(-100);
        
        Lancamento l2 = new Lancamento();
        l2.setFato(saque);
        l2.setConta(carteira);
        l2.setValor(100);
        
        Lancamento l3 = new Lancamento();
        l3.setFato(almoco);
        l3.setConta(cielo);
        l3.setValor(-16);

        EntityManager entityManager = emf.createEntityManager();
        
        entityManager.getTransaction().begin();
        
        
        entityManager.persist(saque);
        entityManager.persist(almoco);
        
        entityManager.persist(bb);
        entityManager.persist(santander);
        entityManager.persist(carteira);
        entityManager.persist(cielo);
        entityManager.persist(rioCard);
        
        entityManager.persist(l1);
        entityManager.persist(l2);
        entityManager.persist(l3);
        
        entityManager.getTransaction().commit();

        return "OK";
        
    }
    
    
}
