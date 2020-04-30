package textsVocal.structure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import textsVocal.config.CommonConstants;
import textsVocal.config.HeaderAnFooterListsForWebOutput;
import textsVocal.utilsCore.DefineStressInUnknownWordsWebController;

import java.beans.PropertyChangeSupport;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

import static textsVocal.structure.PortionOfTextAnalyser.outputAccumulation;
import static textsVocal.structure.ProsePortionForRythm.outputStressProfileOfWholeText;
import static textsVocal.structure.TextForRythm.SYMB_PARAGRAPH;

@Component
public class BuildingPortion {
    //=== fields =======================================
    private static int numPortion = 1;//start number of the portion
    private static final Logger log = LoggerFactory.getLogger(BuildingPortion.class);//logger

    private PropertyChangeSupport newUnKnownWord;//for listener in DefineStressInUnknownWordsController

    //=== setters and getters
    public int getNumPortion() {
        return numPortion;
    }
    public PropertyChangeSupport getNewUnKnownWord() {
        return newUnKnownWord;
    }

    /**
     * build portion in verse by separator
     */
    public void buildVersePortions(String testText, CommonConstants constants) throws ExecutionException, InterruptedException, IOException {

        boolean readingFromFile = constants.isReadingFromFile();
        String languageOfText = constants.getLanguageOfText();
        int numPortion = getNumPortion();
        String directoryInput = constants.getFileInputDirectory();
        String fileInputName = constants.getFileInputName();
        String directoryOuput = constants.getFileOutputDirectory();
        String fileOutputName = constants.getFileOutputName();
        String charsetName = constants.getCharsetName();
        String portionSeparator = constants.getPortionSeparator();

        //if user didn't give portion separator = there is only one portion
        if(portionSeparator.trim().isEmpty()){
            portionSeparator = "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%";
        }

        if (!readingFromFile) {
            PortionOfTextAnalyser.portionAnalysys(numPortion, testText, constants);
        } else {

            Path textPath = Paths.get(directoryInput, fileInputName);
            if (!Files.exists(textPath)) {
                log.error("There is no file " + textPath);
                throw new FileNotFoundException("There is no file " + textPath);
            }
            if (!Files.isReadable(textPath)) {
                log.error("Impossible to read file " + textPath);
                throw new FileNotFoundException("Impossible to read file " + textPath);
            }

            // find portion in verse by separator and in prose by paraghaph
            File input = new File(textPath.toString());
// read the content from file
            int k = 0;
            try (BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(input), charsetName))) {
                StringBuilder sPortion = new StringBuilder("");
                String line = bufferedReader.readLine().trim();

                while (line != null) {
                    if (line.isEmpty()) {
                        line = bufferedReader.readLine();
                        continue;
                    }

                    if (sPortion.toString().isEmpty() && line.contains(portionSeparator)) {
                        line = bufferedReader.readLine();
                        continue;
                    }

                    if ((k == 0) && (int) line.charAt(0) == 65279 && line.length() <= 1) {
                        line = bufferedReader.readLine();
                        k++;
                        continue;
                    }
                    k++;

                    if (!line.contains(portionSeparator)) {
                        sPortion.append(line);
                        sPortion.append((char) 12);
                    } else {
                        PortionOfTextAnalyser
                                .portionAnalysys(numPortion, sPortion.toString().trim(), constants);
                        sPortion.delete(0, sPortion.length());
                        numPortion++;
                        k = 0;
                    }
                    line = bufferedReader.readLine();
                }

                //last portion
                if (!sPortion.toString()
                        .isEmpty()) {
                    PortionOfTextAnalyser
                            .portionAnalysys(numPortion, sPortion.toString().trim(), constants);
                }
            } catch (FileNotFoundException e) {
                log.error("Undefinite FileNotFoundExctption in main", e);
                throw e;
            } catch (IOException e) {
                log.error("Undefinite IOException in main", e);
                throw e;
            }
        }
    }

    /**
     * build portion in prose by paraghaph
     */
    public void buildProsePortions(String testText, CommonConstants constants) throws ExecutionException, InterruptedException, IOException {

        boolean readingFromFile = constants.isReadingFromFile();
        String languageOfText = constants.getLanguageOfText();
        int numPortion = getNumPortion();
        String directoryInput = constants.getFileInputDirectory();
        String fileInputName = constants.getFileInputName();
        String directoryOuput = constants.getFileOutputDirectory();
        String fileOutputName = constants.getFileOutputName();
        String charsetName = constants.getCharsetName();
        CharSequence[] portionSeparator = SYMB_PARAGRAPH;

        if (!readingFromFile) {
            String portionText = "";
            for (int i = 0; i < testText.length(); i++) {
                boolean mustAdd = true;
                for (int j = 0; j < SYMB_PARAGRAPH.length; j++) {
                    if (("" + testText.charAt(i)).equals(SYMB_PARAGRAPH[j])) {
                        if (!portionText.isEmpty()) {
                            PortionOfTextAnalyser.portionAnalysys(numPortion, portionText, constants);
                            portionText = "";
                            mustAdd = false;
                            numPortion++;
                        }
                    }
                }
                if (mustAdd) {
                    portionText += testText.charAt(i);
                }
            }
            //last portion
            if (!portionText.isEmpty()) {
                PortionOfTextAnalyser.portionAnalysys(numPortion, portionText, constants);
                numPortion++;
            }

        } else {

            Path textPath = Paths.get(directoryInput, fileInputName);
            if (!Files.exists(textPath)) {
                log.error("There is no file " + textPath);
                throw new FileNotFoundException("There is no file " + textPath);
            }
            if (!Files.isReadable(textPath)) {
                log.error("Impossible to read file " + textPath);
                throw new FileNotFoundException("Impossible to read file " + textPath);
            }

            // find portion in prose by paraghaph
            File input = new File(textPath.toString());

// read the content from file
            try (BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(input), charsetName))) {
                StringBuilder sPortion = new StringBuilder("");
                String line = bufferedReader.readLine().trim();

                while (line != null) {
                    if (line.isEmpty()) {
                        line = bufferedReader.readLine();
                        continue;
                    }
                    PortionOfTextAnalyser.portionAnalysys(numPortion, line, constants);
                    numPortion++;
                    line = bufferedReader.readLine();
                }
            } catch (FileNotFoundException e) {
                log.error("Undefinite FileNotFoundExctption in main", e);
                throw e;
            } catch (IOException e) {
                log.error("Undefinite IOException in main", e);
                throw e;
            }

        }
        outputStressProfileOfWholeText(outputAccumulation, constants);

    }

    public void startPortionBuilding(String testText, CommonConstants constants) throws InterruptedException, ExecutionException, IOException {

        //to listener:
        newUnKnownWord = new PropertyChangeSupport(this);
        ApplicationContext context = CommonConstants.getApplicationContext();
        DefineStressInUnknownWordsWebController controller = context.getBean(DefineStressInUnknownWordsWebController.class);
        newUnKnownWord.addPropertyChangeListener(controller);
        newUnKnownWord.firePropertyChange("word","###", "###BeginBuildPortion###");

        if ( constants.isThisIsWebApp()){
            //clean static fields
            HeaderAnFooterListsForWebOutput.getPortionFooters().clear();
            HeaderAnFooterListsForWebOutput.getPortionHeaders().clear();
            PortionOfTextAnalyser.getListOfInstance().clear();
        }
        if (constants.isThisIsVerse()) {
            buildVersePortions(testText, constants);
        } else {// build portion in verse by separator and in prose by paraghaph!!!
            buildProsePortions(testText, constants);
        }
        newUnKnownWord.firePropertyChange("word","###", "###EndBuildPortion###");
    }
}
