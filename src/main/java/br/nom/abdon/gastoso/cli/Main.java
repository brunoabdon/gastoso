package br.nom.abdon.gastoso.cli;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.util.logging.Level;
import java.util.logging.Logger;

import jline.TerminalFactory;
import jline.console.ConsoleReader;
import jline.console.history.FileHistory;
import jline.console.history.History;
import org.apache.commons.lang3.StringUtils;

import br.nom.abdon.gastoso.rest.client.GastosoResponseException;
import br.nom.abdon.gastoso.rest.client.GastosoRestClient;
import br.nom.abdon.gastoso.system.GastosoSystemRTException;
import static br.nom.abdon.gastoso.system.GastosoSystemRTException.SERVIDOR_FORA;

/**
 *
 * @author Bruno Abdon
 */
public class Main {

    private static final Logger log = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {

        ConsoleReader console = null;
        GastosoRestClient gastosoSystem = null;
        
        try {

            console = new ConsoleReader();

            restoreCommandHistory(console);

            gastosoSystem = inicializaSistema(console);

            final GastosoCharacterCommand gastosoCharacterCommand = 
                new GastosoCharacterCommand(gastosoSystem, console.getOutput());

            String line;
            while((line = console.readLine()) != null
                    && !"exit".equals(line)){
                
                if(!StringUtils.isBlank(line)){
                    gastosoCharacterCommand.command(line);
                }
            }

            gastosoSystem.logout();

            console.println("Bye bye");

        } catch (IOException ex) {
            log.log(Level.SEVERE, "Deu ruim!", ex);
        } catch (GastosoSystemRTException ex) {
            log.log(Level.SEVERE, "Erro grave!", ex);
        } finally {
            if(gastosoSystem != null) gastosoSystem.close();
            try {
                if(console != null) {
                    ((FileHistory)console.getHistory()).flush();
                    console.shutdown();
                } 

                TerminalFactory.get().restore();
            } catch (IOException ex) {
                log.log(Level.SEVERE, "Erro ao fechar command history.", ex);
            } catch (Exception e) {
                log.log(Level.SEVERE, "Estranho.... !", e);
            }
        }
    }

    private static GastosoRestClient inicializaSistema(
            final ConsoleReader console) 
        throws GastosoSystemRTException, IOException {

        final GastosoRestClient gastosoRestClient;

        String uriStr = System.getenv("ABD_GASTOSO_SRV_URI");
        if(StringUtils.isBlank(uriStr)) uriStr = "http://localhost:5000/";

        try {
            final URI uri = new URI(uriStr);
            gastosoRestClient = inicializaSistema(console,uri);
        } catch (URISyntaxException e) {
            throw new GastosoSystemRTException(e);
        }
        
        return gastosoRestClient;
    }    
    
    private static GastosoRestClient inicializaSistema(
            final ConsoleReader console, final URI uri) 
        throws GastosoSystemRTException, IOException {

        final GastosoRestClient gastosoRestClient;

        try {

            gastosoRestClient = new GastosoRestClient(uri);

            final PrintWriter writer = new PrintWriter(console.getOutput());

            boolean conseguiu = 
                Boolean.parseBoolean(System.getenv("ABD_AUTH_OMNI_EST_LICET"));
            int tentativasSobrando = 3;

            System.out.printf(
                "Connecting to Gastoso's severs on %s:%d.\n", 
                uri.getHost(),
                uri.getPort());

            while(!conseguiu && tentativasSobrando > 0){
                console.setEchoCharacter('*');
                
                String password = console.readLine("Senha:");
                conseguiu = gastosoRestClient.login("", String.valueOf(password));
                if(!conseguiu){
                    if(--tentativasSobrando > 0){
                        writer.println("Foi mal. Tá errada.\n");
                    } else {
                        writer.println("Não rolou não.");
                        writer.flush();
                        
                        System.exit(1);
                    }
                }
            }
            console.setEchoCharacter(null);
            console.setPrompt("gastoso>");
            printWellcome(writer);

        } catch (GastosoSystemRTException e) {
            if(e.getCode() == SERVIDOR_FORA){
                System.out.printf(
                    "%s:%d is not responding.\n",
                    uri.getHost(),
                    uri.getPort());
                System.out.flush();
                System.exit(1);
            }
            throw e;
        } catch (GastosoResponseException e) {
            throw new GastosoSystemRTException(e);
        }

        return gastosoRestClient;
    }

    private static void restoreCommandHistory(final ConsoleReader console) 
            throws IOException {

        final String userHome = System.getProperty("user.home");
        final File gastosoDir = 
            FileSystems.getDefault().getPath(userHome,".gastoso").toFile();

        if(gastosoDir.exists() || gastosoDir.mkdir()){
            restoreCommandHistory(console, gastosoDir);
        }

    }

    private static void restoreCommandHistory(
        final ConsoleReader console, final File gastosoDir) throws IOException {

        if(gastosoDir.canRead() && gastosoDir.canWrite()){
            final File histFile = new File(gastosoDir, "gastoso_history");
            final History history = new FileHistory(histFile);

            console.setHistory(history);
        }
    }    

    private static void printWellcome(PrintWriter writer) {
        writer.println(
            "   ____           _\n  / ___| __ _ ___| |_ ___  ___  ___\n"
            + " | |  _ / _` / __| __/ _ \\/ __|/ _ \\\n | |_| | (_| \\__ \\ || "
            + "(_) \\__ \\ (_) |\n  \\____|\\__,_|___/\\__\\___/|___/\\___/\n");
    }
}