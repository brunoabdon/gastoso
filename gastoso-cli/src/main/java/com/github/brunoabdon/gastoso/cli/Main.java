package com.github.brunoabdon.gastoso.cli;

import com.github.brunoabdon.gastoso.rest.client.GastosoRestClient;
import com.github.brunoabdon.gastoso.system.GastosoSystemRTException;
import com.github.brunoabdon.commons.rest.RESTClientRTException;
import static com.github.brunoabdon.commons.rest.RESTClientRTException.SERVIDOR_FORA;
import com.github.brunoabdon.commons.rest.RESTResponseException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import jline.TerminalFactory;
import jline.console.ConsoleReader;
import jline.console.history.FileHistory;
import jline.console.history.History;
import org.apache.commons.lang3.StringUtils;


/**
 *
 * @author Bruno Abdon
 */
public class Main {

    private static final Logger log = Logger.getLogger(Main.class.getName());

    private static final Preferences prefs =
        Preferences.userNodeForPackage(Main.class);

    public static void main(String[] args) {

        ConsoleReader console = null;
        GastosoRestClient gastosoSystem = null;

        try {

            console = buildConsole();

            if(args.length > 0 && args[0].equals("--askURI")){ //improve this
                askForURI(console);
            }

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
        } catch (RESTClientRTException ex) {
            log.log(Level.SEVERE, "Erro na comunicacao!", ex);
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

    private static ConsoleReader buildConsole() throws IOException {

        final ConsoleReader console = new ConsoleReader();

        final String userHome = System.getProperty("user.home");
        final File gastosoDir =
            FileSystems.getDefault().getPath(userHome,".gastoso").toFile();

        if(gastosoDir.exists() || gastosoDir.mkdir()){
            if(gastosoDir.canRead() && gastosoDir.canWrite()){
                final File histFile = new File(gastosoDir, "gastoso_history");
                final History history = new FileHistory(histFile);

                console.setHistory(history);
            }
        }

        return console;
    }

    private static GastosoRestClient inicializaSistema(
            final ConsoleReader console) throws IOException {

        String uriStr = prefs.get(SERVER_URI_PREF, null);
        URI uri;
        try {
            uri =
                uriStr == null
                    ? askForURI(console)
                    : new URI(uriStr);
        } catch(URISyntaxException e){
            log.severe("Preference file is corrupted.");
            uri = askForURI(console);
        }

        return inicializaSistema(console,uri);
    }

    private static URI askForURI(ConsoleReader console) throws IOException {

        URI uri = null;

        String uriStr = console.readLine("Gastoso's Server's URI:");

        boolean ainda = true;
        while(ainda) {
            try {
                uri = new URI(uriStr);
                if(ainda = uri.isOpaque()){
                    uriStr = console.readLine("Invalid Opaque URI. Try again:");
                } else {
                    prefs.put(SERVER_URI_PREF, uriStr);
                }
            } catch (URISyntaxException e) {
                uriStr = console.readLine("Invalid URI. Try again:");
            }
        }
        return uri;
    }
    private static final String SERVER_URI_PREF = "serverUri";

    private static GastosoRestClient inicializaSistema(
            final ConsoleReader console, final URI uri) throws IOException {

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

        } catch (RESTClientRTException e) {
            if(e.getCode() == SERVIDOR_FORA){
                System.out.printf(
                    "%s:%d is not responding.\n",
                    uri.getHost(),
                    uri.getPort());
                System.out.flush();
                System.exit(1);
            }
            throw e;
        } catch (RESTResponseException e) {
            throw new GastosoSystemRTException(e);
        }

        return gastosoRestClient;
    }

    private static void printWellcome(PrintWriter writer) {
        writer.println(
            "   ____           _\n  / ___| __ _ ___| |_ ___  ___  ___\n | |  _ "
            + "/ _` / __| __/ _ \\/ __|/ _ \\\n | |_| | (_| \\__ \\ || (_) \\__"
            + " \\ (_) |\n  \\____|\\__,_|___/\\__\\___/|___/\\___/\n");
    }
}