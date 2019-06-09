package com.github.brunoabdon.heroku;

import java.io.IOException;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.webapp.WebAppContext;


/**
 * This class launches the web application in an embedded Jetty container. 
 * This is the entry point to your application. The Java command that is 
 * used for launching should fire this main method.
 */
public class Main {

    private static final Logger log = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws Exception{
        inicializaServidorWeb();
    }

    private static void inicializaServidorWeb() throws NumberFormatException, InterruptedException, Exception {
        // The port that we should run on can be set into an environment variable
        // Look for that variable and default to 8080 if it isn't there.
        String webPort = System.getenv("PORT");
        if (StringUtils.isBlank(webPort)) {
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

        final ErrorHandler errHand = new ErrorHandler(){
            @Override
            protected void handleErrorPage(
                    HttpServletRequest request, 
                    Writer writer, 
                    int code, 
                    String message) throws IOException {
                
                log.log(
                    Level.INFO,
                    "http error {0} - {1}",
                    new Object[]{code, message});
                
                if(message != null){
                    writer.write(message);
                }
                writer.close();
            }
        };

        root.setErrorHandler(errHand);

        GzipHandler gzip = new GzipHandler();
        gzip.setIncludedMimeTypes(MediaType.APPLICATION_JSON);
        gzip.setMinGzipSize(1024);
        gzip.setCompressionLevel(9);
        gzip.setHandler(root);        
        
        server.setHandler(gzip);
        
        server.start();
        server.join();
    }   
}