package textsVocal.dialogs;

import java.util.Scanner;

import static textsVocal.structure.TextForRythm.symbolOfNoStress;
import static textsVocal.structure.TextForRythm.symbolOfStress;

public class ConsoleDialog {

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
        return schema;
    }
}
