package br.nom.abdon.heroku;

import br.nom.abdon.gastoso.CrossOriginFilter;
import java.io.IOException;
import java.io.Writer;
import java.util.EnumSet;
import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import static org.eclipse.jetty.servlets.CrossOriginFilter.ALLOWED_HEADERS_PARAM;
import static org.eclipse.jetty.servlets.CrossOriginFilter.ALLOWED_METHODS_PARAM;
import static org.eclipse.jetty.servlets.CrossOriginFilter.ALLOWED_ORIGINS_PARAM;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * This class launches the web application in an embedded Jetty container. 
 * This is the entry point to your application. The Java command that is 
 * used for launching should fire this main method.
 */
public class Main {

    private static final String ALLOWED_HEADERS = "X-Requested-With,Content-Type,Accept,Origin,Accept-Encoding,Accept-Language,Connection,Host";
    private static final String ALLOWED_METHODS = "GET,POST,PUT,HEAD,OPTIONS,DELETE";

    public static void main(String[] args) throws Exception{
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

        final String corsAllowedOrigins = 
            System.getenv("ABD_HTTP_ALLOWED_ORIGINS");
        
        if(corsAllowedOrigins != null){
            FilterHolder corsFilter = new FilterHolder(CrossOriginFilter.class);
            corsFilter.setInitParameter(ALLOWED_ORIGINS_PARAM, corsAllowedOrigins);
            corsFilter.setInitParameter(ALLOWED_HEADERS_PARAM, ALLOWED_HEADERS);
            corsFilter.setInitParameter(ALLOWED_METHODS_PARAM, ALLOWED_METHODS);
            root.addFilter(corsFilter, "/*", EnumSet.of(DispatcherType.REQUEST));
        }
        
        root.setInitParameter(
            "jersey.config.server.provider.packages", 
            "br.nom.abdon.gastoso.rest");
        
        final String webappDirLocation = "src/main/webapp/";
        root.setDescriptor(webappDirLocation + "/WEB-INF/web.xml");
        root.setResourceBase(webappDirLocation);

        ErrorHandler errHand = new ErrorHandler(){
            @Override
            protected void handleErrorPage(HttpServletRequest request, Writer writer, int code, String message) throws IOException {
                if(message != null){
                    writer.write(message);
                }
                writer.close();
            }
        };
        
        root.setErrorHandler(errHand);
        
        server.setHandler(root);
        
        server.start();
        server.join();
    }
    
}
