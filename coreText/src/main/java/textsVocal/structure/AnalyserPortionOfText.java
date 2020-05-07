package textsVocal.structure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import textsVocal.config.CommonConstants;
import textsVocal.ru.VocalAnalisysRu;
import textsVocal.utilsCommon.DataTable;
import textsVocal.utilsCore.ConsoleDialog;
import textsVocal.utilsCore.OutputWebCharacteristics;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static textsVocal.structure.TextPortionForRythm.*;

public class AnalyserPortionOfText {

    //=== static fields ===
    public static final StringBuilder outputAccumulation = new StringBuilder();//output accumulation
    private static final List<TextPortionForRythm> listOfInstance = new ArrayList<>();//list with all portions: verses or prose
    private static final Set<String> wordsToDefine = new HashSet<>();//list words to define stress schema
    private static final Logger log = LoggerFactory.getLogger(AnalyserPortionOfText.class);//logger

    private static int numberOfSegments;//number of sentences/lines
    private static int maxLengthSegment;//maximal length of segment
    private static double averageLengthOfSegments;//average length of sentences in syllable
    private static String[] distributionSegmentByLength;//how sentetnces are distributed by their length in syllable
    private static String[] stressProfileOfAllPortions;//stress profile for all portions

//=== static methods =========================
    public static List<TextPortionForRythm> getListOfInstance() {
        return listOfInstance;
    }

    public static Set<String> getWordsToDefine() {
        return wordsToDefine;
    }

    public static int getNumberOfSegments() {
        return numberOfSegments;
    }

    public static void setNumberOfSegments(int numberOfSegments) {
        AnalyserPortionOfText.numberOfSegments = numberOfSegments;
    }

    public static double getAverageLengthOfSegments() {
        return averageLengthOfSegments;
    }

    public static void setAverageLengthOfSegments(double averageLengthOfSegments) {
        AnalyserPortionOfText.averageLengthOfSegments = averageLengthOfSegments;
    }

    public static String[] getStressProfileOfAllPortions() {
        return stressProfileOfAllPortions;
    }

    public static int getMaxLengthSegment() {
        return maxLengthSegment;
    }

    public static void setMaxLengthSegment(int maxLengthSegment) {
        AnalyserPortionOfText.maxLengthSegment = maxLengthSegment;
    }

    public static String[] getDistributionSegmentByLength() {
        return distributionSegmentByLength;
    }

    public static void setDistributionSegmentByLength(String[] distributionSegmentByLength) {
        AnalyserPortionOfText.distributionSegmentByLength = distributionSegmentByLength;
    }

    /**
     * define and set stress profile for whole tetx (all portions)
     */
    public static void setStressProfileOfAllPortions() {
        int maxLength = 0;
        int lineLength;
        String line;
        List<String> meterRepresentationOfPortion = new ArrayList<>();
        for (TextPortionForRythm textPortionForRythm : listOfInstance) {
            List<SegmentOfPortion> listSegments = textPortionForRythm.getListOfSegments();
            for (SegmentOfPortion listSegment : listSegments) {
                line = listSegment.getSelectedMeterRepresentation();
                lineLength = line.length();
                if (lineLength > maxLength) {
                    maxLength = lineLength;
                }
                meterRepresentationOfPortion.add(line);
            }
        }

        int[] numberOfStress = new int[maxLength];
        int[] numberOfLines = new int[maxLength];
        for (String s : meterRepresentationOfPortion) {
            line = s;
            for (int j = 0; j < line.length(); j++) {
                numberOfLines[j] = numberOfLines[j] + 1;
                if (line.charAt(j) == symbolOfStress) {
                    numberOfStress[j] = numberOfStress[j] + 1;
                }
            }
        }

        String[] stressProfile = new String[maxLength];
        for (int i = 0; i < maxLength; i++) {
            stressProfile[i] = "" + (double) (1000 * numberOfStress[i] / numberOfLines[i]) / 10;
        }
        stressProfileOfAllPortions = stressProfile;
        setMaxLengthSegment(maxLength);

    }

    /**
     * refine verse characteristics
     *
     * @param verseInstance if something was changed in instance verseInstance we have to refine all characteristics
     */
    public static void refineVerseCharacteristics(VersePortionForRythm verseInstance) {

        //we need to prioritize possible meters in segment and then fill, edit segments and so on
        verseInstance.fillPortionWithCommonRythmCharacteristics(verseInstance.WhatAreMostPolularMetersInMultiplePossibleValues());

        //now consider endings, duration. number of stress and elaborate meter of verse
        verseInstance.setRegularNumberOfStressOfFirstStrophe("Not regular");
        verseInstance.setRegularDurationOfFirstStrophe("Not regular");
        verseInstance.setRegularEndingsOfFirstStrophe("Not regular");
        List<SegmentOfPortion> listSegment = verseInstance.getListOfSegments();

        if (listSegment.size() >= 3) {
            verseInstance.IsThereRegularEndings();
            verseInstance.IsThereRegularDuration();
            verseInstance.IsThereRegularNumberOfStress();
            verseInstance.IsThereRegularCaesura();
        }
        verseInstance.setMaxAndMinDuration();

        if (verseInstance.getMainMeter().contains("Unknown") || verseInstance.getMainMeter().contains("Mixed")) {
            if (verseInstance.getMainMeter().contains("Unknown")) {
                verseInstance.setMainMeter("Free verse");
            }
            //we have ground to consider main meter
            if (!"Not regular".equals(verseInstance.getRegularEndingsOfFirstStrophe())
                    || !"Not regular".equals(verseInstance.getRegularDurationOfFirstStrophe())
                    || !"Not regular".equals(verseInstance.getRegularNumberOfStressOfFirstStrophe())) {
                verseInstance.finalDefinitionOfPortionsMeter();
            }
        }
    }


    /**
     * prepare Set of words to define their stress schema
     *
     * @param numberOfPortion number of portion (start from 1!)
     */
    public static void prepareSetOfWordsForFurtherDefineMeterSchema(int numberOfPortion) {

        TextPortionForRythm instance = getListOfInstance().get(numberOfPortion - 1);
        String text = instance.getpText();

        ApplicationContext context = CommonConstants.getApplicationContext();
        CommonConstants constants = context.getBean(CommonConstants.class);
        String language = constants.getLanguageOfText();
        if (!language.equals("ru")) {
            log.error(language + "! Unknown language. Impossible to split text.");
            throw new IllegalArgumentException(language + "! Unknown language! Impossible to split text.");
        }

        List<CharSequence[]> allChars = new ArrayList<>();
        allChars.add(SYMB_PARAGRAPH);
        allChars.add(SYMB_PUNCTUATION);
        String pText = cleanTextFromSymbols(text, allChars);
        List<String> strWords = StringToWords(pText, SYMB_SPACE);

        for (String s : strWords) {
            String textWord = s.trim();
            if (!textWord.trim().isEmpty()) {
                if (language.equals("ru")) {
                    int duration = (int) VocalAnalisysRu.calculateDurationOnlyVocale(textWord);
                    String reprParticularCasesOfWords = VocalAnalisysRu.particularCasesOfWords(textWord);

                    if (duration == 0) {
                        continue;
                    } else if (!reprParticularCasesOfWords.isEmpty()) {
                        continue;
                    } else if (duration == 1) {
                        continue;
                    }
                    wordsToDefine.add(textWord.toLowerCase());
                }
            }
        }
    }


    /**
     * prepare Sets with Unknown words and Temporary Word Dictionary
     */
    public static void prepareUnknownAndKnownWords() {

        ApplicationContext context = CommonConstants.getApplicationContext();
        CommonConstants constants = context.getBean(CommonConstants.class);
        String language = constants.getLanguageOfText();
        if (!language.equals("ru")) {
            log.error(language + "! Unknown language. Impossible to split text.");
            throw new IllegalArgumentException(language + "! Unknown language! Impossible to recognize stress schema.");
        }
        if (language.equals("ru")) {
            Map<String, Set<String>> stressMap = VocalAnalisysRu.getStressSchemaForWordsList(wordsToDefine.stream().distinct().collect(Collectors.toList()));
            for (String s : wordsToDefine) {
                if (stressMap.containsKey(s)) {
                    CommonConstants.getTempWordDictionary().put(s.trim(), stressMap.get(s.trim()));
                    continue;
                }
                Word w = new Word();
                w.setTextWord(s.trim());
                CommonConstants.getUnKnownWords().add(w);
            }
        }
        getWordsToDefine().clear();
    }


    /**
     * getting stress schema from user (in console variant)
     */
    public static void whatIsStressSchemaOfUnknownWordsConsole() {
        ApplicationContext context = CommonConstants.getApplicationContext();
        ConsoleDialog consoleDialog = context.getBean(ConsoleDialog.class);
        for (Word w : CommonConstants.getUnKnownWords()) {
            String textWord = w.getTextWord().trim();
            int duration = (int) VocalAnalisysRu.calculateDurationOnlyVocale(textWord);
            String schema = consoleDialog.giveMePleaseStressSchema(textWord, duration);
            if (!schema.equals("N")) {
                w.setMeterRepresentationForUser(schema);
                w.addMeterRepresentation(schema);
                w.setDuration((int) VocalAnalisysRu.calculateDurationOnlyVocale(textWord));
                w.setNumSyllable((int) VocalAnalisysRu.calculateDurationOnlyVocale(textWord));

                //adding to temporary dictionary:
                Set<String> serviceSet = new HashSet<>();
                serviceSet.add(schema);
                CommonConstants.getTempWordDictionary().put(textWord, serviceSet);
            }
        }
    }

    /**
     * process portion analysis
     *
     * @param constants - commno constants of the app
     */
    public static void portionAnalysys(CommonConstants constants) {

        boolean thisIsVerse = constants.isThisIsVerse();
        log.info("Number or portions {}", getListOfInstance().size());

        LocalDateTime localDateTime = LocalDateTime.now();
        outputAccumulation.append("Begin: "+localDateTime).append(" ------------!\n");

        //todo executor service
        for (TextPortionForRythm instance : getListOfInstance()) {
            //creating tables with words, segments and so on

            outputAccumulation.append("!----------PORTION N").append(instance.getNumberOfPortion()).append(" ------------!\n");
            log.debug("!----------PORTION N" + instance.getNumberOfPortion() + " ------------!");

            instance.parsePortionOfText();
            instance.fillWordsObjectsWithStresses();
            DataTable dtOfTextSegmentsAndStresses = instance.getDtOfTextSegmentsAndStresses();

            //name of the first column: they are different in verse and prose
            String nameOfFirstColumn = dtOfTextSegmentsAndStresses.getNamesOfColumn().toArray()[1].toString();
            //every segment has to have table with metric characteristics
            instance.setListOfSegments(buildSegmentMeterPerfomanceWithAllOptions(dtOfTextSegmentsAndStresses, nameOfFirstColumn, "Word-object / Stress form", thisIsVerse, constants.getLanguageOfText()));

            if (thisIsVerse) {
                refineVerseCharacteristics((VersePortionForRythm) instance);
            } else {//prose
                ProsePortionForRythm proseInstance = (ProsePortionForRythm) instance;
                proseInstance.fillPortionWithCommonRythmCharacteristics(null);
            }

            instance.resumeOutput(outputAccumulation, constants);
        }

        calculateSummeryForAllPortions();

        if (!thisIsVerse) {

            if (!constants.isThisIsWebApp()) {//only for console
                ProsePortionForRythm.outputFootProseConsole(outputAccumulation, constants);
            }
        }
    }

    /**
     * set average lentg and distribution by length for all segments in all portions
     */
    public static void setAverageLengthAndDistributionByLengthForAllPortion() {
        //count of sentences
        int countLength = 0;
        int countSentences = 0;
        int[] distributionArray = new int[getMaxLengthSegment()];

        for (int i = 0; i < AnalyserPortionOfText.getListOfInstance().size(); i++) {
            List<SegmentOfPortion> listSegments = AnalyserPortionOfText.getListOfInstance().get(i).getListOfSegments();
            for (SegmentOfPortion listSegment : listSegments) {
                countSentences++;
                countLength += listSegment.getNumberSyllable();
                distributionArray[listSegment.getNumberSyllable() - 1] += 1;
            }
        }
        setNumberOfSegments(countSentences);
        setAverageLengthOfSegments((double) (10 * countLength / countSentences) / 10);

        distributionSegmentByLength = new String[getMaxLengthSegment()];
        for (int i = 0; i < distributionArray.length; i++) {
            distributionSegmentByLength[i] = "" + (double) (1000 * distributionArray[i] / countSentences) / 10;
        }
    }

    /**
     * calculate common characteristics of all portions
     */
    public static void calculateSummeryForAllPortions() {
        setStressProfileOfAllPortions();
        setAverageLengthAndDistributionByLengthForAllPortion();
    }


    /**
     * Verse. Prepare list of lists table with stresses for web-application (for HTML)
     *
     * @return list portions of list with output characteristics for each (verse)
     */
    public static List<List<OutputWebCharacteristics>> prepareListStressesVerseForWeb() {

        List<List<OutputWebCharacteristics>> listTableVerse = new ArrayList<>();

        for (int i = 0; i < listOfInstance.size(); i++) {

            List<OutputWebCharacteristics> tableVerse = new ArrayList<>();
            String id = "" + (i + 1) + ".";

            DataTable dt = listOfInstance.get(i).getDtOfTextSegmentsAndStresses();
            List<SegmentOfPortion> listSegments = listOfInstance.get(i).getListOfSegments();
            String nameOfFirstColumn = (String) dt.getNamesOfColumn().toArray()[1];
            for (int j = 0; j < listSegments.size(); j++) {

                int nSegment = listSegments.get(j).getSegmentIdentifier();
                List<String> words = (List<String>) dt.getValueFromColumnAndRowByCondition("Word", nameOfFirstColumn, nSegment);
                String line = words.stream().map(s -> s + " ").reduce("", String::concat).trim();
                String meterRepresentation = listSegments.get(j).getSelectedMeterRepresentation().trim();
                String meterRepresentationWithSpaces = listSegments.get(j).getMeterRepresentationWithSpaces().trim();
                String meter = listSegments.get(j).getMeter().trim();
                int numberOfTonicFoot = listSegments.get(j).getNumberOfTonicFoot();
                int shiftRegularMeterOnSyllable = listSegments.get(j).getShiftRegularMeterOnSyllable();

                tableVerse.add(new OutputWebCharacteristics(id + (j + 1), i + 1, nSegment,
                        line, meterRepresentation, meter, meterRepresentationWithSpaces,
                        numberOfTonicFoot, shiftRegularMeterOnSyllable, listSegments.get(j).getNumberSyllable(), listSegments.get(j)));
            }
            listTableVerse.add(tableVerse);
        }
        return listTableVerse;
    }

    /**
     * Prose. Prepare list of lists table with stresses for web-application (for HTML)
     *
     * @return list portions of list with output characteristics for each (prose)
     */
    public static List<List<OutputWebCharacteristics>> prepareListStressesTableProseForWeb() {
        //todo
        List<List<OutputWebCharacteristics>> listTableProse = new ArrayList<>();
        for (int i = 0; i < listOfInstance.size(); i++) {

            List<OutputWebCharacteristics> tableVerse = new ArrayList<>();
            String id = "" + (i + 1) + ".";

            DataTable dt = listOfInstance.get(i).getDtOfTextSegmentsAndStresses();
            List<SegmentOfPortion> listSegments = listOfInstance.get(i).getListOfSegments();
            String nameOfFirstColumn = (String) dt.getNamesOfColumn().toArray()[1];
            for (int j = 0; j < listSegments.size(); j++) {

                int nSegment = listSegments.get(j).getSegmentIdentifier();
                List<String> words = (List<String>) dt.getValueFromColumnAndRowByCondition("Word", nameOfFirstColumn, nSegment);
                String line = words.stream().map(s -> s + " ").reduce("", String::concat).trim();
                String meterRepresentation = listSegments.get(j).getSelectedMeterRepresentation().trim();
                String meterRepresentationWithSpaces = listSegments.get(j).getMeterRepresentationWithSpaces().trim();

                tableVerse.add(new OutputWebCharacteristics(id + (j + 1), i + 1, nSegment,
                        line, meterRepresentation, "", meterRepresentationWithSpaces,
                        0, 0, listSegments.get(j).getNumberSyllable(), listSegments.get(j)));
            }
            listTableProse.add(tableVerse);
        }
        return listTableProse;
    }

    /**
     * list juncture profiles for each portion (web-application)
     *
     * @return array with juncture-profile
     */
    public static List<Double[]> prepareListJunctureProfileWeb() {

        List<Double[]> listJunctureProfile = new ArrayList<>();
        Function<SegmentOfPortion, String> funcGetMeter = (SegmentOfPortion::getSelectedMeterRepresentation);
        for (TextPortionForRythm textPortionForRythm : listOfInstance) {
            List<SegmentOfPortion> listSegments = textPortionForRythm.getListOfSegments();
            listJunctureProfile.add((TextPortionForRythm.getJunctureProfileFromSegments(listSegments, funcGetMeter)));
        }
        return listJunctureProfile;
    }


    /**
     * list of arrays with stress profiles for web application
     *
     * @return list of arrays with stress profiles for web application
     */
    public static List<Double[]> prepareListStressesProfileWeb() {
        List<Double[]> listStressesProfile;
        Function<SegmentOfPortion, String> funcGetMeter = (SegmentOfPortion::getSelectedMeterRepresentation);
        listStressesProfile = listOfInstance.stream().map(TextPortionForRythm::getListOfSegments).map(listSegments -> (getStressProfileFromSegments(listSegments, funcGetMeter))).collect(Collectors.toList());
        return listStressesProfile;
    }
}

