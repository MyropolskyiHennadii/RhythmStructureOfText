package textsVocal.dialogs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import textsVocal.ru.VocalAnalisysWordRu;
import textsVocal.utils.DBHelper;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

import static textsVocal.structure.TextForRythm.symbolOfNoStress;
import static textsVocal.structure.TextForRythm.symbolOfStress;

public class ConsoleDialog {

    //=== static fields ===
    private static final ApplicationContext context = new ClassPathXmlApplicationContext("beansTextsVocalUtil.xml");
    private static final DBHelper db = context.getBean(DBHelper.class);
    private static final Logger log = LoggerFactory.getLogger(VocalAnalisysWordRu.class);

    //=== non-static fields ===
    private final Scanner scanner = new Scanner(System.in);

    public String giveMePleaseStressSchema(String word, int duration) {
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
                System.out.println("Wrong durstion! Must be " + duration + ", and you have " + schema.length());
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

            String sql = "INSERT INTO " + db.getDb_TableUnKnownWords() + " (textWord, meterRepresentation) " + "VALUES ('" + word + "', '";
            try {
                Connection conn = db.getConnectionMainStressTable();
                Statement stmt = conn.createStatement();
                int nRecords = stmt.executeUpdate(sql + schema + "')");
                log.info("Added records "+nRecords + " in unknown words database. Word = " + word);
            } catch (SQLException e) {
                log.error("Something wrong with SQL!", e);
                e.getMessage();
            }
        }
        return schema;
    }

}
