package textsVocal.structure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import textsVocal.utils.DynamicTableRythm;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static textsVocal.structure.TextForRythm.*;

public class PortionOfTextAnalyser {

    //=== fields =================================
    public static final StringBuilder outputAccumulation = new StringBuilder();//output accumulation
    public static final List<String> meterRepresentationOfPortion = new ArrayList<>();//stress profile of whole portion
    private static final Logger log = LoggerFactory.getLogger(PortionOfTextAnalyser.class);//logger

    //=== static methods =========================
    public static void portionAnalysys(int numberOfPortion, String pText, boolean thisIsVerse, String pathToFileOutput, String language) throws ExecutionException, InterruptedException {

        ApplicationContext context = new ClassPathXmlApplicationContext("beansCoreText.xml");
        TextForRythm instanceOfText = thisIsVerse
                ? context.getBean(VersePortionForRythm.class)
                : context.getBean(ProsePortionForRythm.class);
        instanceOfText.reset(pText);

        //creating tables with words, segments and so on
        DynamicTableRythm dtOfTextSegmentsAndStresses = CreateDynamicTableOfPortionSegmentsAndStresses(instanceOfText, language);

        outputAccumulation.append("!----------PORTION N" + numberOfPortion + " ------------!\n");
        log.info("!----------PORTION N" + numberOfPortion + " ------------!");

        //name of the first column: they are different in verse and prose
        String nameOfFirstColumn = dtOfTextSegmentsAndStresses.getNamesOfColumn().toArray()[1].toString();
        //every segment has to have table with metric characteristics
        instanceOfText.setListOfSegments(buildSegmentMeterPerfomanceWithAllOptions(dtOfTextSegmentsAndStresses, nameOfFirstColumn, "Word-object / Stress form", thisIsVerse, language));

        if (thisIsVerse) {
            VersePortionForRythm verseInstance = (VersePortionForRythm)instanceOfText;
            //we need to prioritize possible meters in segment and then fill, edit segments and so on
            verseInstance.fillPortionWithCommonRythmCharacteristics(verseInstance.WhatAreMostPolularMetersInMultiplePossibleValues());

            //now consider endings, duration. number of stress and elaborate meter of verse
            verseInstance.setRegularNumberOfStressOfFirstStrophe("Not regular");
            verseInstance.setRegularDurationOfFirstStrophe("Not regular");
            verseInstance.setRegularEndingsOfFirstStrophe("Not regular");
            List<SegmentOfPortion> listSegment = instanceOfText.getListOfSegments();

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

        } else {//prose
            ProsePortionForRythm proseInstance = (ProsePortionForRythm)instanceOfText;
            proseInstance.fillPortionWithCommonRythmCharacteristics(null);
        }

        instanceOfText.resumeOutput(numberOfPortion, outputAccumulation, pathToFileOutput);
    }

    /**
     * @return array with average stress per syllable from segments
     */
    public static double[] getStressProfileFromWholeText() {
        int maxLength = 0;
        int lineLength = 0;
        String line = "";
        for (int i = 0; i < meterRepresentationOfPortion.size(); i++) {
            line = meterRepresentationOfPortion.get(i);
            lineLength = line.length();
            if (lineLength > maxLength) {
                maxLength = lineLength;
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

        double[] stressProfile = new double[maxLength];
        for (int i = 0; i < maxLength; i++) {
            stressProfile[i] = 100 * numberOfStress[i] / numberOfLines[i];
        }

        return stressProfile;
    }

    /**
     * cleaning list with meter representations
     */
    public static void ClearListMeterRepresentation(){
        meterRepresentationOfPortion.clear();
    }
}

