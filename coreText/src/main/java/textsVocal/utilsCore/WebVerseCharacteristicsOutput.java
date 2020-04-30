package textsVocal.utilsCore;

import textsVocal.structure.SegmentOfPortion;

public class WebVerseCharacteristicsOutput {

    private String id;
    private int nPortion;
    private int nSegment;
    private String words;
    private String meterRepresentation;
    private String meter;
    private String meterRepresentationWithSpaces;
    private int numberOfTonicFoot;
    private int shiftRegularMeterOnSyllable;
    private int numberOfSyllable;
    private SegmentOfPortion segment;

    public WebVerseCharacteristicsOutput(String id, int nPortion, int nSegment, String words,
                                         String meterRepresentation, String meter,
                                         String meterRepresentationWithSpaces,
                                         int numberOfTonicFoot, int shiftRegularMeterOnSyllable,
                                         int numberOfSyllable, SegmentOfPortion segment) {
        this.id = id;
        this.nPortion = nPortion;
        this.nSegment = nSegment;
        this.words = words;
        this.meterRepresentation = meterRepresentation;
        this.meter = meter;
        this.meterRepresentationWithSpaces = meterRepresentationWithSpaces;
        this.numberOfTonicFoot = numberOfTonicFoot;
        this.shiftRegularMeterOnSyllable = shiftRegularMeterOnSyllable;
        this.numberOfSyllable = numberOfSyllable;
        this.segment = segment;
    }

    public String getId() {
        return id;
    }

    public int getnPortion() {
        return nPortion;
    }

    public int getnSegment() {
        return nSegment;
    }

    public String getWords() {
        return words;
    }

    public String getMeterRepresentation() {
        return meterRepresentation;
    }

    public String getMeter() {
        return meter;
    }

    public String getMeterRepresentationWithSpaces() {
        return meterRepresentationWithSpaces;
    }

    public int getNumberOfTonicFoot() {
        return numberOfTonicFoot;
    }

    public int getShiftRegularMeterOnSyllable() {
        return shiftRegularMeterOnSyllable;
    }

    public int getNumberOfSyllable() {
        return numberOfSyllable;
    }

    public SegmentOfPortion getSegment() {
        return segment;
    }

    @Override
    public String toString() {
        return meter + "-" + numberOfTonicFoot;
    }
}
