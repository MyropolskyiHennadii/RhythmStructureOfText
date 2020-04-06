package textsVocal.dialogs;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import textsVocal.utils.DBHelper;

import java.util.Scanner;

import static textsVocal.structure.TextForRythm.symbolOfNoStress;
import static textsVocal.structure.TextForRythm.symbolOfStress;

public class ConsoleDialog {

    private static final ApplicationContext context = new ClassPathXmlApplicationContext("beansTextsVocalUtil.xml");
    private static final DBHelper db = context.getBean(DBHelper.class);
    private final Scanner scanner = new Scanner(System.in);

    public String giveMePleaseStressSchema(String word, int duration) {
        boolean check = false;
        String schema = "";


        //ApplicationContext context = new ClassPathXmlApplicationContext("beansTextsVocalUtil.xml");
        //DBHelper db = context.getBean(DBHelper.class);

        //String sql = "SELECT distinct textWord, meterRepresentation, partOfSpeech FROM " + db.getDb_Table() + " WHERE textWord in (";

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
       /* if (!schema.equals("N")){

        }*/
        return schema;
    }
}
