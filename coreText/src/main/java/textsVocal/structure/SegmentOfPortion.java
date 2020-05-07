package textsVocal.structure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import textsVocal.config.CommonConstants;
import textsVocal.ru.VocalAnalisysRu;
import textsVocal.utilsCommon.DataTable;

import java.util.*;
import java.util.function.Supplier;

import static textsVocal.structure.TextPortionForRythm.symbolOfStress;

/**
 * class for segments of portion
 */
public class SegmentOfPortion {

    //=== fields ==================================================
    private static final Logger log = LoggerFactory.getLogger(SegmentOfPortion.class);//logger

    private final List<String> meterRepresentations;// full list of representations such as "...0101..."  (probably more than one element)
    private final DataTable tableOfMeterDefinitions;// table with definition every meter in meterRepresentations
    private final List<Integer> schemaOfSpaces;//numbers of syllables with space: for ceasure

    private String meterRepresentationForUser;//first meter representation with "?", whether they are
    private Integer segmentIdentifier;// number of segment
    private int numberSyllable;
    private double duration = 0;// in our case duration == numberSyllable, but theoretically may be something other
    private String meter;//classic meter definition, if there is
    private int numberOfTonicFoot;// classic number of foots, if there is
    private int shiftRegularMeterOnSyllable;// number of syllable with shift regular stress, if there is
    private String selectedMeterRepresentation;//if there is more than one representations, here we define main (if there is)
    private String meterRepresentationWithSpaces;// selected meter with spaces
    private String ending;//ending of segment

    //=== constructor ============================================
    public SegmentOfPortion(List<String> meterRepresentations, List<Integer> schemaOfSpaces) {

        List<Integer> spaces = new ArrayList<>();
        int count = 0;
        for (Integer schemaOfSpace : schemaOfSpaces) {
            count += schemaOfSpace;
            spaces.add(count);
        }

        this.schemaOfSpaces = spaces;
        this.meterRepresentations = meterRepresentations;

        // names of columns
        List<String> namesOfColumns = new ArrayList<>();
        namesOfColumns.add("Segment representation");
        namesOfColumns.add("Meter");
        namesOfColumns.add("Number tonic foot");
        namesOfColumns.add("Shift regular meter on syllable");

        // supliers for lists with data
        List<Supplier<?>> sup = new ArrayList<>();
        sup.add(ArrayList<String>::new);
        sup.add(ArrayList<String>::new);
        sup.add(ArrayList<Integer>::new);
        sup.add(ArrayList<Integer>::new);

        this.tableOfMeterDefinitions = new DataTable(namesOfColumns, sup);

    }

    //=== static methods ==================================================
    /**
     * returns number of stresses in the stress schema
     * @param representation stress schema like  0101...
     * @return number of stress in stress schema
     */
    public static int getNumberOfSegmentStress(String representation) {
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
     * @param listWords list of Word-objects
     * @param language language of whole text
     * @return segment instance of class
     */
    public static SegmentOfPortion buildSegmentMeterRepresentationWithAllOptions(List<Word> listWords, String language, boolean thisIsVerse) {

        List<String> varSegmentMeterRepresentations = new ArrayList<>();
        List<Integer> spacesSchema = new ArrayList<>();//positions of space
        varSegmentMeterRepresentations.add("");//initializing
        listWords.stream().map(Word::getMeterRepresentation).map((wordRepresentations) -> {
            String[] arrayWordStress = new String[wordRepresentations.size() * varSegmentMeterRepresentations.size()];
            int j = 0;
            for (String currentSegmentRepresentation : varSegmentMeterRepresentations) {//cycle by segment presentations
                for (String repr : wordRepresentations) {//cycle by word presentation
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
                        || thisIsVerse && ((s.startsWith("" + symbolOfStress + symbolOfStress))
                        || (s.endsWith("" + symbolOfStress + symbolOfStress)))
                        || (!s.contains("" + symbolOfStress))
                        || (!checkSensibleRepresentationOfMeter(s, listWords, language, thisIsVerse))
        );

        //for ceasure
        listWords.forEach(s-> spacesSchema.add(s.getNumSyllable()));

        return new SegmentOfPortion(varSegmentMeterRepresentations, spacesSchema);
    }

    /**
     * check whether all is OK with representations
     * @param meterRepresentation stress schema
     * @param listWords list of words-objects
     * @param language labguage of the whole text
     * @return true if all OK and false otherwise
     */
    private static boolean checkSensibleRepresentationOfMeter(String meterRepresentation, List<Word> listWords, String language, boolean thisIsVerse) {
        if (!language.equals("ru")) {
            return true;
        } else {
            return VocalAnalisysRu.checkSensibleRepresentationOfMeter(meterRepresentation, listWords, thisIsVerse);
        }
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

    public String getSelectedMeterRepresentation() {
        return selectedMeterRepresentation;
    }

    public void setSelectedMeterRepresentation(String selectedMeterRepresentation) {
        this.selectedMeterRepresentation = selectedMeterRepresentation;
        setDuration(selectedMeterRepresentation);
        setNumberSyllable(selectedMeterRepresentation.length());
        if ((this.schemaOfSpaces.size() > 0) && (!selectedMeterRepresentation.isEmpty())){
            StringBuilder meterRepresentationWithSpaces = new StringBuilder();
            for (int i = 0; i < selectedMeterRepresentation.length(); i++) {
                if(schemaOfSpaces.contains(i)){
                    {
                        meterRepresentationWithSpaces.append(" ");}
                }
                meterRepresentationWithSpaces.append(selectedMeterRepresentation.charAt(i));
            }
            setMeterRepresentationWithSpaces(meterRepresentationWithSpaces.toString().trim());
        }
    }

    public String getMeterRepresentationWithSpaces() {
        return meterRepresentationWithSpaces;
    }

    public void setMeterRepresentationWithSpaces(String meterRepresentationWithSpaces) {
        this.meterRepresentationWithSpaces = meterRepresentationWithSpaces;
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

    public int getShiftRegularMeterOnSyllable() {
        return shiftRegularMeterOnSyllable;
    }

    public void setShiftRegularMeterOnSyllable(int shiftRegularMeterOnSyllable) {
        this.shiftRegularMeterOnSyllable = shiftRegularMeterOnSyllable;
    }

    public Integer getSegmentIdentifier() {
        return segmentIdentifier;
    }

    public void setSegmentIdentifier(Integer segmentIdentifier) {
        this.segmentIdentifier = segmentIdentifier;
    }

    public List<String> getMeterRepresentations() {
        return meterRepresentations;
    }

    public String getMeterRepresentationForUser() {
        return meterRepresentationForUser;
    }

    public void setMeterRepresentationForUser(String meterRepresentationForUser) {
        this.meterRepresentationForUser = meterRepresentationForUser;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(String representation) {
        //TODO: that isn't alwayas a fact...
        this.duration = representation.length();
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public DataTable getTableOfMeterDefinitions() {
        return tableOfMeterDefinitions;
    }

    public List<Integer> getSchemaOfSpaces() {
        return schemaOfSpaces;
    }

    // === overriden methods ==============================================
    @Override
    public String toString() {
        return segmentIdentifier + ": " + meterRepresentations;
    }

    // === public instance methods ===================================================

    /**
     * after meter was defined (but not precisely) we need to fill segment with metric characteristics
     * @param mainGroup main group of meter in poem
     * @param secondGroup second group of meter in poem
     * @param mainPart part of main group in poem in %
     * @param secondPart part of second group in poem in %
     */
    public void fillVerseSegmentWithMeterCharacteristics(String mainGroup, String secondGroup, int mainPart, int secondPart) {

        String repr;
        int nStresses = 0;
        int nLastStressPosition = 0;
        CommonConstants constants = CommonConstants.getApplicationContext().getBean(CommonConstants.class);

        DataTable dtMeters = getTableOfMeterDefinitions();
        if (dtMeters == null) {
            log.error("Null instead table of meter definitions!");
            throw new IllegalArgumentException("Null instead table of meter definitions.");
        }
        if (dtMeters.getSize() == 0) {
            log.error("0-dimensioned table of meter definitions!");
            throw new IllegalArgumentException("0-dimensioned table of meter definitions.");
        }

        //setNumberSyllable from the first representation (all must have the same length)
        repr = (String) dtMeters.getValueFromColumnAndRow("Segment representation", 0);
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
                    if (!dtMeters.getValueFromColumnAndRow("Meter", i).toString().contains("Unknown")) {
                        repr = (String) dtMeters.getValueFromColumnAndRow("Segment representation", i);
                        if (wasDefined) {// if there is caesura, but meter was defined without, continue
                            if (Integer.parseInt(dtMeters.getValueFromColumnAndRow("Shift regular meter on syllable", i).toString()) > getShiftRegularMeterOnSyllable()) {
                                continue;
                            }
                        }
                        if ((getNumberOfSegmentStress(repr) > nStresses) || (repr.lastIndexOf(symbolOfStress) > nLastStressPosition)) {
                            nStresses = getNumberOfSegmentStress(repr);
                            nLastStressPosition = repr.lastIndexOf(symbolOfStress);
                            fillMeterCharacteristicsVariables(dtMeters, i);
                            wasDefined = true;
                        }
                    }
                }

                if (!wasDefined) {
                    for (int i = 0; i < dtMeters.getSize(); i++) {
                        repr = (String) dtMeters.getValueFromColumnAndRow("Segment representation", i);
                        if (wasDefined) {// if there is caesura, but meter was defined without, continue
                            if (Integer.parseInt(dtMeters.getValueFromColumnAndRow("Shift regular meter on syllable", i).toString()) > getShiftRegularMeterOnSyllable()) {
                                continue;
                            }
                        }
                        if ((getNumberOfSegmentStress(repr) > nStresses) || (repr.lastIndexOf(symbolOfStress) > nLastStressPosition)) {
                            nStresses = getNumberOfSegmentStress(repr);
                            nLastStressPosition = repr.lastIndexOf(symbolOfStress);
                            fillMeterCharacteristicsVariables(dtMeters, i);
                            wasDefined = true;
                        }
                    }
                }

            } else {
                if ((mainPart >= constants.getValidLevelOfMainMeterGroupInVerseText()) && (mainPart - secondPart) >= constants.getValidDifferenceBetweenTwoMainGroupsInVerseText()) {
                    // that's good defined meter
                    boolean wasDefined = false;
                    //may be there is well-defined meter. But we choose representation with maximal number of stresses
                    for (int i = 0; i < dtMeters.getSize(); i++) {
                        if (dtMeters.getValueFromColumnAndRow("Meter", i).toString().equals(mainGroup)) {
                            repr = (String) dtMeters.getValueFromColumnAndRow("Segment representation", i);
                            if (wasDefined) {// if there is caesura, but meter was defined without, continue
                                if (Integer.parseInt(dtMeters.getValueFromColumnAndRow("Shift regular meter on syllable", i).toString()) > getShiftRegularMeterOnSyllable()) {
                                    continue;
                                }
                            }
                            if ((getNumberOfSegmentStress(repr) > nStresses) || (repr.lastIndexOf(symbolOfStress) > nLastStressPosition)) {
                                nStresses = getNumberOfSegmentStress(repr);
                                nLastStressPosition = repr.lastIndexOf(symbolOfStress);
                                fillMeterCharacteristicsVariables(dtMeters, i);
                                wasDefined = true;
                            }
                        }
                    }

                    if (!wasDefined) {
                        for (int i = 0; i < dtMeters.getSize(); i++) {
                            repr = (String) dtMeters.getValueFromColumnAndRow("Segment representation", i);
                            if (wasDefined) {// if there is caesura, but meter was defined without, continue
                                if (Integer.parseInt(dtMeters.getValueFromColumnAndRow("Shift regular meter on syllable", i).toString()) > getShiftRegularMeterOnSyllable()) {
                                    continue;
                                }
                            }
                            if ((getNumberOfSegmentStress(repr) > nStresses) || (repr.lastIndexOf(symbolOfStress) > nLastStressPosition)) {
                                nStresses = getNumberOfSegmentStress(repr);
                                nLastStressPosition = repr.lastIndexOf(symbolOfStress);
                                fillMeterCharacteristicsVariables(dtMeters, i);
                                wasDefined = true;
                            }
                        }
                    }

                } else if ((mainPart + secondPart) >= constants.getValidLevelOfMainMeterGroupInVerseText()) {
                    // two main meters compete one with another
                    boolean wasDefined = false;
                    for (int i = 0; i < dtMeters.getSize(); i++) {
                        if (dtMeters.getValueFromColumnAndRow("Meter", i).toString().equals(mainGroup) || dtMeters.getValueFromColumnAndRow("Meter", i).toString().equals(secondGroup)) {
                            repr = (String) dtMeters.getValueFromColumnAndRow("Segment representation", i);
                            if (wasDefined) {// if there is caesura, but meter was defined without, continue
                                if (Integer.parseInt(dtMeters.getValueFromColumnAndRow("Shift regular meter on syllable", i).toString()) > getShiftRegularMeterOnSyllable()) {
                                    continue;
                                }
                            }
                            if ((getNumberOfSegmentStress(repr) > nStresses) || (repr.lastIndexOf(symbolOfStress) > nLastStressPosition)) {
                                nStresses = getNumberOfSegmentStress(repr);
                                nLastStressPosition = repr.lastIndexOf(symbolOfStress);
                                fillMeterCharacteristicsVariables(dtMeters, i);
                                wasDefined = true;
                            }
                        }
                    }
                    if (!wasDefined) {
                        for (int i = 0; i < dtMeters.getSize(); i++) {
                            repr = (String) dtMeters.getValueFromColumnAndRow("Segment representation", i);
                            if (wasDefined) {// if there is caesura, but meter was defined without, continue
                                if (Integer.parseInt(dtMeters.getValueFromColumnAndRow("Shift regular meter on syllable", i).toString()) > getShiftRegularMeterOnSyllable()) {
                                    continue;
                                }
                            }
                            if ((getNumberOfSegmentStress(repr) > nStresses) || (repr.lastIndexOf(symbolOfStress) > nLastStressPosition)) {
                                nStresses = getNumberOfSegmentStress(repr);
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
                        if (dtMeters.getValueFromColumnAndRow("Meter", i).toString().equals(mainGroup)) {
                            repr = (String) dtMeters.getValueFromColumnAndRow("Segment representation", i);
                            if (wasDefined) {// if there is caesura, but meter was defined without, continue
                                if (Integer.parseInt(dtMeters.getValueFromColumnAndRow("Shift regular meter on syllable", i).toString()) > getShiftRegularMeterOnSyllable()) {
                                    continue;
                                }
                            }
                            if ((getNumberOfSegmentStress(repr) > nStresses) || (repr.lastIndexOf(symbolOfStress) > nLastStressPosition)) {
                                nStresses = getNumberOfSegmentStress(repr);
                                nLastStressPosition = repr.lastIndexOf(symbolOfStress);
                                fillMeterCharacteristicsVariables(dtMeters, i);
                                wasDefined = true;
                            }
                        }
                    }
                    if (!wasDefined) {
                        for (int i = 0; i < dtMeters.getSize(); i++) {
                            repr = (String) dtMeters.getValueFromColumnAndRow("Segment representation", i);
                            if (wasDefined) {// if there is caesura, but meter was defined without, continue
                                if (Integer.parseInt(dtMeters.getValueFromColumnAndRow("Shift regular meter on syllable", i).toString()) > getShiftRegularMeterOnSyllable()) {
                                    continue;
                                }
                            }
                            if ((getNumberOfSegmentStress(repr) > nStresses) || (repr.lastIndexOf(symbolOfStress) > nLastStressPosition)) {
                                nStresses = getNumberOfSegmentStress(repr);
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
     * @param language language of whole text
     * @return map with number of segment as a key and set of stress schema as values
     */
    public Map<Integer, Set<String>> getMeterDefinitions(String language) {
        if (!language.equals("ru")) {
            log.error("Unknown language for meter's definition " + language +"!");
            throw new IllegalArgumentException("Unknown language for meter's definition!");
        } else {
            return VocalAnalisysRu.getMeterSchemaOfSegment(this);
        }
    }

    //=== private instance methods ===

    public void setEndingByRepresentation(String repr){
        int posLastSymbolOfStress = repr.lastIndexOf(symbolOfStress);
        if (posLastSymbolOfStress >= 0) {
            setEnding("..." + repr.substring(posLastSymbolOfStress));
        }
    }

    /**
     * filling segment fields, when we have all lines with meter-definitions
     * @param dtMeters data table with all possible meter definitions for this segment
     * @param nRow number of row in data table
     */
    private void fillMeterCharacteristicsVariables(DataTable dtMeters, int nRow) {

        String repr = (String) dtMeters.getValueFromColumnAndRow("Segment representation", nRow);
        setMeter((String) dtMeters.getValueFromColumnAndRow("Meter", nRow));
        setSelectedMeterRepresentation((String) dtMeters.getValueFromColumnAndRow("Segment representation", nRow));
        setNumberOfTonicFoot((Integer) dtMeters.getValueFromColumnAndRow("Number tonic foot", nRow));
        setShiftRegularMeterOnSyllable((Integer) dtMeters.getValueFromColumnAndRow("Shift regular meter on syllable", nRow));
        // ending
        setEndingByRepresentation(repr);
        setDuration(repr);
    }
}

