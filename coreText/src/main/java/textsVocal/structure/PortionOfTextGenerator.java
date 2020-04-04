package textsVocal.structure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import textsVocal.utils.DynamicTableRythm;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static textsVocal.structure.TextForRythm.CreateDynamicTableOfPortionSegmentsAndStresses;
import static textsVocal.structure.TextForRythm.buildSegmentMeterPerfomanceWithAllOptions;

public class PortionOfTextGenerator {

    //=== fields =================================
    public static final StringBuilder outputAccumulation = new StringBuilder();//output accumulation
    private static final Logger log = LoggerFactory.getLogger(PortionOfTextGenerator.class);//logger

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
        log.debug("!----------PORTION N" + numberOfPortion + " ------------!");

        //every segment has to have table with metric characteristics
        instanceOfText.setListOfSegments(buildSegmentMeterPerfomanceWithAllOptions(dtOfTextSegmentsAndStresses, "Number of line", "Word-object / Stress form", thisIsVerse, language));

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

        instanceOfText.resumeOutput(numberOfPortion, outputAccumulation, pathToFileOutput);
    }

}

