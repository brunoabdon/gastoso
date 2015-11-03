package br.nom.abdon.gastoso.load;

import br.nom.abdon.gastoso.Conta;
import br.nom.abdon.gastoso.Fato;
import br.nom.abdon.gastoso.Lancamento;
import br.nom.abdon.heroku.HerokuUtils;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.Stack;
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
            final Row linhaSaldos = sheet.getRow(2);
            final int numCells = linhaContas.getLastCellNum() - 1;
            final int numContas = numCells-2;
            
            final Conta[] contas = new Conta[numContas];
            final int saldos[] = new int[numContas];
            
            final FormulaEvaluator evaluator = 
                wb.getCreationHelper().createFormulaEvaluator();
            
            em.getTransaction().begin();
            //contas
            for (short i = 0; i < numContas; i++) {
                final int cellIdx = i+2;
                final Cell cellConta = linhaContas.getCell(cellIdx);
                final String nomeDaConta = 
                    cellConta
                    .getStringCellValue()
                    .replaceAll("\\s*(\\r|\\n)\\s*", " ");
                
                final Conta conta = new Conta(nomeDaConta);
                em.persist(conta);
                
                final Cell cellSaldo = linhaSaldos.getCell(cellIdx);
                
                evaluator.evaluate(cellSaldo);
                
                final String strValor = 
                    cellSaldo
                    .getStringCellValue()
                    .replace("R$ ", "")
                    .replace(",", "")
                    .replace(".", "");
                
                final int saldo = Integer.parseInt(strValor);

                contas[i] = conta;
                saldos[i] = saldo;
            }
           
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
                    System.out.printf("%s\n",date);
                }

                String descricao = celulaDescricao.getStringCellValue();
                Fato fato = new Fato(date,descricao);
                
                System.out.printf("\t%s\n",fato.getDescricao());
                
                em.persist(fato);
                
                for (short i = 0; i < numContas; i++) {
                    final int cellIdx = i+2;
                    final Cell celulaLancamento = linhaFato.getCell(cellIdx);
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
                            
                        default: 
                            throw new RuntimeException(
                                String.valueOf(celulaLancamento.getCellType()));
                    }

                    final int valor = 
                        BigDecimal
                            .valueOf(cellValue)
                            .movePointRight(2)
                            .setScale(0, RoundingMode.HALF_UP)
                            .intValueExact();
                    
                    final Conta conta = contas[i];
                    final Lancamento lancamento = new Lancamento(fato,conta,valor);

                    System.out.printf("\t\t%s R$ %.2f\n",
                        conta.getNome(),
                        BigDecimal.valueOf(valor, 2).doubleValue());
                    
                    em.persist(lancamento);
                    
                    saldos[i]-=valor;
                }
                rowNum++;
            }

            
            //fechando
            final Fato fatoFechamento = new
                Fato(LocalDate.now(),"Erro importação");

            final Stack<Lancamento> lancamentosErros = new Stack<>();
            
            for (short i = 0;  i < numContas; i++) {
                final int saldo = saldos[i];
                if(saldo != 0){
                    Lancamento lancamento = 
                        new Lancamento(fatoFechamento, contas[i], saldo);
                    lancamentosErros.push(lancamento);
                    
                }
            }
            if(!lancamentosErros.empty()){
                em.persist(fatoFechamento);
                lancamentosErros.stream().forEach(em::persist);
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