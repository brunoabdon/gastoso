package br.nom.abdon.gastoso.cli;


import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import jline.TerminalFactory;
import jline.console.ConsoleReader;
import org.apache.commons.lang3.StringUtils;

import br.nom.abdon.gastoso.rest.client.GastosoRestClient;
import br.nom.abdon.gastoso.system.GastosoSystem;
import br.nom.abdon.gastoso.system.GastosoSystemRTException;

/**
 *
 * @author Bruno Abdon
 */
public class Main {

    private static final Logger log = Logger.getLogger(Main.class.getName());
    
    public static void main(String[] args) {        

        ConsoleReader console = null;

        try {

            console = new ConsoleReader();

            final GastosoSystem gastosoSystem = inicializaSistema(console);

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
            if(console != null) console.shutdown();
            try {
                TerminalFactory.get().restore();
            } catch (Exception e) {
                log.log(Level.SEVERE, "Estranho.... !", e);
            }            
        }
    }

    private static GastosoSystem inicializaSistema(final ConsoleReader console) 
        throws GastosoSystemRTException, IOException {

        final GastosoSystem gastosoSystem;

        try {
            //inicializacao vai setar servidor web, provavelmente atraves de
            //parametros no args ou por variaveis de ambiente...
            final URI uri = new URI("http://localhost:5000/");

            gastosoSystem = new GastosoRestClient(uri);

            final PrintWriter writer = new PrintWriter(console.getOutput());

            boolean conseguiu = false;
            int tentativasSobrando = 3;
            while(!conseguiu && tentativasSobrando > 0){
                console.setEchoCharacter('*');
                String password = console.readLine("Senha:");
                conseguiu = gastosoSystem.login("", String.valueOf(password));
                if(conseguiu){
                    console.setEchoCharacter(null);
                    console.setPrompt("gastoso>");
                    printWellcome(writer);
                } else if(--tentativasSobrando > 0){
                    writer.println("Foi mal. Tá errada.");
                } else {
                    writer.println("Não rolou não.");
                    System.exit(1);
                }
            }
        } catch (URISyntaxException e) {
            throw new GastosoSystemRTException(e);
        }

        return gastosoSystem;
    }

    private static void printWellcome(PrintWriter writer) {
        writer.println(
            "   ____           _\n  / ___| __ _ ___| |_ ___  ___  ___\n"
            + " | |  _ / _` / __| __/ _ \\/ __|/ _ \\\n | |_| | (_| \\__ \\ || "
            + "(_) \\__ \\ (_) |\n  \\____|\\__,_|___/\\__\\___/|___/\\___/\n");
    }
}