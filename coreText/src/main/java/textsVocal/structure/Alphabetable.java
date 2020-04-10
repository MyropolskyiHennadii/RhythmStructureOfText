package textsVocal.structure;

public interface Alphabetable<T> {
    Number calculateDuration(String word);//calculate duration of segment
    T getRythmSchemaOfTheText();//getting schema stresses per syllables
}