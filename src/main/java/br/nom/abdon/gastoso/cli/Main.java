package br.nom.abdon.gastoso.cli;

import br.nom.abdon.gastoso.cli.parser.GastosoCliLexer;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser.CommandContext;

import java.io.Console;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;

/**
 *
 * @author Bruno Abdon
 */
public class Main {
    
    public static void main(String[] args) {        
        
        GastosoCommandExecutor cLICommand = new GastosoCommandExecutor();
        
        Console console = System.console();

        String line;
        while((line = console.readLine("parser> ")) != null){
            
            if("exit".equals(line)){
                System.out.println("Bye bye");
                System.exit(0);
            }
            
            final CommandContext commandCtx = parse(line);

            if(commandCtx == null){
                System.out.println("Não entendi");
            } else {
                cLICommand.processCommand(commandCtx);
            }
        }
    }
    
    private static CommandContext parse(String msg){
        
        CommandContext commandCtx;
        
        CharStream cs = new ANTLRInputStream(msg);
        
        GastosoCliLexer hl = new GastosoCliLexer(cs);
        
        CommonTokenStream tokens = new CommonTokenStream(hl);

        GastosoCliParser parser = new GastosoCliParser(tokens);
        
        parser.setErrorHandler(new BailErrorStrategy());
        parser.removeErrorListeners();
        
        //parser.setBuildParseTree(true);
        try {
            commandCtx = parser.command();
            
        } catch (ParseCancellationException e){
            commandCtx = null;
        }
        
        return commandCtx;
    }
}
