package textsVocal.console.dbRuMaintenance;

/**
 * class patterns for quick adding records to Russian dictionary
 */
public class DB_PatternItemRu implements HaveID{
    private int id;
    private int idPatternInDB;
    private String word;
    private String meterSchema;
    private String partOfSpeech;
    private String mainForm;
    private int numberSymbolsInEndingMainForm;

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DB_PatternItemRu that = (DB_PatternItemRu) o;
        return id == that.id;
    }

    @Override
    public String toString() {
        return  "" + id +
                "  |, pattern ='" + word + '\'' +
                ", meterSchema='" + meterSchema + '\'' +
                ", partSpeech='" + partOfSpeech;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public int getIdPatternInDB() {
        return idPatternInDB;
    }

    public String getWord() {
        return word;
    }

    public String getMeterSchema() {
        return meterSchema;
    }

    public String getPartOfSpeech() {
        return partOfSpeech;
    }

    public String getMainForm() {
        return mainForm;
    }

    public int getNumberSymbolsInEndingMainForm() {
        return numberSymbolsInEndingMainForm;
    }

    public DB_PatternItemRu(int id, int idPatternInDB, String word, String meterSchema,
                            String partOfSpeech, String mainForm, int numberSymbolsInEndingMainForm) {
        this.id = id;
        this.idPatternInDB = idPatternInDB;
        this.word = word;
        this.meterSchema = meterSchema;
        this.partOfSpeech = partOfSpeech;
        this.mainForm = mainForm;
        this.numberSymbolsInEndingMainForm = numberSymbolsInEndingMainForm;
    }
}
