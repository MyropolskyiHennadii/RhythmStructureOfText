package textsVocal.console;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import textsVocal.structure.PortionOfTextAnalyser;
import textsVocal.structure.TextForRythm;
import textsVocal.structure.Word;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

public class MainRythmConsole {

    //=== fields =======================================
    private String languageOfText;//language of the text
    private int numPortion;//start number of the portion
    private boolean thisIsVerse;//verse = true or prose = false
    private boolean readingFromFile;// read text from file or from ...
    private String fileInputDirectory;//path to directory with input file
    private String fileInputName;//name of the input file
    private String fileOutputDirectory;//path to directory with output file
    private String fileOutputName;//name of the output file
    private String charsetName;//UTF-8 and so on
    private String portionSeparator;//separator of text's portions

    private static final Logger log = LoggerFactory.getLogger(MainRythmConsole.class);//logger

    //=== setters and getters
    public String getLanguageOfText() {
        return languageOfText;
    }

    public void setLanguageOfText(String languageOfText) {
        this.languageOfText = languageOfText;
    }

    public int getNumPortion() {
        return numPortion;
    }

    public void setNumPortion(int numPortion) {
        this.numPortion = numPortion;
    }

    public boolean isThisIsVerse() {
        return thisIsVerse;
    }

    public void setThisIsVerse(boolean thisIsVerse) {
        this.thisIsVerse = thisIsVerse;
    }

    public boolean isReadingFromFile() {
        return readingFromFile;
    }

    public void setReadingFromFile(boolean readingFromFile) {
        this.readingFromFile = readingFromFile;
    }

    public String getFileInputDirectory() {
        return fileInputDirectory;
    }

    public void setFileInputDirectory(String fileInputDirectory) {
        this.fileInputDirectory = fileInputDirectory;
    }

    public String getFileInputName() {
        return fileInputName;
    }

    public void setFileInputName(String fileInputName) {
        this.fileInputName = fileInputName;
    }

    public String getFileOutputDirectory() {
        return fileOutputDirectory;
    }

    public void setFileOutputDirectory(String fileOutputDirectory) {
        this.fileOutputDirectory = fileOutputDirectory;
    }

    public String getFileOutputName() {
        return fileOutputName;
    }

    public void setFileOutputName(String fileOutputName) {
        this.fileOutputName = fileOutputName;
    }

    public String getCharsetName() {
        return charsetName;
    }

    public void setCharsetName(String charsetName) {
        this.charsetName = charsetName;
    }

    public String getPortionSeparator() {
        return portionSeparator;
    }

    public void setPortionSeparator(String portionSeparator) {
        this.portionSeparator = portionSeparator;
    }

    /**
     * start
     *
     * @param args the command line arguments
     * @throws java.lang.InterruptedException
     * @throws java.util.concurrent.ExecutionException
     * @throws java.io.FileNotFoundException
     */
    public static void main(String[] args) throws InterruptedException, ExecutionException, FileNotFoundException, IOException {

        String testText = "Любви, надежды, тихой славы" + (char) 12
                + "Недолго нежил нас обман," + (char) 12
                + "Исчезли юные забавы," + (char) 12
                + "Как сон, как утренний туман;" + (char) 13
                + "Но в нас горит еще желанье," + (char) 10
                + "Под гнетом власти роковой" + (char) 10
                + "Нетерпеливою душой" + (char) 12
                + "Отчизны внемлем призыванье.";

        log.info("Beginning main...");
        ApplicationContext context = new ClassPathXmlApplicationContext("beansMainRythmConsole.xml");
        MainRythmConsole startObject = context.getBean(MainRythmConsole.class);

        boolean thisIsVerse = startObject.isThisIsVerse();
        boolean readingFromFile = startObject.isReadingFromFile();
        String languageOfText = startObject.getLanguageOfText();
        int numPortion = startObject.getNumPortion();
        String directoryInput = startObject.getFileInputDirectory();
        String fileInputName = startObject.getFileInputName();
        String directoryOuput = startObject.getFileOutputDirectory();
        String fileOutputName = startObject.getFileOutputName();
        String charsetName = startObject.getCharsetName();
        String portionSeparator = startObject.getPortionSeparator();


        if (!readingFromFile) {
            PortionOfTextAnalyser.portionAnalysys(numPortion, testText, thisIsVerse, "", languageOfText);
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
                                .portionAnalysys(numPortion, sPortion.toString().trim(), thisIsVerse, directoryOuput + fileOutputName, languageOfText);
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
                            .portionAnalysys(numPortion, sPortion.toString().trim(), thisIsVerse, directoryOuput + fileOutputName, languageOfText);
                }
            } catch (FileNotFoundException e) {
                log.error("Undefinite FileNotFoundExctption in main", e);
                throw e;
            } catch (IOException e) {
                log.error("Undefinite IOException in main", e);
                throw e;
            }

        }


        //publish unknown words
        if (TextForRythm.unKnownWords.size() > 0) {
            log.info("====== Unknown words ======");
            for (Word w : TextForRythm.unKnownWords) {
                log.info(w.toString());
            }
        }

        log.info("End main ...");
    }
}
