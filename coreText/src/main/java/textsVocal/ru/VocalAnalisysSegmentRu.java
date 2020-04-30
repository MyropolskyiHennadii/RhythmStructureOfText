package textsVocal.ru;

import textsVocal.structure.Alphabetable;
import textsVocal.structure.SegmentOfPortion;
import textsVocal.structure.VersePortionForRythm;
import textsVocal.structure.Word;
import textsVocal.utilsCommon.DynamicTableRythm;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static textsVocal.structure.TextForRythm.symbolOfNoStress;
import static textsVocal.structure.TextForRythm.symbolOfStress;

public class VocalAnalisysSegmentRu implements Alphabetable {

    //=== fields ============================================
    private final SegmentOfPortion segment;

    //=== constructor =======================================
    public VocalAnalisysSegmentRu(SegmentOfPortion segment) {
        this.segment = segment;
    }

    //=== getter ============================================
    public SegmentOfPortion getSegment() {
        return segment;
    }

    //=== overriden methods =================================
    @Override
    public Number calculateDuration(String segment) {
        return segment.length();
    }

    @Override
    /**
     * receive meter definitions from mysql
     */
    public Map getRythmSchemaOfTheText() {

        String var;
        Set<String> setMeters = new HashSet<>();//set of represantations
        Map<Integer, Set<String>> mapMeters = new HashMap<>();

        DynamicTableRythm dtSegment = segment.getTableOfMeterDefinitions();

        List<String> meterRepresentations = segment.getMeterRepresentations();

        for (String meterRepresentation : meterRepresentations) {

            var = meterRepresentation.trim();

            Map<String, String> descriptionMeter = VersePortionForRythm.WhatIsTheMettersPatternForStringWithoutPentons(var);

            if (!"Unknown".equals(descriptionMeter.get("meter").trim()) || var.length() <= 10) {//without pentons
                String repr = var + ";" + descriptionMeter.get("meter").trim() + "-" + descriptionMeter.get("nTonicFoot").trim() + ";" + descriptionMeter.get("nShiftRegularMeterOnSyllable").trim();

                if (!setMeters.contains(repr)) {//without duplicates
                    dtSegment.setRow(Stream.of(var, descriptionMeter.get("meter").trim(), Integer.valueOf(descriptionMeter.get("nTonicFoot").trim()), Integer.valueOf(descriptionMeter.get("nShiftRegularMeterOnSyllable").trim())).collect(Collectors.toList()));
                    setMeters.add(repr);
                }
            } else {//pentons
                descriptionMeter = VersePortionForRythm.WhatIsTheMettersPatternForStringPentons(var);
                String repr = var + ";" + descriptionMeter.get("meter").trim() + "-" + descriptionMeter.get("nTonicFoot").trim() + ";" + descriptionMeter.get("nShiftRegularMeterOnSyllable").trim();

                if (!setMeters.contains(repr)) {//without duplicates
                    dtSegment.setRow(Stream.of(var, descriptionMeter.get("meter").trim(), Integer.valueOf(descriptionMeter.get("nTonicFoot").trim()), Integer.valueOf(descriptionMeter.get("nShiftRegularMeterOnSyllable").trim())).collect(Collectors.toList()));
                    setMeters.add(repr);
                }
            }
        }

        //if there is no row in dtSegment, that means: no word was recognized
        //Probably it was the line with foreign language
        if (dtSegment.getSize() == 0) {
            dtSegment.setRow(Stream.of("0", "Unknown. Probably unknown language", 0, 0).collect(Collectors.toList()));
            segment.setMeter("" + symbolOfNoStress);
            segment.setSelectedMeterRepresentation("" + symbolOfNoStress);
        }

        mapMeters.put(segment.getSegmentIdentifier(), setMeters);
        return mapMeters;
    }

    //=== public static methods ==============================================

    /**
     * check in Russian: whether current representation have sense
     *
     * @param meterRepresentation
     * @param listWords
     * @return
     */
    public static boolean checkSensibleRepresentationOfMeter(String meterRepresentation, List<Word> listWords, boolean thisIsVerse) {

        int size = listWords.size();

        boolean allOK = true;
        int numSyllableSum = 0;
        int numSyllable = 0;

        for (int i = 0; i < size; i++) {
            numSyllable = listWords.get(i).getNumSyllable();
            numSyllableSum += numSyllable;

            if (numSyllable == 1 && numSyllableSum < meterRepresentation.length() - 1) {
                if (thisIsVerse) {
                    //one-syllable-word with stress and next word beginns with stress
                    if (meterRepresentation.charAt(numSyllableSum - 1) == symbolOfStress
                            && meterRepresentation.charAt(numSyllableSum) == symbolOfStress) {
                        return false;
                    }
                    //one-syllable-word with stress and previous word ends with stress
                    if (numSyllableSum >= 2) {
                        if (meterRepresentation.charAt(numSyllableSum - 1) == symbolOfStress
                                && meterRepresentation.charAt(numSyllableSum - 2) == symbolOfStress) {
                            return false;
                        }
                    }
                }
            }
        }

        return allOK;
    }
}

