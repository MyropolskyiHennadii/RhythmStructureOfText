package textsVocal.structure;

import textsVocal.utils.DynamicTableRythm;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ProsePortionForRythm extends TextForRythm {

    private String pText;//formatted text
    private String originalText;//original text

    // === constructor ===================================================
    public ProsePortionForRythm(String pText) {
        this.originalText = pText;
        this.pText = prepareStringForParsing(pText, SYMB_BRIEF_PUNCTUATION, SYMB_SPACE).toString();
    }

    public ProsePortionForRythm() {
    }

    // === public instance overriden methods ============================
    @Override
    public void reset(String pText){
        this.pText = prepareStringForParsing(pText, SYMB_BRIEF_PUNCTUATION, SYMB_SPACE).toString();
        this.originalText = pText;
    }

    @Override
    public String getpText() {
        return pText;
    }

    @Override
    public DynamicTableRythm parsePortionOfText() {

        // names of columns
        List<String> namesOfColumns = new ArrayList<>();
        namesOfColumns.add("Number of segment");
        namesOfColumns.add("Number of sentence");
        namesOfColumns.add("Sentence");
        namesOfColumns.add("Number of word");
        namesOfColumns.add("Word");
        namesOfColumns.add("Word-object / Stress form");

        // supliers for lists with data
        List<Supplier<?>> sup = new ArrayList<>();
        sup.add(ArrayList<Integer>::new);
        sup.add(ArrayList<Integer>::new);
        sup.add(ArrayList<String>::new);
        sup.add(ArrayList<Integer>::new);
        sup.add(ArrayList<String>::new);
        sup.add(ArrayList<Word>::new);

        DynamicTableRythm prepareTable = new DynamicTableRythm(namesOfColumns, sup);

        int NumberOfFragment = 1;
        int NumberOfSentence = 1;
        int NumberOfWord = 1;
        StringBuilder sbText = new StringBuilder(pText);
        String fragment = "";
        String sentence = "";
        String word = "";
        int posSymbol = -1;
        List<Object> addData = new ArrayList<>();

        do {
            fragment = textFragmentToDelimiter(sbText, SYMB_SEGMENT);
            StringBuilder fragmentToSentence = (fragment == null ? new StringBuilder(sbText) : new StringBuilder(fragment));

            do {
                sentence = textFragmentToDelimiter(fragmentToSentence, SYMB_SENTENCE);
                StringBuilder SentenceToWord = (sentence == null ? fragmentToSentence : new StringBuilder(sentence));

                do {
                    word = textFragmentToDelimiter(SentenceToWord, SYMB_SPACE);
                    StringBuilder wordToTable = (word == null ? SentenceToWord : new StringBuilder(word));

                    // symbol "Return" - away
                    for (CharSequence delimiter : SYMB_SEGMENT) {
                        posSymbol = wordToTable.indexOf("" + delimiter);
                        if (posSymbol >= 0) {
                            wordToTable.replace(posSymbol, posSymbol + 1, "");
                        }
                    }

                    // line with only spaces - away
                    if (("!" + wordToTable.toString().trim() + "!").equals("!!")) {
                        continue;
                    }

                    addData.clear();
                    addData.add(NumberOfFragment);
                    addData.add(NumberOfSentence);
                    addData.add((sentence == null ? wordToTable.toString().trim() : sentence.trim()));
                    addData.add(NumberOfWord);
                    addData.add(wordToTable.toString().trim());
                    //addData.add(cleanWordFromPunctuation(wordToTable, SYMB_PUNCTUATION));
                    addData.add(new Word());

                    if (!prepareTable.setRow(addData)) {
                        break;
                    }

                    NumberOfWord++;

                } while (word != null);

                NumberOfSentence++;

            } while (sentence != null);

            NumberOfFragment++;

        } while (fragment != null);

        setDtOfTextSegmentsAndStresses(prepareTable);

        return prepareTable;
    }

    @Override
    public void resumeOutput(int nPortion, StringBuilder outputAccumulation, String pathToFileOutput) {

    }

}

