package textsVocal.utilsCore;

import org.springframework.stereotype.Component;
import textsVocal.ru.DB_RussianDictionary;

import java.util.Scanner;

import static textsVocal.structure.TextPortionForRythm.symbolOfNoStress;
import static textsVocal.structure.TextPortionForRythm.symbolOfStress;

/**
 * class for dialog with user in console-case
 */
@Component
public class ConsoleDialog {

    //=== non-static fields ===
    private final Scanner scanner = new Scanner(System.in);

    //== constructor ==
    public ConsoleDialog() {
    }

    /**
     * dialog: ask user about meter representation of unknown word
     * @param word word we want to define stress schema
     * @param duration duration of the word in syllables
     * @return stress schema
     */
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
                System.out.println("Wrong duration! Must be " + duration + ", and you have " + schema.length());
            } else {
                for (int i = 0; i < schema.length(); i++) {
                    if (!schema.substring(i, i + 1).equals("" + symbolOfStress) && !schema.substring(i, i + 1).equals("" + symbolOfNoStress)) {
                        System.out.println("Wrong symbol " + schema.substring(i, i + 1));
                        check = false;
                        break;
                    }
                }
            }
        }
        if (!schema.equals("N")) {
            DB_RussianDictionary.addNewRecordToUnknownWordsDataBase(word, schema);
        }
        return schema;
    }

    //== getters ==
    public Scanner getScanner() {
        return scanner;
    }

}
