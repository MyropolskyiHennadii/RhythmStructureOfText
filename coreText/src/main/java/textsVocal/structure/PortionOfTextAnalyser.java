package textsVocal.structure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import textsVocal.config.CommonConstants;
import textsVocal.utilsCommon.DynamicTableRythm;
import textsVocal.utilsCore.WebVerseCharacteristicsOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import static textsVocal.structure.TextForRythm.*;

public class PortionOfTextAnalyser {

    //=== static fields ===
    public static final StringBuilder outputAccumulation = new StringBuilder();//output accumulation
    //public static final List<String> meterRepresentationOfPortion = new ArrayList<>();//stress profile of whole portion
    private static final List<TextForRythm> listOfInstance = new ArrayList<>();//list with all portions: verses or prose
    private static final Logger log = LoggerFactory.getLogger(PortionOfTextAnalyser.class);//logger

    //=== getter ===//
    public static List<TextForRythm> getListOfInstance() {
        return listOfInstance;
    }


    //=== static methods =========================

    /**
     * refine verse characteristics
     *
     * @param verseInstance
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
            verseInstance.IsThereRegularСaesura();
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
     * process portion analysis
     *
     * @param numberOfPortion
     * @param pText
     * @param constants
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static void portionAnalysys(int numberOfPortion, String pText, CommonConstants constants) throws ExecutionException, InterruptedException {

        boolean thisIsVerse = constants.isThisIsVerse();
        String language = constants.getLanguageOfText();
        String pathToFileOutput = constants.getFileOutputDirectory() + constants.getFileOutputName();
        ApplicationContext context = constants.getApplicationContext();

        TextForRythm instanceOfText = thisIsVerse
                ? context.getBean(VersePortionForRythm.class)
                : context.getBean(ProsePortionForRythm.class);
        instanceOfText.reset(pText);
        instanceOfText.setRequireUnknownWordsByUser(constants.isRequireUnknownWordsByUser());
        listOfInstance.add(instanceOfText);

        //creating tables with words, segments and so on
        DynamicTableRythm dtOfTextSegmentsAndStresses = CreateDynamicTableOfPortionSegmentsAndStresses(instanceOfText, constants);

        outputAccumulation.append("!----------PORTION N" + numberOfPortion + " ------------!\n");
        log.debug("!----------PORTION N" + numberOfPortion + " ------------!");

        //name of the first column: they are different in verse and prose
        String nameOfFirstColumn = dtOfTextSegmentsAndStresses.getNamesOfColumn().toArray()[1].toString();
        //every segment has to have table with metric characteristics
        instanceOfText.setListOfSegments(buildSegmentMeterPerfomanceWithAllOptions(dtOfTextSegmentsAndStresses, nameOfFirstColumn, "Word-object / Stress form", thisIsVerse, language));

        if (thisIsVerse) {
            VersePortionForRythm verseInstance = (VersePortionForRythm) instanceOfText;
            refineVerseCharacteristics(verseInstance);
        } else {//prose
            ProsePortionForRythm proseInstance = (ProsePortionForRythm) instanceOfText;
            proseInstance.fillPortionWithCommonRythmCharacteristics(null);
        }

        instanceOfText.resumeOutput(numberOfPortion, outputAccumulation, constants);
    }

    /**
     * @return array with average stress per syllable from segments
     */
    public static String[] getStressProfileFromWholeText() {

        int maxLength = 0;
        int lineLength = 0;
        String line = "";
        List<String> meterRepresentationOfPortion = new ArrayList<>();
        for (int k = 0; k < listOfInstance.size(); k++) {
            List<SegmentOfPortion> listSegments = listOfInstance.get(k).getListOfSegments();
            for (int m = 0; m < listSegments.size(); m++) {
                line = listSegments.get(m).getSelectedMeterRepresentation();
                lineLength = line.length();
                if (lineLength > maxLength) {
                    maxLength = lineLength;
                }
                meterRepresentationOfPortion.add(line);
            }
        }

        int[] numberOfStress = new int[maxLength];
        int[] numberOfLines = new int[maxLength];
        for (int i = 0; i < meterRepresentationOfPortion.size(); i++) {
            line = meterRepresentationOfPortion.get(i);
            for (int j = 0; j < line.length(); j++) {
                numberOfLines[j] = numberOfLines[j] + 1;
                if (line.charAt(j) == symbolOfStress) {
                    numberOfStress[j] = numberOfStress[j] + 1;
                }
            }
        }

        String[] stressProfile = new String[maxLength];
        for (int i = 0; i < maxLength; i++) {
            stressProfile[i] = "" + (int) 100 * numberOfStress[i] / numberOfLines[i];
            //+ " (" + numberOfLines[i] + ")";
        }

        return stressProfile;
    }


    /**
     * Verse. Prepare list of lists table with stresses for web-application (for HTML)
     *
     * @return
     */
    public static List<List<WebVerseCharacteristicsOutput>> prepareListStressesTableVerseForWeb() {

        List<List<WebVerseCharacteristicsOutput>> listTableVerse = new ArrayList<>();

        for (int i = 0; i < listOfInstance.size(); i++) {

            List<WebVerseCharacteristicsOutput> tableVerse = new ArrayList<>();
            String id = "" + (i + 1) + ".";

            DynamicTableRythm dt = listOfInstance.get(i).getDtOfTextSegmentsAndStresses();
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

                tableVerse.add(new WebVerseCharacteristicsOutput(id + (j + 1), i + 1, nSegment,
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
     * @return
     */
    public static List<List<WebVerseCharacteristicsOutput>> prepareListStressesTableProseForWeb() {
        //todo
        List<List<WebVerseCharacteristicsOutput>> listTableProse = new ArrayList<>();

        return listTableProse;
    }

    /**
     * list joncture profiles for web-application
     *
     * @return
     */
    public static List<Double[]> prepareListJunctureProfileWeb() {

        List<Double[]> listStressesProfile = new ArrayList<>();
        Function<SegmentOfPortion, String> funcGetMeter = (s -> s.getSelectedMeterRepresentation());
        for (int i = 0; i < listOfInstance.size(); i++) {
            //List<SegmentOfPortion> listSegments = listListSegments.get(i);
            List<SegmentOfPortion> listSegments = listOfInstance.get(i).getListOfSegments();
            listStressesProfile.add((TextForRythm.getJunctureProfileFromSegments(listSegments, funcGetMeter)));
        }
        return listStressesProfile;
    }


    /**
     * list stress profiles for web application
     *
     * @return
     */
    public static List<Double[]> prepareListStressesProfileWeb() {

        List<Double[]> listStressesProfile = new ArrayList<>();
        Function<SegmentOfPortion, String> funcGetMeter = (s -> s.getSelectedMeterRepresentation());
        for (int i = 0; i < listOfInstance.size(); i++) {
            //List<SegmentOfPortion> listSegments = listListSegments.get(i);
            List<SegmentOfPortion> listSegments = listOfInstance.get(i).getListOfSegments();
            listStressesProfile.add((TextForRythm.getStressProfileFromSegments(listSegments, funcGetMeter)));
        }
        return listStressesProfile;
    }
}

