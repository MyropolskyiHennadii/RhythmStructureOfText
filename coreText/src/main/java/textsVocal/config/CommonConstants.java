package textsVocal.config;

import org.springframework.beans.BeansException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import textsVocal.ru.DB_RussianDictionary;
import textsVocal.ru.VocalAnalisysRu;
import textsVocal.structure.Word;

import java.util.*;
import java.util.stream.Collectors;

import static textsVocal.structure.TextPortionForRhythm.symbolOfNoStress;
import static textsVocal.structure.TextPortionForRhythm.symbolOfStress;

/**
 * common constants of application
 */
@ConfigurationProperties
public final class CommonConstants implements ApplicationContextAware {

    private static final Map<String, Set<String>> tempWordDictionary = new HashMap<>();//temporary dictionary ("cash")
    private static final Set<Word> unKnownWords = new HashSet<>();//set of words with unknown stresses
    private static Locale webLocale;//Locale from the web app
    private static ApplicationContext context;
    //=== fields =======================================
    private final String languageOfText = "ru";//language of the text
    private final String charsetName = "UTF-8";//UTF-8 and so on
    private final boolean requireUnknownWordsByUser = true;//ask user about unknown words or no - only in console
    private final int validLevelOfMainMeterGroupInVerseText = 65;//in %
    private final int validDifferenceBetweenTwoMainGroupsInVerseText = 30;//in %
    //=== (in web-app fill auto) =======================
    private boolean thisIsVerse = true;//verse = true or prose = false
    private boolean readingFromFile = true;// read text from file or from ...
    private String portionSeparator = "//////////////////////////////////////////////////";
    private String fileInputDirectory = "c:\\Users\\Геннадий\\Documents\\Tests for RythmStructureOfText\\";//path to directory with input file
    private String fileInputName = "Kuzmin Verses.txt";//name of the input file
    private String fileOutputDirectory = "c:\\Users\\Геннадий\\Documents\\Tests for RythmStructureOfText\\";//path to directory with output file
    private String fileOutputName = "out_Kuzmin Verses.txt";//name of the output file
    private boolean thisIsWebApp = false;//webapp = true
    private String textFromWebForm;//text from textarea in web-form

    public CommonConstants() {
    }

    public static ApplicationContext getApplicationContext() {
        return context;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    public static Map<String, Set<String>> getTempWordDictionary() {
        return tempWordDictionary;
    }

    public static Set<Word> getUnKnownWords() {
        return unKnownWords;
    }

    public static Locale getWebLocale() {
        return webLocale;
    }

    public static void setWebLocale(Locale locale) {
        webLocale = locale;
    }

    /**
     * updating TempWordDictionary from table with web-defined stress schema
     *
     * @param listStressSchema list web-defined stress schema
     */
    public static void updateTempWordDictionaryWithUsersDefinition(List<String> listStressSchema) {
        List<String> unknownWords = getUnKnownWords().stream().map(Word::getTextWord).collect(Collectors.toList());
        for (int i = 0; i < listStressSchema.size(); i++) {
            String s = listStressSchema.get(i);
            String word = unknownWords.get(i);
            if (s.trim().isEmpty()) {
                continue;
            }
            if (s.equals("N")) {
                continue;
            }
            int duration = (int) VocalAnalisysRu.calculateDurationOnlyVocale(word);
            boolean check = true;
            if (s.length() != duration) {
                continue;
            } else {
                for (int j = 0; j < s.length(); j++) {
                    if (!s.substring(j, j + 1).equals("" + symbolOfStress) && !s.substring(j, j + 1).equals("" + symbolOfNoStress)) {
                        check = false;
                        break;
                    }
                }
            }
            if (!check) {
                continue;
            }
            DB_RussianDictionary.addNewRecordToUnknownWordsDataBase(word, s);

            //adding to temporary dictionary:
            Set<String> serviceSet = new HashSet<>();
            serviceSet.add(s);
            getTempWordDictionary().put(word, serviceSet);
        }
    }

    /**
     *
     * @return ResourceBundle from Locale
     */
    public static ResourceBundle getResourceBundle() {
        ApplicationContext context = getApplicationContext();
        CommonConstants constants = context.getBean(CommonConstants.class);
        Locale currentLocale = new Locale("en");
        if (constants.getWebLocale() != null) {
            currentLocale = constants.getWebLocale();
        }
        return ResourceBundle.getBundle("messages", currentLocale);
    }

    //== getters and setters ==
    public String getLanguageOfText() {
        return languageOfText;
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

    public boolean isThisIsWebApp() {
        return thisIsWebApp;
    }

    public void setThisIsWebApp(boolean thisIsWebApp) {
        this.thisIsWebApp = thisIsWebApp;
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

    public void setPortionSeparator(String portionSeparator) {
        this.portionSeparator = portionSeparator;
    }

    public String getTextFromWebForm() {
        return textFromWebForm;
    }

    public void setTextFromWebForm(String textFromWebForm) {
        this.textFromWebForm = textFromWebForm;
    }

}
