package textsVocal.console.dbRuMaintenance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;
import textsVocal.config.CommonConstants;
import textsVocal.console.MainRythmConsoleApplication;
import textsVocal.ru.DB_RussianDictionary;
import textsVocal.utilsCore.ConsoleDialog;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import static textsVocal.ru.VocalAnalisysWordRu.calculateDurationOnlyVocale;
import static textsVocal.structure.TextForRythm.symbolOfNoStress;
import static textsVocal.structure.TextForRythm.symbolOfStress;

@ConfigurationPropertiesScan("coreText.textsVocal.config")
public class StockingOfDictionary {

    public static Scanner scanner;
    private static DB_RussianDictionary db;
    private static Connection conn;
    private static Set<String> words = new HashSet<>();

    private static final Logger log = LoggerFactory.getLogger(StockingOfDictionary.class);

    /**
     * select punkt menu from any list
     *
     * @param menu
     * @return
     */
    public static int selectPunctMenuFromConsole(Collection<HaveID> menu) {
        while (true) {
            Iterator<HaveID> it = menu.iterator();
            while (it.hasNext()) {
                System.out.println(it.next());
            }
            String answer = scanner.next();
            scanner.nextLine();
            try {
                return Integer.parseInt(answer);
            } catch (NumberFormatException e) {
                System.out.println("Impossible symbol in your answer! Repeat, please, your choice!");
            }

        }
    }

    /**
     * mapping word with form's endings and ancodes
     *
     * @param word
     * @param pattern
     * @return
     */
    public static Map<String, String> checkPatternAndAddRecordToMainDB(String word, DB_PatternItemRu pattern) {
        String sqlUnknownWords = "SELECT distinct id, textWord, MainForm, meterRepresentation, Ancode FROM " + db.getDb_Table() +
                " WHERE MainForm = '" + pattern.getMainForm() + "' AND partOfSpeech = '" + pattern.getPartOfSpeech() + "'";
        int countUnchangableSymbols = pattern.getMainForm().trim().length() - pattern.getNumberSymbolsInEndingMainForm();
        Map<String, String> wordForms = new HashMap<>();
        try {
            conn = db.getConnectionMainStressTable();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sqlUnknownWords);
            while (rs.next()) {
                String patternWord = rs.getString(2).trim();
                String patternMainForm = rs.getString(3).trim();
                if (patternWord.length() < patternMainForm.length()) {
                    continue;
                }

                String ending = "";
                for (int i = patternMainForm.length() - pattern.getNumberSymbolsInEndingMainForm(); i < patternWord.length(); i++) {
                    ending += patternWord.charAt(i);
                }
                String ancode = rs.getString(5).trim();
                wordForms.put(rs.getString(5).trim(), word.substring(0, word.length() - pattern.getNumberSymbolsInEndingMainForm()) + ending);
            }

        } catch (SQLException e) {
            log.error("Something wrong with ResultSet! {}", e.getMessage());
        }
        return wordForms;
    }

    /*            2020-04-06 16:45:01,021 [main] [INFO ] textsVocal.ru.VocalAnalisysWordRu - Существительное
            2020-04-06 16:45:01,022 [main] [INFO ] textsVocal.ru.VocalAnalisysWordRu - Междометие
            2020-04-06 16:45:01,022 [main] [INFO ] textsVocal.ru.VocalAnalisysWordRu - Союз
            2020-04-06 16:45:01,022 [main] [INFO ] textsVocal.ru.VocalAnalisysWordRu - Инфинитив
            2020-04-06 16:45:01,022 [main] [INFO ] textsVocal.ru.VocalAnalisysWordRu - Местоимение существительное
            2020-04-06 16:45:01,022 [main] [INFO ] textsVocal.ru.VocalAnalisysWordRu - Наречие
            2020-04-06 16:45:01,022 [main] [INFO ] textsVocal.ru.VocalAnalisysWordRu - Глагол в личной форме
            2020-04-06 16:45:01,023 [main] [INFO ] textsVocal.ru.VocalAnalisysWordRu - Прилагательное
            2020-04-06 16:45:01,023 [main] [INFO ] textsVocal.ru.VocalAnalisysWordRu - Предлог
            2020-04-06 16:45:01,023 [main] [INFO ] textsVocal.ru.VocalAnalisysWordRu - Числительное количественное
            2020-04-06 16:45:01,023 [main] [INFO ] textsVocal.ru.VocalAnalisysWordRu - Местоименное прилагательное
            2020-04-06 16:45:01,023 [main] [INFO ] textsVocal.ru.VocalAnalisysWordRu - Частица
            2020-04-06 16:45:01,023 [main] [INFO ] textsVocal.ru.VocalAnalisysWordRu - Причастие
            2020-04-06 16:45:01,023 [main] [INFO ] textsVocal.ru.VocalAnalisysWordRu - Деепричастие
            2020-04-06 16:45:01,023 [main] [INFO ] textsVocal.ru.VocalAnalisysWordRu - Вводное слово
            2020-04-06 16:45:01,023 [main] [INFO ] textsVocal.ru.VocalAnalisysWordRu - Краткое прилагательное
            2020-04-06 16:45:01,024 [main] [INFO ] textsVocal.ru.VocalAnalisysWordRu - Предикатив
            2020-04-06 16:45:01,024 [main] [INFO ] textsVocal.ru.VocalAnalisysWordRu - Фразеологизм
            2020-04-06 16:45:01,024 [main] [INFO ] textsVocal.ru.VocalAnalisysWordRu - Краткое причастие
            2020-04-06 16:45:01,024 [main] [INFO ] textsVocal.ru.VocalAnalisysWordRu - Числительное порядковое
            2020-04-06 16:45:01,024 [main] [INFO ] textsVocal.ru.VocalAnalisysWordRu - Местоимение предикатив*/

    public static void main(String[] args) {

        ApplicationContext context = SpringApplication.run(MainRythmConsoleApplication.class, args);
        CommonConstants commonConstants = (CommonConstants) context.getBean("commonConstants");
        commonConstants.setApplicationContext(context);
        scanner = context.getBean(ConsoleDialog.class).getScanner();

        //search unknown words
        db = context.getBean(DB_RussianDictionary.class);
        conn = null;
        ResultSet rs = null;
        Statement stmtInsert = null;//for records insert

        String sqlUnknownWords = "SELECT distinct textWord, meterRepresentation FROM " + db.getDb_TableUnKnownWords();
        String sqlInsert = "INSERT INTO " + db.getDb_Table() + " (textWord, partOfSpeech, MainForm, meterRepresentation, Ancode) " + "VALUES ('";
        try {
            conn = db.getConnectionMainStressTable();
            Statement stmt = conn.createStatement();
            stmtInsert = conn.createStatement();
            rs = stmt.executeQuery(sqlUnknownWords);
        } catch (SQLException e) {
            log.error("Something wrong with SQL! {}", e.getMessage());
        }

        System.out.println("==================================================");
        DB_PatternDataRu db_patternDataRu = new DB_PatternDataRu();

        int countRecords = 0;
        if (rs == null) {
            System.out.println("There are no records in UnknownWords table! Nothing to define!");
        } else {//there are records
            while (true) {
                try {
                    if (rs.next()) {
                        System.out.println("Beginning selection. If you want to stop, enter 0, to skip enter 999");
                        String word = rs.getString(1);

                        if (words.contains(word)) {
                            continue;
                        }

                        String meterRepresentation = rs.getString(2);
                        System.out.println("LOOKING PATTERN FOR WORD: " + word + ", meter schema " + meterRepresentation);
                        System.out.println("ENTER THE MAIN FORM FOR: " + word);
                        String mainForm = scanner.next();
                        scanner.nextLine();

                        if (mainForm.equals("0")) {
                            break;
                        }

                        if (mainForm.equals("999")) {
                            continue;
                        }

                        //select punct menu
                        int idPattern = selectPunctMenuFromConsole(db_patternDataRu.getItems());
                        if (idPattern == 0) {
                            break;
                        }
                        DB_PatternItemRu pattern = db_patternDataRu.getDB_PatternItemRuById(idPattern);
                        if (pattern == null) {
                            System.out.println("There is no pattern with id = " + idPattern);
                            break;
                        }

                        countRecords++;

                        Map<String, String> wordForms = checkPatternAndAddRecordToMainDB(mainForm, pattern);
                        String mainFormWithoutEnding = mainForm.substring(0, mainForm.length() - pattern.getNumberSymbolsInEndingMainForm());
                        int durationWordWithoutEnding = (int) calculateDurationOnlyVocale(mainFormWithoutEnding);
                        //System.out.println("Main form without ending "+mainFormWithoutEnding);
                        //System.out.println("durationWordWithoutEnding " + durationWordWithoutEnding);

                        String baseSchema = meterRepresentation.substring(0, durationWordWithoutEnding);
                        //System.out.println("baseSchema " + baseSchema);

                        for (Map.Entry<String, String> entry : wordForms.entrySet()) {
                            //System.out.println("ID =  " + entry.getKey() + ": " + entry.getValue());
                            int durationEnding = (int) calculateDurationOnlyVocale(entry.getValue()) - durationWordWithoutEnding;
                            String finalMeterSchema = baseSchema;
                            //System.out.println("duration ending = " + durationEnding);
                            for (int i = 0; i < durationEnding; i++) {
                                finalMeterSchema += "0";
                            }
                            if (("" + meterRepresentation.charAt(meterRepresentation.length() - 1)).equals("" + symbolOfStress)
                                    && !finalMeterSchema.contains("" + symbolOfStress)) {
                                finalMeterSchema = finalMeterSchema.substring(0, meterRepresentation.length() - 1) + symbolOfStress;
                                while (finalMeterSchema.length() < (int) calculateDurationOnlyVocale(entry.getValue())) {
                                    finalMeterSchema += symbolOfNoStress;
                                }
                            }
                            //String sqlInsert = "INSERT INTO " + db.getDb_Table() + " (textWord, partOfSpeech, MainForm, meterRepresentation, Ancode) " + "VALUES ('";
                            int nRecords = stmtInsert.executeUpdate(sqlInsert + entry.getValue().trim() + "', '"
                                    + pattern.getPartOfSpeech().trim() +  "', '"
                                    + mainForm.trim() + "', '"
                                    + finalMeterSchema.trim()  + "', '"
                                    + entry.getKey().trim() + "')");
                            //log.info("Added records " + nRecords + " in unknown words database. Word = " + word);
                            System.out.println(entry.getValue().trim() + "; " + finalMeterSchema + ";");
                        }
                        words.add(word);
                    }
                } catch (SQLException e) {
                    log.error("Somеthing wrong with ResultSet! {}", e.getMessage());
                }
                if (countRecords == 0) {
                    System.out.println("There are no records in UnknownWords table! Nothing to define!");
                }
            }
        }
    }
}
