package textsVocal.utilsCommon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;

public class FileTreatment {

    private static final Logger log = LoggerFactory.getLogger(FileTreatment.class);

    public static void outputResultToFile(StringBuilder outputAccumulation, String pathToFileOutput) {
        try (java.io.FileWriter fw = new java.io.FileWriter(pathToFileOutput, true)) {
            fw.write(outputAccumulation.toString());
        } catch (
                FileNotFoundException e) {
            log.error("Something wrong with output!" + e.getMessage());
            e.getMessage();
        } catch (
                IOException e) {
            log.error("Something wrong with output!" + e.getMessage());
            e.getMessage();
        }
    }
}
