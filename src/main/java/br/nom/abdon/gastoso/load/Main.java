package br.nom.abdon.gastoso.load;

import br.nom.abdon.gastoso.Conta;
import br.nom.abdon.gastoso.Fato;
import br.nom.abdon.gastoso.Lancamento;
import br.nom.abdon.heroku.HerokuUtils;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author bruno
 */
public class Main {

    private static final String PERSISTENT_UNIT = "gastoso_peruni";

    private static final ZoneId ZONE_ID = ZoneId.systemDefault();

    public static void main(String[] args) {
        
        //banco
        final Map<String, String> properties = 
            HerokuUtils.getEMFEnvProperties();
        
        final EntityManagerFactory emf = 
            Persistence.createEntityManagerFactory(PERSISTENT_UNIT, properties);        
        
        //arquivo excell...
        final String fileName = System.getenv("XLS_FILE");
        if(fileName == null){
            System.err.println("defina a envprop XLS_FILE");
            System.exit(1);
        }
        
        final File arquivoXLS = new File(fileName);
        
        if(!arquivoXLS.isFile()){
            System.err.printf("sei ler %s não.\n",fileName);
            System.exit(1);
        }

        final EntityManager em = emf.createEntityManager();
        
        try {
            
            final Workbook wb = new XSSFWorkbook(arquivoXLS);
            final Sheet sheet = wb.getSheet("Contas");
            final Row linhaContas = sheet.getRow(0);
            final int numCells = linhaContas.getLastCellNum() - 1;
            
            final Conta[] contas = new Conta[numCells-2];
            
            em.getTransaction().begin();
            //contas
            for (short i = 2; i < numCells; i++) {
                final Cell cellConta = linhaContas.getCell(i);
                final String nomeDaConta = 
                    cellConta
                    .getStringCellValue()
                    .replaceAll("\\s*(\\r|\\n)\\s*", " ");
                
                final Conta conta = new Conta(nomeDaConta);
                em.persist(conta);
                contas[i-2] = conta;
            }
           
            final FormulaEvaluator evaluator = 
                wb.getCreationHelper().createFormulaEvaluator();
            
            LocalDate date = null;
            short rowNum = 3;

            while(true){
                
                final Row linhaFato = sheet.getRow(rowNum);
                
                final Cell celulaDescricao = linhaFato.getCell(1);
                
                if(celulaDescricao.getCellType() == Cell.CELL_TYPE_BLANK)
                    break;
             
                final Cell celulaData = linhaFato.getCell(0);
                if(celulaData.getCellType() != Cell.CELL_TYPE_BLANK){
                    Date data = celulaData.getDateCellValue();
                    date = data.toInstant().atZone(ZONE_ID).toLocalDate();
                }

                String descricao = celulaDescricao.getStringCellValue();
                Fato fato = new Fato(date,descricao);
                
                em.persist(fato);
                
                for (short i = 2; i < numCells; i++) {
                    final Cell celulaLancamento = linhaFato.getCell(i);
                    final int cellType = celulaLancamento.getCellType();

                    double cellValue;

                    switch(cellType){
                        case Cell.CELL_TYPE_FORMULA:
                            final CellValue formulaValue = 
                                evaluator.evaluate(celulaLancamento);

                            cellValue = formulaValue.getNumberValue();
                            break;
                        case Cell.CELL_TYPE_NUMERIC:
                            cellValue = celulaLancamento.getNumericCellValue();
                            break;
                        case Cell.CELL_TYPE_BLANK:
                            continue;
                        case Cell.CELL_TYPE_STRING:
                            String strVal = 
                                celulaLancamento
                                .getStringCellValue()
                                .replace("R$ ", "");
                            cellValue = Double.parseDouble(strVal);
                            
                        default: 
                            throw new RuntimeException(
                                String.valueOf(celulaLancamento.getCellType()));
                    }

                    int valor = (int)Math.floor(cellValue*100d);
                    Conta conta = contas[i-2];
                    Lancamento lancamento = new Lancamento(fato,conta,valor);

                    System.out.printf("\t%s #%f#\n",
                        lancamento.getConta(),
                        BigDecimal.valueOf(lancamento.getValor(), 2).floatValue());
                    
                    em.persist(lancamento);
                }
                rowNum++;
            }
            
            em.getTransaction().commit();
            
        } catch (IOException | InvalidFormatException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            em.close();
        }
        
        System.exit(0);
    }
}