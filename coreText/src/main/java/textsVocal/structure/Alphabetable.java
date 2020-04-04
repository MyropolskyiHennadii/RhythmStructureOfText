package textsVocal.structure;

public interface Alphabetable<T> {
    public Number calculateDuration(String word);//calculate duration of segment
    public T getRythmSchemaOfTheText();//getting schema stresses per syllables
}