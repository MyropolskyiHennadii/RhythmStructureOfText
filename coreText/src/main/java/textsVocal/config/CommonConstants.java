package textsVocal.config;

import org.springframework.beans.BeansException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import textsVocal.structure.Word;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@ConfigurationProperties
public final class CommonConstants implements ApplicationContextAware {

    private static ApplicationContext context;

    //=== fields =======================================
    private final String languageOfText = "ru";//language of the text
    private boolean thisIsVerse = true;//verse = true or prose = false
    private boolean readingFromFile = false;// read text from file or from ...
    private String portionSeparator = "//////////////////////////////////////////////////";
    private String fileInputDirectory = "c:\\Users\\Геннадий\\Documents\\Tests for RythmStructureOfText\\";//path to directory with input file
    private String fileInputName = "Хаджи Мурат — 1 порция.txt";//name of the input file
    private String fileOutputDirectory = "c:\\Users\\Геннадий\\Documents\\Tests for RythmStructureOfText\\";//path to directory with output file
    private String fileOutputName = "out_Хаджи Мурат — 1 порция.txt";//name of the output file
    private final String charsetName = "UTF-8";//UTF-8 and so on
    private boolean thisIsWebApp = false;//webapp = true
    private String textFromWebForm;//text from textarea in web-form

    private final boolean requireUnknownWordsByUser = true;//ask user abot unknown words or no
    private final int validLevelOfMainMeterGroupInVerseText = 65;//in %
    private final int validDifferenceBetweenTwoMainGroupsInVerseText = 30;//in %

    private static boolean alreadyRunning = false;//wheter app already run
    private static Map<String, Set<String>> tempWordDictionary = new HashMap<>();//temporary dictionary ("cash")
    private static final Set<Word> unKnownWords = new HashSet<>();//set of words with unknown stresses

    public CommonConstants() {
    }
//== getters ==
    public String getLanguageOfText() {
        return languageOfText;
    }

    public boolean isThisIsVerse() {
        return thisIsVerse;
    }

    public boolean isReadingFromFile() {
        return readingFromFile;
    }

    public String getFileInputDirectory() {
        return fileInputDirectory;
    }

    public String getFileInputName() {
        return fileInputName;
    }

    public String getFileOutputDirectory() {
        return fileOutputDirectory;
    }

    public String getFileOutputName() {
        return fileOutputName;
    }

    public String getCharsetName() {
        return charsetName;
    }

    public boolean isThisIsWebApp() {
        return thisIsWebApp;
    }

    public boolean isRequireUnknownWordsByUser() {
        return requireUnknownWordsByUser;
    }

    public int getValidLevelOfMainMeterGroupInVerseText() {
        return validLevelOfMainMeterGroupInVerseText;
    }

    public int getValidDifferenceBetweenTwoMainGroupsInVerseText() {
        return validDifferenceBetweenTwoMainGroupsInVerseText;
    }

    public String getPortionSeparator() {
        return portionSeparator;
    }

    public void setThisIsVerse(boolean thisIsVerse) {
        this.thisIsVerse = thisIsVerse;
    }

    public void setReadingFromFile(boolean readingFromFile) {
        this.readingFromFile = readingFromFile;
    }

    public void setPortionSeparator(String portionSeparator) {
        this.portionSeparator = portionSeparator;
    }

    public void setFileInputDirectory(String fileInputDirectory) {
        this.fileInputDirectory = fileInputDirectory;
    }

    public void setFileInputName(String fileInputName) {
        this.fileInputName = fileInputName;
    }

    public void setFileOutputDirectory(String fileOutputDirectory) {
        this.fileOutputDirectory = fileOutputDirectory;
    }

    public void setFileOutputName(String fileOutputName) {
        this.fileOutputName = fileOutputName;
    }

    public void setThisIsWebApp(boolean thisIsWebApp) {
        this.thisIsWebApp = thisIsWebApp;
    }

    public String getTextFromWebForm() {
        return textFromWebForm;
    }

    public void setTextFromWebForm(String textFromWebForm) {
        this.textFromWebForm = textFromWebForm;
    }

    public static ApplicationContext getApplicationContext() {
        return context;
    }

    public static Map<String, Set<String>> getTempWordDictionary() {
        return tempWordDictionary;
    }

    public static Set<Word> getUnKnownWords() {
        return unKnownWords;
    }

    public static boolean isAlreadyRunning() {
        return alreadyRunning;
    }

    public static void setAlreadyRunning(boolean alreadyRunning) {
        CommonConstants.alreadyRunning = alreadyRunning;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

}
