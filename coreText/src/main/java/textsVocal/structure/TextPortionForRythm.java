package textsVocal.structure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import textsVocal.config.CommonConstants;
import textsVocal.ru.VocalAnalisysRu;
import textsVocal.utilsCommon.DataTable;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

/**
 * abstract class for portions instances: prose, verse and what we can else
 */
public abstract class TextPortionForRythm {

    //== fields ==================================================================
    public static final char symbolOfStress = '1';
    public static final char symbolOfNoStress = '0';
    public static final String[] stressRepresentations = {"" + symbolOfNoStress, "" + symbolOfStress};
    public static final CharSequence[] SYMB_PARAGRAPH = {"" + (char) 10, "" + (char) 12, "" + (char) 13};// unicodes return
    public static final CharSequence[] SYMB_SPACE = {"" + (char) 32, "" + (char) 160, "" + (char) 9};// unicodes spaces
    // strings with different marks end the sentence
    public static final CharSequence[] SYMB_SENTENCE = {"!!!", "???", "...", "??!", "??", "?!", "!!", "!", "?",
            ".", ";"};
    // punctuation
    public static final CharSequence[] SYMB_PUNCTUATION = {"!", "?", ".", ",", ".", ";",
            ":", "'", "-", "+", "(", ")", "[", "]", "#", "@", "{", "}", "$", "%", "&", "^", "/", "|",
            "*", "" + (char) 92, "" + (char) 34, "_", "<", ">", "" + (char) 8212, "" + (char) 65279,
            "" + (char) -1};
    //brief punctuation
    public static final CharSequence[] SYMB_BRIEF_PUNCTUATION = {",", ".", ";", ":", "!", "?", "-", "_", "" + (char) 8212, "" + (char) -1, "" + (char) 65279};
    //symbols of quote
    public static final CharSequence[] SYMB_QUOTE = {"" + (char) 34};

    private static Logger log = LoggerFactory.getLogger(VersePortionForRythm.class);//logger

    private final String pText = "";//original text
    private final String originalText = "";

    private DataTable dtOfTextSegmentsAndStresses;// table with all segmentations
    private List<SegmentOfPortion> listOfSegments;//list of segments
    private int numberOfPortion;//number of portion

    public static char getSymbolOfStress() {
        return symbolOfStress;
    }

    public static Logger getLog() {
        return log;
    }


//== static methods =========================================================
    /**
     * as split(), but split doesn't work (I don't know why) with symbols like "/\" and so on
     *
     * @param pTextForConsideration what is the StringBuilder input for consideration
     * @param pSeparators array with separators
     * @return string (text) after preparing
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
     * @param _Text original text
     * @param pSeparators array with lines separators
     * @param pSpace array with spaces
     * @return result as StringBuilder
     */
    public static StringBuilder prepareStringForParsing(String _Text, CharSequence[] pSeparators, CharSequence[] pSpace) {

        _Text = _Text.trim();
        StringBuilder pText = new StringBuilder(_Text.length());

        // replaceAll() with "???" doesn't work, that's why:
        boolean wasChanging;
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

        //if first symbol in reverse order is symbol of quote => trow last quote
        for (CharSequence delimiter : SYMB_QUOTE) {
            String lastSymbol = "" + pText.charAt(0);
            if (lastSymbol.equals("" + delimiter)) {
                pText = new StringBuilder(pText.substring(1, pText.length()));
            }
        }

        int finishedLength;
        int beginningLength;
        String fragment;
        int posRedundantSpace;

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
            finishedLength = pText.length();
        } while (finishedLength != beginningLength);

        pText.reverse();
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
            finishedLength = pText.length();
        } while (finishedLength != beginningLength);

        return pText;
    }

    /**
     * punctuation - away
     *
     * @param word object represents word (object Word or simple String)
     * @param pSeparators array with punctuation
     * @param <T> type of the word
     * @return result string without punctuation
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

    /**
     *
     * @param pText original text or object
     * @param pSeparators array with symbols which will from text - away
     * @param <T> type
     * @return resul string
     */
    public static <T> String cleanTextFromSymbols(T pText, List<CharSequence[]> pSeparators) {
        String Text = pText.toString().trim();

        List<CharSequence> allChars = new ArrayList<>();
        for (CharSequence[] ch : pSeparators) {
            allChars.addAll(Arrays.asList(ch));
        }

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
     * works as split(), but not split() because spilt doesn't work with "???" and so on (I don't know why)
     *
     * @param pText origibal string
     * @param pSeparators array with separators
     * @return list of words (not objects Word)
     */
    public static List<String> StringToWords(String pText, CharSequence[] pSeparators) {
        List<String> listOfWords = new ArrayList<>();
        String word;

        int begIndex = 0;
        for (int i = 0; i < pText.length(); i++) {
            char s = pText.charAt(i);
            for (CharSequence delimiter : pSeparators) {
                if ((delimiter.length() == 1) && (delimiter.charAt(0) == s)) {
                    word = pText.substring(begIndex, i).trim();
                    if (!word.isEmpty()) {
                        listOfWords.add(word);
                    }
                    begIndex = i + 1;
                } else if ((delimiter.length() + i) < pText.length()) {
                    if (pText.subSequence(i, i + delimiter.length()).toString()
                            .equalsIgnoreCase(delimiter.toString())) {
                        word = pText.substring(begIndex, i).trim();
                        if (!word.isEmpty()) {
                            listOfWords.add(word);
                        }
                        begIndex = i + delimiter.length() + 1;
                    }
                }
            }
        }
        //last word:
        listOfWords.add(pText.substring(begIndex).trim());
        return listOfWords;
    }

    /**
     * add unknown word(object) to set
     * @param objWord word object
     * @param textWord word as string
     * @param duration duration of word (in syllable)
     */
    private static void addUnkownWordToSet(Word objWord, String textWord, int duration) {
        objWord.setMeterRepresentationForUser(textWord);
        StringBuilder pMeterRepresentation = new StringBuilder();
        //all cases
        for (int j = 0; j < duration; j++) {
            pMeterRepresentation.append(symbolOfNoStress);
        }
        for (int j = 0; j < duration; j++) {
            String pCase = pMeterRepresentation.substring(0, j) + symbolOfStress + pMeterRepresentation.substring(j + 1, duration);
            objWord.addMeterRepresentation(pCase);
        }

        pMeterRepresentation = new StringBuilder();
        //all cases
        for (int j = 0; j < duration; j++) {
            pMeterRepresentation.append("?");
        }
        objWord.setMeterRepresentationForUser(pMeterRepresentation.toString());
        CommonConstants.getUnKnownWords().add(objWord);
    }

    /**
     *  builds all possible - for each segment - stress schema 01010101...001100 because there are few possible for each
     *
     * @param dt data table of portion
     * @param nameColumnSegmentIdentifier how called column with segment identifier
     * @param nameColumnWord how called column with Word
     * @param thisIsVerse true = poem, false = prose
     * @param language language of the whole text
     * @return list of segments with rebuilt stress schema
     */
    public static List<SegmentOfPortion> buildSegmentMeterPerfomanceWithAllOptions(DataTable dt, String nameColumnSegmentIdentifier, String nameColumnWord, boolean thisIsVerse, String language) {
        List<SegmentOfPortion> listSegments = new ArrayList<>();
        List<Future<Map<Integer, Set<String>>>> listFuture = new ArrayList<>();

        //list with number of segments
        Integer min = dt.getMinimumValue(nameColumnSegmentIdentifier);
        Integer max = dt.getMaximumValue(nameColumnSegmentIdentifier);
        if ((min < 0) || (max < 0)) {
            log.error("Impossible to define min and max values in segment identifier. May be, incorrect names of columns " + nameColumnSegmentIdentifier);
            throw new IllegalArgumentException("Impossible to define min and max values in segment identifier. May be, incorrect names of columns " + nameColumnSegmentIdentifier);
        }

        ExecutorService exService = Executors.newCachedThreadPool();

        for (int i = min; i <= max; i++) {

            List<Word> words = dt.getValueFromColumnAndRowByCondition(nameColumnWord, nameColumnSegmentIdentifier, i);
            SegmentOfPortion s = SegmentOfPortion.buildSegmentMeterRepresentationWithAllOptions(words,
                    language,
                    thisIsVerse);
            s.setSegmentIdentifier(i);
            listSegments.add(s);

            //every segment we send to define meters
            if (thisIsVerse) {
                Future<Map<Integer, Set<String>>> f = exService.submit(() -> s.getMeterDefinitions(language));
                listFuture.add(f);
            }
        }

        exService.shutdown();

        if (thisIsVerse) {
            for (int i = 0; i < listFuture.size(); i++) {
                try {
                    listFuture.get(i).get();
                } catch (InterruptedException | ExecutionException ex) {
                    log.error("Impossible to receive meter. i = " + i + ". " + dt.getValueFromColumnAndRowByCondition(nameColumnWord, nameColumnSegmentIdentifier, i) + ex.getMessage());
                }
            }
        }

        return listSegments;
    }

    /**
     * @return array with average stress per syllable from all segments
     */
    public static Double[] getStressProfileFromSegments(List<SegmentOfPortion> listSegments, Function<SegmentOfPortion, String> func) {
        int maxLength = 0;
        int lineLength;
        String line;
        for (SegmentOfPortion segment : listSegments) {
            line = func.apply(segment);
            lineLength = line.length();
            if (lineLength > maxLength) {
                maxLength = lineLength;
            }
        }
        int[] numberOfStress = new int[maxLength];
        int[] numberOfLines = new int[maxLength];
        for (SegmentOfPortion listSegment : listSegments) {
            line = func.apply(listSegment);
            for (int j = 0; j < line.length(); j++) {
                numberOfLines[j] = numberOfLines[j] + 1;
                if (line.charAt(j) == symbolOfStress) {
                    numberOfStress[j] = numberOfStress[j] + 1;
                }
            }
        }

        Double[] stressProfile = new Double[maxLength];
        for (int i = 0; i < maxLength; i++) {
            stressProfile[i] = (double) (1000 * numberOfStress[i] / numberOfLines[i]) / 10;
        }

        return stressProfile;
    }

    /**
     * @return static: array with average juncture between words per syllable from segments
     */
    public static Double[] getJunctureProfileFromSegments(List<SegmentOfPortion> listSegments, Function<SegmentOfPortion, String> func) {
        int maxLength = 0;
        int lineLength;
        String line;
        for (SegmentOfPortion listSegment : listSegments) {
            line = func.apply(listSegment);
            lineLength = line.length();
            if (lineLength > maxLength) {
                maxLength = lineLength;
            }
        }
        int[] numberOfJunctures = new int[maxLength];
        int[] numberOfLines = new int[maxLength];
        for (SegmentOfPortion listSegment : listSegments) {
            List<Integer> listJuncture = listSegment.getSchemaOfSpaces();
            line = func.apply(listSegment);
            for (int j = 0; j < line.length(); j++) {
                numberOfLines[j] = numberOfLines[j] + 1;
                if (listJuncture.contains(j + 1)) {
                    numberOfJunctures[j] = numberOfJunctures[j] + 1;
                }
            }
        }

        Double[] junctureProfile = new Double[maxLength];
        for (int i = 0; i < maxLength; i++) {
            junctureProfile[i] = (double) (1000 * numberOfJunctures[i] / numberOfLines[i]) / 10;
        }

        return junctureProfile;
    }

//==getters and setters ==
    public String getOriginalText() {
        return originalText;
    }

    public String getpText() {
        return pText;
    }

    public List<SegmentOfPortion> getListOfSegments() {
        return listOfSegments;
    }

    public void setListOfSegments(List<SegmentOfPortion> listOfSegments) {
        this.listOfSegments = listOfSegments;
    }

    public DataTable getDtOfTextSegmentsAndStresses() {
        return dtOfTextSegmentsAndStresses;
    }

    public void setDtOfTextSegmentsAndStresses(DataTable dtOfTextSegmentsAndStresses) {
        this.dtOfTextSegmentsAndStresses = dtOfTextSegmentsAndStresses;
    }

    public int getNumberOfPortion() {
        return numberOfPortion;
    }

    public void setNumberOfPortion(int numberOfPortion) {
        this.numberOfPortion = numberOfPortion;
    }

    /**
     * prepared object Words: filling with stresses
     */
    public void fillWordsObjectsWithStresses() {

        DataTable dt = getDtOfTextSegmentsAndStresses();
        Map<String, Set<String>> mapStress = CommonConstants.getTempWordDictionary();

        List<String> words = dt.getColumnFromTable("Word");
        List<Word> objWords = dt.getColumnFromTable("Word-object / Stress form");
        int duration;

        for (int i = 0; i < words.size(); i++) {
            String textWord = cleanWordFromPunctuation(words.get(i).toLowerCase().trim(), SYMB_PUNCTUATION);//database have the same change ё on е

            Word objWord = objWords.get(i);
            objWord.setTextWord(textWord);
            duration = (int) VocalAnalisysRu.calculateDurationOnlyVocale(textWord);
            objWord.setDuration(duration);
            objWord.setNumSyllable(duration);
            String reprParticularCasesOfWords = VocalAnalisysRu.particularCasesOfWords(textWord);

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

                } else {
                    addUnkownWordToSet(objWord, textWord, duration);
                }
            }
        }
    }

    /**
     * @return array with average stress per syllable from segments
     */
    public double[] getStressProfileFromSegments(Function<SegmentOfPortion, String> func) {
        List<SegmentOfPortion> listSegments = getListOfSegments();
        int maxLength = 0;
        int lineLength;
        String line;
        for (SegmentOfPortion segment : listSegments) {
            line = func.apply(segment);
            lineLength = line.length();
            if (lineLength > maxLength) {
                maxLength = lineLength;
            }
        }
        int[] numberOfStress = new int[maxLength];
        int[] numberOfLines = new int[maxLength];
        for (SegmentOfPortion listSegment : listSegments) {
            line = func.apply(listSegment);
            for (int j = 0; j < line.length(); j++) {
                numberOfLines[j] = numberOfLines[j] + 1;
                if (line.charAt(j) == symbolOfStress) {
                    numberOfStress[j] = numberOfStress[j] + 1;
                }
            }
        }

        double[] stressProfile = new double[maxLength];
        for (int i = 0; i < maxLength; i++) {
            stressProfile[i] = (double) (1000 * numberOfStress[i] / numberOfLines[i])/10;
        }

        return stressProfile;
    }
    //== instance-methods ======================================================================================

    /**
     * @return array with average juncture between words per syllable from segments
     */
    public double[] getJunctureProfileFromSegments(Function<SegmentOfPortion, String> func) {
        List<SegmentOfPortion> listSegments = getListOfSegments();
        int maxLength = 0;
        int lineLength;
        String line;
        for (SegmentOfPortion listSegment : listSegments) {
            line = func.apply(listSegment);
            lineLength = line.length();
            if (lineLength > maxLength) {
                maxLength = lineLength;
            }
        }
        int[] numberOfJunctures = new int[maxLength];
        int[] numberOfLines = new int[maxLength];
        for (SegmentOfPortion listSegment : listSegments) {
            List<Integer> listJuncture = listSegment.getSchemaOfSpaces();
            line = func.apply(listSegment);
            for (int j = 0; j < line.length(); j++) {
                numberOfLines[j] = numberOfLines[j] + 1;
                if (listJuncture.contains(j + 1)) {
                    numberOfJunctures[j] = numberOfJunctures[j] + 1;
                }
            }
        }

        double[] junctureProfile = new double[maxLength];
        for (int i = 0; i < maxLength; i++) {
            junctureProfile[i] = (double)(1000 * numberOfJunctures[i] / numberOfLines[i])/10;
        }

        return junctureProfile;
    }

    /**
     * @return parsed portion of text to the table with segments, words, stress schema and so on
     */
    public abstract DataTable parsePortionOfText();
    //== abstract methods ===================================================================

    /**
     * fill all segments with rythm charachteristics
     */
    public abstract <T> void fillPortionWithCommonRythmCharacteristics(T t);

    /**
     * reset instance by singlton initiating
     *
     * @param pText original text
     */
    public abstract void reset(String pText);

    /**
     * organize output
     * @param outputAccumulation StringBuilder where output is stored
     * @param commonConstants common app constants
     */
    public abstract void resumeOutput(StringBuilder outputAccumulation, CommonConstants commonConstants);

    //== enums ================================================================
    //must have to define verse meter
    public enum verseMeterPatterns {

        Trochee("10"), Iambus("01"), Dactyl("100"), Amphibrach("010"), Anapaest("001"),
        PentonI("10000"), PentonII("01000"), PentonIII("00100"), PentonIV("00010"), PentonV("00001");

        private final String pattern;

        verseMeterPatterns(String m) {
            this.pattern = m;
        }

        public String getPattern() {
            return pattern;
        }

        public int getDuration() {
            return pattern.length();
        }
    }

}


