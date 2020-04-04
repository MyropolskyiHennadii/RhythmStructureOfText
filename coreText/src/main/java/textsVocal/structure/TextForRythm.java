package textsVocal.structure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import textsVocal.dialogs.ConsoleDialog;
import textsVocal.structure.ru.VocalAnalisysWordRu;
import textsVocal.utils.DynamicTableRythm;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public abstract class TextForRythm {

    //== fields ==================================================================
    public static final char symbolOfStress = '1';
    public static final char symbolOfNoStress = '0';
    public static final String[] stressRepresentations = {"" + symbolOfNoStress, "" + symbolOfStress};
    public static final Set<Word> unKnownWords = new HashSet<>();//set of words with unknown stresses
    public static final CharSequence[] SYMB_SEGMENT = {"" + (char) 10, "" + (char) 12, "" + (char) 13};// unicodes return
    public static final CharSequence[] SYMB_SPACE = {"" + (char) 32, "" + (char) 160, "" + (char) 9};// unicodes spaces
    // strings with different marks end of sentence
    public static final CharSequence[] SYMB_SENTENCE = {"!!!", "???", "...", "??!", "??", "?!", "!!", "!", "?",
            ".", ";"};
    // punctuation
    public static final CharSequence[] SYMB_PUNCTUATION = {"!", "?", ".", ",", ".", ";",
            ":", "'", "-", "+", "(", ")", "[", "]", "#", "@", "{", "}", "$", "%", "&", "^", "/", "|",
            "*", "" + (char) 92, "" + (char) 34, "_", "<", ">", "" + (char) 8212, "" + (char) 65279,
            "" + (char) -1};
    //brief punctuation
    public static final CharSequence[] SYMB_BRIEF_PUNCTUATION = {",", ".", ";", ":", "!", "?", "-", "_", "" + (char) 8212, "" + (char) -1, "" + (char) 65279};

    private static Logger log = LoggerFactory.getLogger(VersePortionForRythm.class);//logger

    private final String pText = "";//original text

    private DynamicTableRythm dtOfTextSegmentsAndStresses;// table with all segmentations
    private List<SegmentOfPortion> listOfSegments;//list of segments
    private boolean requireUnknownWordsByUser;//require users about unknown words or not

    //== enums ================================================================
    //must have to define verse meter
    public static enum verseMeterPatterns {

        Trochee("10"), Iambus("01"), Dactyl("100"), Amphibrach("010"), Anapaest("001"),
        PentonI("10000"), PentonII("01000"), PentonIII("00100"), PentonIV("00010"), PentonV("00001");

        private final String pattern;

        private verseMeterPatterns(String m) {
            this.pattern = m;
        }

        public String getPattern() {
            return pattern;
        }

        public int getDuration() {
            return pattern.length();
        }
    }

    //== getters and setters ===================================================

    public String getpText() {
        return pText;
    }

    public static char getSymbolOfStress() {
        return symbolOfStress;
    }

    public List<SegmentOfPortion> getListOfSegments() {
        return listOfSegments;
    }

    public void setListOfSegments(List<SegmentOfPortion> listOfSegments) {
        this.listOfSegments = listOfSegments;
    }

    public DynamicTableRythm getDtOfTextSegmentsAndStresses() {
        return dtOfTextSegmentsAndStresses;
    }

    public void setDtOfTextSegmentsAndStresses(DynamicTableRythm dtOfTextSegmentsAndStresses) {
        this.dtOfTextSegmentsAndStresses = dtOfTextSegmentsAndStresses;
    }

    public static Logger getLog() {
        return log;
    }

    public boolean isRequireUnknownWordsByUser() {
        return requireUnknownWordsByUser;
    }

    public void setRequireUnknownWordsByUser(boolean requireUnknownWordsByUesr) {
        this.requireUnknownWordsByUser = requireUnknownWordsByUesr;
    }

    //== static methods =========================================================
    public static CharSequence[] getSYMB_SEGMENT() {
        return SYMB_SEGMENT;
    }

    public static CharSequence[] getSYMB_SPACE() {
        return SYMB_SPACE;
    }

    public static CharSequence[] getSYMB_SENTENCE() {
        return SYMB_SENTENCE;
    }

    public static CharSequence[] getSYMB_PUNCTUATION() {
        return SYMB_PUNCTUATION;
    }

    public static CharSequence[] getSYMB_BRIEF_PUNCTUATION() {
        return SYMB_BRIEF_PUNCTUATION;
    }


    /**
     * as split(), but not split(), because of 'any' segment for segments
     *
     * @param pTextForConsideration
     * @param pSeparators
     * @return
     */
    public static String textFragmentToDelimiter(StringBuilder pTextForConsideration, CharSequence[] pSeparators) {
        String pText = pTextForConsideration.toString();

        for (int i = 0; i < pText.length(); i++) {
            char s = pText.charAt(i);
            for (CharSequence delimiter : pSeparators) {
                if ((delimiter.length() == 1) && (delimiter.charAt(0) == s)) {
                    pTextForConsideration.delete(0, i + 1);
                    return pText.substring(0, i + 1);
                } else if ((delimiter.length() + i) < pTextForConsideration.length()) {
                    if (pTextForConsideration.subSequence(i, i + delimiter.length()).toString()
                            .equalsIgnoreCase(delimiter.toString())) {
                        pTextForConsideration.delete(0, i + delimiter.length() + 1);
                        return pText.substring(0, i + delimiter.length() + 1);
                    }
                }
            }
        }
        return null;
    }

    /**
     * have to prepare string: probable string has uncorrect format, uncorrect spaces with punctuation "...! abc", but not "... !abc"
     *
     * @param _Text
     * @param pSeparators
     * @param pSpace
     * @return
     */
    public static StringBuilder prepareStringForParsing(String _Text, CharSequence[] pSeparators, CharSequence[] pSpace) {

        _Text = _Text.trim();
        StringBuilder pText = new StringBuilder(_Text.length());

        // replaceAll() with "???" doesn't work, that's why:
        boolean wasChanging = false;
        for (int i = 0; i < _Text.length(); i++) {
            char s = _Text.charAt(i);
            wasChanging = false;

            for (CharSequence delimiter : pSeparators) {
                if (delimiter.charAt(0) == s) {
                    pText.append(s).append(" ");
                    wasChanging = true;
                    break;
                }
            }
            if (!wasChanging) {
                pText.append(s);
            }
        }

        // because of the string like "abc ! ! !"
        pText.reverse();
        int finishdLength = 0;
        int beginningLength = pText.length();

        String fragment = "";
        int posRedundantSpace = 0;

        do {
            beginningLength = pText.length();
            for (CharSequence delimiter : pSeparators) {
                for (CharSequence space : pSpace) {
                    fragment = "" + delimiter + space;
                    posRedundantSpace = pText.indexOf(fragment);
                    if (posRedundantSpace >= 0) {
                        pText.deleteCharAt(posRedundantSpace + 1);
                    }
                }
            }
            finishdLength = pText.length();
        } while (finishdLength != beginningLength);

        pText.reverse();
        finishdLength = 0;
        do {
            beginningLength = pText.length();
            for (CharSequence delimiter : pSeparators) {
                for (CharSequence space : pSpace) {
                    fragment = "" + delimiter + space + space;
                    posRedundantSpace = pText.indexOf(fragment);
                    if (posRedundantSpace >= 0) {
                        pText.deleteCharAt(posRedundantSpace + 2);
                    }
                }
            }
            finishdLength = pText.length();
        } while (finishdLength != beginningLength);

        return pText;
    }

    /**
     * punctuation - away
     *
     * @param word
     * @param pSeparators
     * @param <T>
     * @return
     */
    public static <T> String cleanWordFromPunctuation(T word, CharSequence[] pSeparators) {
        String pWord = word.toString().toLowerCase().trim();
        for (int i = 0; i < pWord.length(); i++) {
            char s = pWord.charAt(i);

            for (CharSequence delimiter : pSeparators) {
                if (delimiter.charAt(0) == s) {
                    pWord = pWord.substring(0, i) + " " + pWord.substring(i + 1);
                }
            }
        }

        return pWord.trim();
    }

    public static <T> String cleanTextFromSymbols(T pText, List<CharSequence[]> pSeparators) {
        String Text = pText.toString().trim();

        List<CharSequence> allChars = new ArrayList<>();
        pSeparators.stream().forEach((ch) -> {
            allChars.addAll(Arrays.asList(ch));
        });

        for (int i = 0; i < Text.length(); i++) {
            char s = Text.charAt(i);

            for (CharSequence delimiter : allChars) {
                if (delimiter.charAt(0) == s) {
                    Text = Text.substring(0, i) + " " + Text.substring(i + 1);
                }
            }
        }

        return Text.trim();
    }

    /**
     * as split(), but not split() for words
     *
     * @param pText
     * @param pSeparators
     * @return
     */
    public static List<String> StringToWords(String pText, CharSequence[] pSeparators) {
        List<String> listOfWords = new ArrayList<>();
        String Word;

        int begIndex = 0;
        for (int i = 0; i < pText.length(); i++) {
            char s = pText.charAt(i);
            for (CharSequence delimiter : pSeparators) {
                if ((delimiter.length() == 1) && (delimiter.charAt(0) == s)) {
                    Word = pText.substring(begIndex, i).trim();
                    if (!Word.isEmpty()) {
                        listOfWords.add(Word);
                    }
                    begIndex = i + 1;
                } else if ((delimiter.length() + i) < pText.length()) {
                    if (pText.subSequence(i, i + delimiter.length()).toString()
                            .equalsIgnoreCase(delimiter.toString())) {
                        Word = pText.substring(begIndex, i).trim();
                        if (!Word.isEmpty()) {
                            listOfWords.add(Word);
                        }
                        begIndex = i + delimiter.length() + 1;
                    }
                }
            }
        }
        //last word:
        listOfWords.add(pText.substring(begIndex, pText.length()).trim());
        return listOfWords;
    }

    //create dynamic table with all data about portion of text
    public static DynamicTableRythm CreateDynamicTableOfPortionSegmentsAndStresses(TextForRythm instance, String language) throws InterruptedException, ExecutionException {

        ApplicationContext context = new ClassPathXmlApplicationContext("beansCoreText.xml");
        ConsoleDialog consoleDialog = context.getBean(ConsoleDialog.class);
        String entranceText = instance.getpText();
        ExecutorService exService = Executors.newCachedThreadPool();

        //1. parallel: receive stresses and dynamic table without stresses
        Future<Map<String, Set<String>>> stresses = exService.submit(() -> {//stream for stress to find
                    List<CharSequence[]> allChars = new ArrayList<>();
                    allChars.add(SYMB_SEGMENT);
                    allChars.add(SYMB_PUNCTUATION);
                    String pText = cleanTextFromSymbols(entranceText, allChars);
                    List<String> words = StringToWords(pText, SYMB_SPACE);
                    if (language.equals("ru")) {
                        VocalAnalisysWordRu vo = new VocalAnalisysWordRu(words);
                        return vo.getRythmSchemaOfTheText();
                    } else {
                        log.error(language + "! Unknown language! Impossible to build DynamicTableRythm.", IllegalArgumentException.class);
                        throw new IllegalArgumentException(language + "! Unknown language! Impossible to build DynamicTableRythm.");
                    }
                }
        );

        //2. parallel: receive stresses and dynamic table without stresses
        Future<DynamicTableRythm> dyntable = exService.submit(() -> {
            return instance.parsePortionOfText();
        });

        Map<String, Set<String>> mapStress = stresses.get();
        DynamicTableRythm dt = dyntable.get();

        exService.shutdown();

        if (dt.getSize() == 0) {
            throw new IllegalArgumentException("Empty dynamic table!");
        }

        //full object Words with stress and so on...
        List<String> words = dt.getColumn("Word");
        List<Word> objWords = dt.getColumn("Word-object / Stress form");
        int duration = 0;

        for (int i = 0; i < words.size(); i++) {
            String textWord = cleanWordFromPunctuation(words.get(i).toLowerCase().trim(), SYMB_PUNCTUATION);//database have the same change ё on е

            Word objWord = objWords.get(i);
            objWord.setTextWord(textWord);
            duration = (int) VocalAnalisysWordRu.calculateDurationOnlyVocale(textWord.replaceAll("ё", "е"));
            objWord.setDuration(duration);
            objWord.setNumSyllable(duration);
            String reprParticularCasesOfWords = VocalAnalisysWordRu.particularCasesOfWords(textWord);

            if (duration == 0) {
                objWord.addMeterRepresentation("");
                objWord.setMeterRepresentationForUser("");
            } else if (!reprParticularCasesOfWords.isEmpty()) {
                objWord.addMeterRepresentation(reprParticularCasesOfWords);
                objWord.setMeterRepresentationForUser(reprParticularCasesOfWords);
            } else if (duration == 1) {
                objWord.addMeterRepresentation(stressRepresentations[0]);//always doubt: may be so, may be other
                objWord.addMeterRepresentation(stressRepresentations[1]);
                objWord.setMeterRepresentationForUser("x");
            } else {
                Set<String> findInDB = mapStress.get(textWord);

                if (findInDB != null) {
                    int sizeFind = findInDB.size();

                    for (String s : findInDB) {
                        objWord.addMeterRepresentation(s);
                        objWord.setMeterRepresentationForUser(s + (sizeFind == 1 ? "" : ". One of: " + sizeFind));
                    }
                } else {//unknown word
                    if (instance.isRequireUnknownWordsByUser()) {//try to ask user about...
                        String schema = consoleDialog.giveMePleaseStressSchema(textWord, duration);
                        if(schema.equals("N")){
                            addUnkownWordToSet(objWord, textWord, duration);
                        } else {
                            objWord.addMeterRepresentation(schema);
                            objWord.setMeterRepresentationForUser(schema);
                        }
                    } else {//add word to set of unknown words
                        addUnkownWordToSet(objWord, textWord, duration);
                    }
                }
            }

        }

        return dt;
    }

    /**
     * service method for adding unknown word to set
     * @param objWord
     */
    private static void addUnkownWordToSet(Word objWord, String textWord, int duration){
        objWord.setMeterRepresentationForUser(textWord);
        String pMeterRepresentation = "";
        //all cases
        for (int j = 0; j < duration; j++) {
            pMeterRepresentation += symbolOfNoStress;
        }
        for (int j = 0; j < duration; j++) {
            String pCase = pMeterRepresentation.substring(0, j) + symbolOfStress + pMeterRepresentation.substring(j + 1, duration);
            objWord.addMeterRepresentation(pCase);
        }
        //objWord.addMeterRepresentation(pMeterRepresentation);
        pMeterRepresentation = "";
        //all cases
        for (int j = 0; j < duration; j++) {
            pMeterRepresentation += "?";
        }
        objWord.setMeterRepresentationForUser(pMeterRepresentation);
        unKnownWords.add(objWord);
    }

    /**
     * for every segment build lines with stresses 01010101...001100. May be there are few for every
     *
     * @param dt
     * @param nameColumnSegmentIdentifier
     * @param nameColumnWord
     * @param thisIsVerse
     * @param language
     * @return
     */
    public static List<SegmentOfPortion> buildSegmentMeterPerfomanceWithAllOptions(DynamicTableRythm dt, String nameColumnSegmentIdentifier, String nameColumnWord, boolean thisIsVerse, String language) {
        List<SegmentOfPortion> listSegments = new ArrayList<>();
        List<Future<Map>> listFuture = new ArrayList<>();

        //list with number of segments
        Integer min = dt.getMinimumValue(nameColumnSegmentIdentifier);
        Integer max = dt.getMaximumValue(nameColumnSegmentIdentifier);
        if ((min < 0) || (max < 0)) {
            throw new IllegalArgumentException("Impossible to define min and max values in segment identifier. May be, incorrect names of columns");
        }

        ExecutorService exService = Executors.newCachedThreadPool();

        for (int i = min; i <= max; i++) {

            List<Word> words = dt.getValue(nameColumnWord, nameColumnSegmentIdentifier, i);
            SegmentOfPortion s = SegmentOfPortion.buildSegmentMeterRepresentationWithAllOptions(words, language);
            s.setSegmentIdentifier(i);
            listSegments.add(s);

            //every segment we send to define meters
            if (thisIsVerse) {
                Future<Map> f = (Future<Map>) exService.submit(() -> s.getMeterDefinitions(language));
                listFuture.add(f);
            }
        }

        exService.shutdown();

        if (thisIsVerse) {
            for (int i = 0; i < listFuture.size(); i++) {
                try {
                    listFuture.get(i).get();
                } catch (InterruptedException | ExecutionException ex) {
                    log.error("Impossible to receive meter. i = " + i + ". " + dt.getValue(nameColumnWord, nameColumnSegmentIdentifier, i) + ex.getMessage());
                }
            }
        }

        return listSegments;
    }


    //== instance-methods ======================================================================================

    /**
     * @return array with average stress per syllable
     */
    public double[] getStressProfile() {
        List<SegmentOfPortion> listSegments = getListOfSegments();
        int maxLength = 0;
        int lineLength = 0;
        String line = "";
        for (int i = 0; i < listSegments.size(); i++) {
            lineLength = listSegments.get(i).getChoosedMeterRepresentation().length();
            if (lineLength > maxLength) {
                maxLength = lineLength;
            }
        }
        int[] numberOfStress = new int[maxLength];
        int[] numberOfLines = new int[maxLength];
        for (int i = 0; i < listSegments.size(); i++) {
            line = listSegments.get(i).getChoosedMeterRepresentation();
            for (int j = 0; j < line.length(); j++) {
                numberOfLines[j] = numberOfLines[j] + 1;
                if (line.charAt(j) == symbolOfStress) {
                    numberOfStress[j] = numberOfStress[j] + 1;
                }
            }
        }

        double[] stressProfile = new double[maxLength];
        for (int i = 0; i < maxLength; i++) {
            stressProfile[i] = 100 * numberOfStress[i] / numberOfLines[i];
        }

        return stressProfile;
    }

    //== abstract methods ===================================================================

    /**
     * @param <R>
     * @return parsed portion of text (why with parameter?)
     */
    public abstract <R> R parsePortionOfText();

    /**
     * reset instance by singlton initiating
     *
     * @param pText
     */
    public abstract void reset(String pText);

    /**
     * organize output
     *
     * @param nPortion
     * @param pathToFileOutput
     */
    public abstract void resumeOutput(int nPortion, StringBuilder outputAccumulation, String pathToFileOutput);

}


