package br.nom.abdon.gastoso.cli;

import br.nom.abdon.gastoso.cli.parser.GastosoCliLexer;
import br.nom.abdon.gastoso.cli.parser.GastosoCliParser;
import java.io.Console;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;

/**
 *
 * @author Bruno Abdon
 */
public class Main {
    
    public static void main(String[] args) {
        
        

        Console console = System.console();
        
        String line;
        while((line = console.readLine("parser> ")) != null){
            parse(line);
        }
        
        
//        parse("fatos");
//        parse("contas");
//        parse("contas brasil");
//        parse("conta 1");
//        parse("fato 1");
//        parse("fato 1/2");
//        parse("rm conta 434");
        
    }
    
    private static void parse(String msg){
        CharStream cs = new ANTLRInputStream(msg);
        
        GastosoCliLexer hl = new GastosoCliLexer(cs);
        
        final Vocabulary vocabulary = hl.getVocabulary();
        
        hl.getAllTokens().stream().forEach(
            t -> System.out.printf(
                "<%2d> [%s]: %s \n",
                t.getType(),
                vocabulary.getDisplayName(t.getType()),
                t.getText()
            )
        );
        
//        final List<? extends Token> tokensList = hl.getAllTokens();
//        System.out.println(tokensList.size() + " tokens");
//        tokensList.stream().forEach((token) -> {
//            System.out.printf("<%s>\n",token.getText());
//        });

        hl.reset();

        CommonTokenStream tokens = new CommonTokenStream(hl);

        GastosoCliParser parser = new GastosoCliParser(tokens);
        
        
        parser.setBuildParseTree(true);

        ParserRuleContext tree = parser.r();
        
        
        
        System.out.println(msg  + " -> " + tree.toStringTree());
    
    }
    
}
