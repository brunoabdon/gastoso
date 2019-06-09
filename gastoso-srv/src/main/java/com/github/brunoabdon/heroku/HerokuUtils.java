package com.github.brunoabdon.heroku;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 *
 * @author bruno
 */
public class HerokuUtils {

    /**
     * Cria e retorna um mapa de propriedades pra inicializar um 
     * EntityManagerFactory jpa com as propriedades de banco encontradas na 
     * variavel de ambiente DATABASE_URL.
     * 
     * @return Um mapa de propriedades de EntityManagerFactory.
     */
    public static Map<String, String> getEMFEnvProperties() {
        final String databaseUrl = System.getenv("DATABASE_URL");
        final StringTokenizer st = new StringTokenizer(databaseUrl, ":@/");
        final String dbVendor = st.nextToken();
        final String userName = st.nextToken();
        final String password = st.nextToken();
        final String host = st.nextToken();
        final String port = st.nextToken();
        final String databaseName = st.nextToken();
        final String jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory", host, port, databaseName);
        System.out.println(jdbcUrl);
        final String showSql = System.getenv("ABD_HIBERNATE_SHOW_SQL");
        final Map<String, String> properties = new HashMap<>();
        properties.put("javax.persistence.jdbc.url", jdbcUrl);
        properties.put("javax.persistence.jdbc.user", userName);
        properties.put("javax.persistence.jdbc.password", password);
        properties.put("hibernate.show_sql", showSql);
        return properties;
    }
}
