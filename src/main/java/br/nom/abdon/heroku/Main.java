package br.nom.abdon.heroku;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import org.apache.commons.dbcp2.BasicDataSource;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * This class launches the web application in an embedded Jetty container. 
 * This is the entry point to your application. The Java command that is 
 * used for launching should fire this main method.
 */
public class Main {

    public static BasicDataSource connectionPool;

    private static void inicializaConnectionPool() throws URISyntaxException, SQLException {
        URI dbUri = new URI(System.getenv("DATABASE_URL"));
        String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + dbUri.getPath();
        connectionPool = new BasicDataSource();

        if (dbUri.getUserInfo() != null) {
            connectionPool.setUsername(dbUri.getUserInfo().split(":")[0]);
            connectionPool.setPassword(dbUri.getUserInfo().split(":")[1]);
        }
        connectionPool.setDriverClassName("org.postgresql.Driver");
        connectionPool.setUrl(dbUrl);
        connectionPool.setInitialSize(1);
    }

    public static void main(String[] args) throws Exception{
        inicializaConnectionPool();
        inicializaServidorWeb();
    }

    private static void inicializaServidorWeb() throws NumberFormatException, InterruptedException, Exception {
        // The port that we should run on can be set into an environment variable
        // Look for that variable and default to 8080 if it isn't there.
        String webPort = System.getenv("PORT");
        if (webPort == null || webPort.isEmpty()) {
            webPort = "8080";
        }

        final Server server = new Server(Integer.valueOf(webPort));
        final WebAppContext root = new WebAppContext();

        root.setContextPath("/");
        // Parent loader priority is a class loader setting that Jetty accepts.
        // By default Jetty will behave like most web containers in that it will
        // allow your application to replace non-server libraries that are part of the
        // container. Setting parent loader priority to true changes this behavior.
        // Read more here: http://wiki.eclipse.org/Jetty/Reference/Jetty_Classloading
        root.setParentLoaderPriority(true);

        final String webappDirLocation = "src/main/webapp/";
        root.setDescriptor(webappDirLocation + "/WEB-INF/web.xml");
        root.setResourceBase(webappDirLocation);

        server.setHandler(root);

        server.start();
        server.join();
    }
}
