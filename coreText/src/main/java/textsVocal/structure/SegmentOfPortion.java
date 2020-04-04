package textsVocal.structure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import textsVocal.structure.ru.VocalAnalisysSegmentRu;
import textsVocal.utils.DynamicTableRythm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static textsVocal.structure.TextForRythm.symbolOfStress;

public class SegmentOfPortion {

    //=== fields ==================================================
    private static final Logger log = LoggerFactory.getLogger(SegmentOfPortion.class);//logger

    private final List<String> meterRepresentations;// full list of representations such as "...0101..."  (probably more than one element)
    private final DynamicTableRythm tableOfMeterDefinitions;// table with definition every meter in meterRepresentations
    private String meterRepresentationForUser;//first meter representation with "?", whether they are
    private Integer segmentIdentifier;// number of segment
    private int numberSyllable;
    private double duration = 0;// in our case duration == namberSyllable, but theoretically may be something other
    private String meter;//classic meter definition, if there is
    private int numberOfTonicFoot;// classic number of foots, if there is
    private int numberСaesuraSyllable;// classic number of syllable with caesura, if there is
    private String choosedMeterRepresentation;//if there is more than one representations, here we define main (if there is)
    private String ending;//ending of segment

    //=== constructor ============================================
    public SegmentOfPortion(List<String> meterRepresentations) {

        this.meterRepresentations = meterRepresentations;

        // names of columns
        List<String> namesOfColumns = new ArrayList<>();
        namesOfColumns.add("Segment representation");
        namesOfColumns.add("Meter");
        namesOfColumns.add("Number tonic foot");
        namesOfColumns.add("Сaesura syllable");

        // supliers for lists with data
        List<Supplier<?>> sup = new ArrayList<>();
        sup.add(ArrayList<String>::new);
        sup.add(ArrayList<String>::new);
        sup.add(ArrayList<Integer>::new);
        sup.add(ArrayList<Integer>::new);

        this.tableOfMeterDefinitions = new DynamicTableRythm(namesOfColumns, sup);

    }

    //=== getters and setters ====================================
    public String getEnding() {
        return ending;
    }

    public void setEnding(String ending) {
        this.ending = ending;
    }

    public int getNumberSyllable() {
        return numberSyllable;
    }

    public void setNumberSyllable(int numberSyllable) {
        this.numberSyllable = numberSyllable;
    }

    public String getChoosedMeterRepresentation() {
        return choosedMeterRepresentation;
    }

    public void setChoosedMeterRepresentation(String choosedMeterRepresentation) {
        this.choosedMeterRepresentation = choosedMeterRepresentation;
    }

    public String getMeter() {
        return meter;
    }

    public void setMeter(String meter) {
        this.meter = meter;
    }

    public int getNumberOfTonicFoot() {
        return numberOfTonicFoot;
    }

    public void setNumberOfTonicFoot(int numberOfTonicFoot) {
        this.numberOfTonicFoot = numberOfTonicFoot;
    }

    public int getNumberСaesuraSyllable() {
        return numberСaesuraSyllable;
    }

    public void setNumberСaesuraSyllable(int numberСaesuraSyllable) {
        this.numberСaesuraSyllable = numberСaesuraSyllable;
    }

    public void setSegmentIdentifier(Integer segmentIdentifier) {
        this.segmentIdentifier = segmentIdentifier;
    }

    public Integer getSegmentIdentifier() {
        return segmentIdentifier;
    }

    public List<String> getMeterRepresentations() {
        return meterRepresentations;
    }

    public void setMeterRepresentationForUser(String meterRepresentationForUser) {
        this.meterRepresentationForUser = meterRepresentationForUser;
    }

    public String getMeterRepresentationForUser() {
        return meterRepresentationForUser;
    }

    public void setDuration(String representation) {
        //TODO: that isn't alwayas a fact...
        this.duration = representation.length();
    }

    public double getDuration() {
        return duration;
    }

    public DynamicTableRythm getTableOfMeterDefinitions() {
        return tableOfMeterDefinitions;
    }

    // === overriden methods ==============================================
    @Override
    public String toString() {
        return segmentIdentifier + ": " + meterRepresentations;
    }


    //=== static methods ==================================================

    /**
     * returns number of stresses in the string
     * @param representation
     * @return
     */
    public static int getNumberOfStress(String representation) {
        int nStress = 0;
        for (int i = 0; i < representation.length(); i++) {
            if (representation.charAt(i) == symbolOfStress) {
                nStress++;
            }
        }
        return nStress;
    }

    /**
     * from list of words receive segment with meter representations
     * @param listWords
     * @param language
     * @return segment
     */
    public static SegmentOfPortion buildSegmentMeterRepresentationWithAllOptions(List<Word> listWords, String language) {

        List<String> varSegmentMeterRepresentations = new ArrayList<>();
        varSegmentMeterRepresentations.add("");//initializing
        listWords.stream().map((w) -> w.getMeterRepresentation()).map((wordRepresentations) -> {
            String[] arrayWordStress = new String[wordRepresentations.size() * varSegmentMeterRepresentations.size()];
            int j = 0;
            for (String currentSegmentRepresentation : varSegmentMeterRepresentations) {//circle by segment presentations
                for (String repr : wordRepresentations) {//circle by word presentation
                    arrayWordStress[j] = "" + currentSegmentRepresentation + repr;
                    j++;
                }
            }
            return arrayWordStress;
        }).forEach((arrayWordStress) -> {
            varSegmentMeterRepresentations.clear();
            varSegmentMeterRepresentations.addAll(Arrays.asList(arrayWordStress));
        });

        //correct and remove "impossible" combinations
        varSegmentMeterRepresentations.removeIf(
                s -> (s.contains("" + symbolOfStress + symbolOfStress + symbolOfStress))
                        || (s.startsWith("" + symbolOfStress + symbolOfStress))
                        || (s.endsWith("" + symbolOfStress + symbolOfStress))
                        || (!s.contains("" + symbolOfStress))
                        || (!checkSensibleRepresentationOfMeter(s, listWords, language))
        );

        SegmentOfPortion s = new SegmentOfPortion(varSegmentMeterRepresentations);

        return s;
    }

    /**
     * check whether all is OK with representations
     * @param meterRepresentation
     * @param listWords
     * @param language
     * @return
     */
    private static boolean checkSensibleRepresentationOfMeter(String meterRepresentation, List<Word> listWords, String language) {
        if (!language.equals("ru")) {
            return true;
        } else {
            return VocalAnalisysSegmentRu.checkSensibleRepresentationOfMeter(meterRepresentation, listWords);
        }
    }

    // === public instance methods ===================================================

    /**
     * after meter was defined (but not precisely) we need to fill segment with metric chsracteristics
     * @param priorityMap
     * @param mainGroup
     * @param secondGroup
     * @param mainPart
     * @param secondPart
     */
    public void fillSegmentWithMeterCharacteristics(Map priorityMap, String mainGroup, String secondGroup, int mainPart, int secondPart) {

        String repr = "";
        int nStresses = 0;
        int nLastStressPosition = 0;

        DynamicTableRythm dtMeters = getTableOfMeterDefinitions();
        if (dtMeters == null) {
            log.error("Null instead table of meter definitions.", IllegalArgumentException.class);
            throw new IllegalArgumentException("Null instead table of meter definitions.");
        }
        if (dtMeters.getSize() == 0) {
            log.error("0-dimensioned table of meter definitions.", IllegalArgumentException.class);
            throw new IllegalArgumentException("0-dimensioned table of meter definitions.");
        }

        //setNumberSyllable from the first representation (all must have the same length)
        repr = (String) dtMeters.getValue("Segment representation", 0);
        int numberOfSyllable = repr.length();
        setNumberSyllable(numberOfSyllable);

        // if table consists from one line - fill from it
        if (dtMeters.getSize() == 1) {
            fillMeterCharacteristicsVariables(dtMeters, 0);
        } else {
            if (mainGroup.contains("Unknown")) {//unknowm meter

                boolean wasDefined = false;
                //may be there is well-defined meter. But we choose representation with maximal number of stresses or with latest position of stress
                for (int i = 0; i < dtMeters.getSize(); i++) {
                    if (!dtMeters.getValue("Meter", i).toString().contains("Unknown")) {
                        repr = (String) dtMeters.getValue("Segment representation", i);
                        if (wasDefined) {// if there is caesura, but meter was defined without, continue
                            if (Integer.parseInt(dtMeters.getValue("Сaesura syllable", i).toString()) > getNumberСaesuraSyllable()) {
                                continue;
                            }
                        }
                        if ((getNumberOfStress(repr) > nStresses) || (repr.lastIndexOf(symbolOfStress) > nLastStressPosition)) {
                            nStresses = getNumberOfStress(repr);
                            nLastStressPosition = repr.lastIndexOf(symbolOfStress);
                            fillMeterCharacteristicsVariables(dtMeters, i);
                            wasDefined = true;
                        }
                    }
                }

                if (!wasDefined) {
                    for (int i = 0; i < dtMeters.getSize(); i++) {
                        repr = (String) dtMeters.getValue("Segment representation", i);
                        if (wasDefined) {// if there is caesura, but meter was defined without, continue
                            if (Integer.parseInt(dtMeters.getValue("Сaesura syllable", i).toString()) > getNumberСaesuraSyllable()) {
                                continue;
                            }
                        }
                        if ((getNumberOfStress(repr) > nStresses) || (repr.lastIndexOf(symbolOfStress) > nLastStressPosition)) {
                            nStresses = getNumberOfStress(repr);
                            nLastStressPosition = repr.lastIndexOf(symbolOfStress);
                            fillMeterCharacteristicsVariables(dtMeters, i);
                            wasDefined = true;
                        }
                    }
                }

            } else {
                if ((mainPart >= VersePortionForRythm.getValidLevelOfMainMeterGroupInVerseText()) && (mainPart - secondPart) >= VersePortionForRythm.getValidDifferenceBetweenTwoMainGroupsInVerseText()) {
                    // that's good defined meter
                    boolean wasDefined = false;
                    //may be there is well-defined meter. But we choose representation with maximal number of stresses
                    for (int i = 0; i < dtMeters.getSize(); i++) {
                        if (dtMeters.getValue("Meter", i).toString().equals(mainGroup)) {
                            repr = (String) dtMeters.getValue("Segment representation", i);
                            if (wasDefined) {// if there is caesura, but meter was defined without, continue
                                if (Integer.parseInt(dtMeters.getValue("Сaesura syllable", i).toString()) > getNumberСaesuraSyllable()) {
                                    continue;
                                }
                            }
                            if ((getNumberOfStress(repr) > nStresses) || (repr.lastIndexOf(symbolOfStress) > nLastStressPosition)) {
                                nStresses = getNumberOfStress(repr);
                                nLastStressPosition = repr.lastIndexOf(symbolOfStress);
                                fillMeterCharacteristicsVariables(dtMeters, i);
                                wasDefined = true;
                            }
                        }
                    }

                    if (!wasDefined) {
                        for (int i = 0; i < dtMeters.getSize(); i++) {
                            repr = (String) dtMeters.getValue("Segment representation", i);
                            if (wasDefined) {// if there is caesura, but meter was defined without, continue
                                if (Integer.parseInt(dtMeters.getValue("Сaesura syllable", i).toString()) > getNumberСaesuraSyllable()) {
                                    continue;
                                }
                            }
                            if ((getNumberOfStress(repr) > nStresses) || (repr.lastIndexOf(symbolOfStress) > nLastStressPosition)) {
                                nStresses = getNumberOfStress(repr);
                                nLastStressPosition = repr.lastIndexOf(symbolOfStress);
                                fillMeterCharacteristicsVariables(dtMeters, i);
                                wasDefined = true;
                            }
                        }
                    }

                } else if ((mainPart + secondPart) >= VersePortionForRythm.getValidLevelOfMainMeterGroupInVerseText()) {
                    // two main meters compete one with another
                    boolean wasDefined = false;
                    for (int i = 0; i < dtMeters.getSize(); i++) {
                        if (dtMeters.getValue("Meter", i).toString().equals(mainGroup) || dtMeters.getValue("Meter", i).toString().equals(secondGroup)) {
                            repr = (String) dtMeters.getValue("Segment representation", i);
                            if (wasDefined) {// if there is caesura, but meter was defined without, continue
                                if (Integer.parseInt(dtMeters.getValue("Сaesura syllable", i).toString()) > getNumberСaesuraSyllable()) {
                                    continue;
                                }
                            }
                            if ((getNumberOfStress(repr) > nStresses) || (repr.lastIndexOf(symbolOfStress) > nLastStressPosition)) {
                                nStresses = getNumberOfStress(repr);
                                nLastStressPosition = repr.lastIndexOf(symbolOfStress);
                                fillMeterCharacteristicsVariables(dtMeters, i);
                                wasDefined = true;
                            }
                        }
                    }
                    if (!wasDefined) {
                        for (int i = 0; i < dtMeters.getSize(); i++) {
                            repr = (String) dtMeters.getValue("Segment representation", i);
                            if (wasDefined) {// if there is caesura, but meter was defined without, continue
                                if (Integer.parseInt(dtMeters.getValue("Сaesura syllable", i).toString()) > getNumberСaesuraSyllable()) {
                                    continue;
                                }
                            }
                            if ((getNumberOfStress(repr) > nStresses) || (repr.lastIndexOf(symbolOfStress) > nLastStressPosition)) {
                                nStresses = getNumberOfStress(repr);
                                nLastStressPosition = repr.lastIndexOf(symbolOfStress);
                                fillMeterCharacteristicsVariables(dtMeters, i);
                                wasDefined = true;
                            }
                        }
                    }
                } else {
                    // that is 'dolnik', 'free verse' there is no 'tough' meter. There is no criterium

                    boolean wasDefined = false;
                    for (int i = 0; i < dtMeters.getSize(); i++) {
                        if (dtMeters.getValue("Meter", i).toString().equals(mainGroup)) {
                            repr = (String) dtMeters.getValue("Segment representation", i);
                            if (wasDefined) {// if there is caesura, but meter was defined without, continue
                                if (Integer.parseInt(dtMeters.getValue("Сaesura syllable", i).toString()) > getNumberСaesuraSyllable()) {
                                    continue;
                                }
                            }
                            if ((getNumberOfStress(repr) > nStresses) || (repr.lastIndexOf(symbolOfStress) > nLastStressPosition)) {
                                nStresses = getNumberOfStress(repr);
                                nLastStressPosition = repr.lastIndexOf(symbolOfStress);
                                fillMeterCharacteristicsVariables(dtMeters, i);
                                wasDefined = true;
                            }
                        }
                    }
                    if (!wasDefined) {
                        for (int i = 0; i < dtMeters.getSize(); i++) {
                            repr = (String) dtMeters.getValue("Segment representation", i);
                            if (wasDefined) {// if there is caesura, but meter was defined without, continue
                                if (Integer.parseInt(dtMeters.getValue("Сaesura syllable", i).toString()) > getNumberСaesuraSyllable()) {
                                    continue;
                                }
                            }
                            if ((getNumberOfStress(repr) > nStresses) || (repr.lastIndexOf(symbolOfStress) > nLastStressPosition)) {
                                nStresses = getNumberOfStress(repr);
                                nLastStressPosition = repr.lastIndexOf(symbolOfStress);
                                fillMeterCharacteristicsVariables(dtMeters, i);
                                wasDefined = true;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * with meter schema get meter definition
     * @param language
     * @return
     */
    public Map getMeterDefinitions(String language) {
        if (!language.equals("ru")) {
            log.error("Unknown language for meter's definition!", IllegalArgumentException.class);
            throw new IllegalArgumentException("Unknown language for meter's definition!");
        } else {
            VocalAnalisysSegmentRu vocalSegment = new VocalAnalisysSegmentRu(this);
            return vocalSegment.getRythmSchemaOfTheText();
        }
    }

    //=== private instance methods
    private int getValidLevelOfMainMeterGroupInVerseText() {
        log.error("Not supported yet.", IllegalArgumentException.class);
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * filling segment fields, when we have all lines with meter-definitions
     * @param dtMeters
     * @param nRow
     */
    private void fillMeterCharacteristicsVariables(DynamicTableRythm dtMeters, int nRow) {

        String repr = (String) dtMeters.getValue("Segment representation", nRow);
        setMeter((String) dtMeters.getValue("Meter", nRow));
        setChoosedMeterRepresentation((String) dtMeters.getValue("Segment representation", nRow));
        setNumberOfTonicFoot((Integer) dtMeters.getValue("Number tonic foot", nRow));
        setNumberСaesuraSyllable((Integer) dtMeters.getValue("Сaesura syllable", nRow));
        // ending
        int posLastSymbolOfStress = repr.lastIndexOf(symbolOfStress);
        if (posLastSymbolOfStress >= 0) {
            setEnding("..." + repr.substring(posLastSymbolOfStress, repr.length()));
        }
        setDuration(repr);
    }
}

