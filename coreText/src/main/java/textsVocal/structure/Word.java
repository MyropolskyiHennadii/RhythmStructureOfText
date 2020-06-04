package textsVocal.structure;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * class for future needs. Only for stresses it's redundant, but if we need grammatic...
 */
public class Word {

    //=== fields ============================================================
    private String textWord;//original string
    private String meterRepresentationForUser;//meter representation for user
    private Set<String> meterRepresentation;// set of meter's representations
    private String mainForm;//main grammatic form
    private String partOfSpeech;//part of speech
    private int numSyllable;//numner of syllables
    private int duration;//duration

    //=== constructors ===================================================
    public Word(String textWord, String meterRepresentation, String mainForm, String partOfSpeech) {
        this.textWord = textWord;
        this.meterRepresentation = new HashSet<>();
        this.meterRepresentation.add(meterRepresentation);
        this.mainForm = mainForm;
        this.partOfSpeech = partOfSpeech;
    }

    Word() {
        this.meterRepresentation = new HashSet<>();
    }

    //=== getters and setters ================================================
    public String getTextWord() {
        return textWord;
    }

    public void setTextWord(String textWord) {
        this.textWord = textWord;
    }

    public String getMeterRepresentationForUser() {
        return meterRepresentationForUser;
    }

    public void setMeterRepresentationForUser(String meterRepresentationForUser) {
        this.meterRepresentationForUser = meterRepresentationForUser;
    }

    public Set<String> getMeterRepresentation() {
        return meterRepresentation;
    }

    public String getMainForm() {
        return mainForm;
    }

    public void setMainForm(String mainForm) {
        this.mainForm = mainForm;
    }

    public int getNumSyllable() {
        return numSyllable;
    }

    public void setNumSyllable(int numSyllable) {
        this.numSyllable = numSyllable;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getPartOfSpeech() {
        return partOfSpeech;
    }

    public void setPartOfSpeech(String partOfSpeech) {
        this.partOfSpeech = partOfSpeech;
    }

    //===overriden methods=================================================
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + this.numSyllable;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Word other = (Word) obj;
        return Objects.equals(this.textWord.toLowerCase().trim(), other.textWord.toLowerCase().trim());
    }

    @Override
    public String toString() {
        return "Word{" + "textWord=" + textWord + ", meterRepresentationForUser=" + meterRepresentationForUser + '}';
    }

    //===public instance methods ==========================================
    public void addMeterRepresentation(String meterRepresentation) {
        this.meterRepresentation.add(meterRepresentation);
    }
}
