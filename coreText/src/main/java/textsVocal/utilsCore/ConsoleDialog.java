package textsVocal.utilsCore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import textsVocal.config.CommonConstants;
import textsVocal.ru.DB_RussianDictionary;
import textsVocal.ru.VocalAnalisysWordRu;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

import static textsVocal.structure.TextForRythm.symbolOfNoStress;
import static textsVocal.structure.TextForRythm.symbolOfStress;

@Component
public class ConsoleDialog {

    private static final Logger log = LoggerFactory.getLogger(VocalAnalisysWordRu.class);

    //=== non-static fields ===
    private final Scanner scanner = new Scanner(System.in);

    //== constructor ==
    public ConsoleDialog() {
    }

    /**
     * dialog: ask user about meter representation of unknown word
     * @param word
     * @param duration
     * @return
     */
    public String giveMePleaseStressSchema(String word, int duration) {

        ApplicationContext context = CommonConstants.getApplicationContext();
        DB_RussianDictionary db = context.getBean(DB_RussianDictionary.class);

        boolean check = false;
        String schema = "";

        while (!check) {
            check = true;
            System.out.println("===================================");
            System.out.println("For Escape print N. Else enter stress schema for " + word.trim() + " (only symbols " + symbolOfStress + " and " + symbolOfNoStress + ").");
            schema = scanner.next();
            scanner.nextLine();
            schema = schema.trim();

            if (schema.equals("N")) {
                return schema;
            }
            if (schema.length() != duration) {
                System.out.println("Wrong duration! Must be " + duration + ", and you have " + schema.length());
            } else {
                for (int i = 0; i < schema.length(); i++) {
                    if (!schema.substring(i, i + 1).equals("" + symbolOfStress) && !schema.substring(i, i + 1).equals("" + symbolOfNoStress)) {
                        System.out.println("Wrong symbol " + schema.substring(i, i + 1));
                        check = false;
                    }
                }
            }
        }
        if (!schema.equals("N")) {
            String sql = "INSERT INTO " + db.getDb_TableUnKnownWords() + " (textWord, meterRepresentation) " + "VALUES ('" + word + "', '";
            try {
                Connection conn = db.getConnectionMainStressTable();
                Statement stmt = conn.createStatement();
                int nRecords = stmt.executeUpdate(sql + schema + "')");
                log.info("Added records "+nRecords + " in unknown words database. Word = " + word);
            } catch (SQLException e) {
                log.error("Something wrong with SQL! {}", e.getMessage());
            }
        }
        return schema;
    }

    //== getters ==
    public Scanner getScanner() {
        return scanner;
    }

}
