package textsVocal.structure;

import textsVocal.utils.DynamicTableRythm;
import textsVocal.utils.FileTreatment;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static textsVocal.structure.PortionOfTextAnalyser.*;

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

    //== static method ==
    public static void outputStressProfileOfWholeText(StringBuilder outputAccumulation, String pathToFileOutput) {

        outputAccumulation.append("Stress profile\n");
        outputAccumulation.append("Number of syllable\n");

        double[] stressProfile = getStressProfileFromWholeText();
        for (int i = 0; i < stressProfile.length; i++) {
            outputAccumulation.append("\t" + (i + 1));
        }
        outputAccumulation.append("\n");
        outputAccumulation.append("% of stress      \n");
        for (int i = 0; i < stressProfile.length; i++) {
            outputAccumulation.append("\t" + (int) stressProfile[i]);
        }
        outputAccumulation.append("\n");
        outputAccumulation.append("==========================\n");

        if (pathToFileOutput.isEmpty()) {//output to console
            System.out.println(outputAccumulation.toString());
        } else // writing to file
        {
            FileTreatment.outputResultToFile(outputAccumulation, pathToFileOutput);
        }
        //clear outputAccumulation before next portion
        outputAccumulation.delete(0, outputAccumulation.length() - 1);
        //clear static variable:
        ClearListMeterRepresentation();

    }
    // === public instance overriden methods ============================
    @Override
    public void reset(String pText) {
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
        //namesOfColumns.add("Number of paragraph"); //Paragraphs are portion, This field is unnecessary
        namesOfColumns.add("Number of sentence");
        namesOfColumns.add("Sentence");
        namesOfColumns.add("Number of word");
        namesOfColumns.add("Word");
        namesOfColumns.add("Word-object / Stress form");

        // supliers for lists with data
        List<Supplier<?>> sup = new ArrayList<>();
        //sup.add(ArrayList<Integer>::new); //Paragraphs are portion, This field is unnecessary
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

        //test
        StringBuilder fragmentToSentence = new StringBuilder(pText);

    /*    do { //Paragraphs are portion, This circle is unnecessary
            fragment = textFragmentToDelimiter(sbText, SYMB_PARAGRAPH);
            StringBuilder fragmentToSentence = (fragment == null ? new StringBuilder(sbText) : new StringBuilder(fragment));*/

        do {
            sentence = textFragmentToDelimiter(fragmentToSentence, SYMB_SENTENCE);
            StringBuilder SentenceToWord = (sentence == null ? fragmentToSentence : new StringBuilder(sentence));

            do {
                word = textFragmentToDelimiter(SentenceToWord, SYMB_SPACE);
                StringBuilder wordToTable = (word == null ? SentenceToWord : new StringBuilder(word));

                // symbol "Return" - away
                for (CharSequence delimiter : SYMB_PARAGRAPH) {
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
/*
            NumberOfFragment++;

        } while (fragment != null);*/ //Paragraphs are portion, This circle is unnecessary

        setDtOfTextSegmentsAndStresses(prepareTable);

        return prepareTable;
    }

    @Override
    public <T> void fillPortionWithCommonRythmCharacteristics(T t) {
        fillProsePortionWithCommonRythmCharacteristics();
    }

    @Override
    public void resumeOutput(int nPortion, StringBuilder outputAccumulation, String pathToFileOutput) {
        outputAccumulation.append("\n");
        outputAccumulation.append("Paragraph #" + nPortion + "\n");
        outputAccumulation.append("==========================\n");

        DynamicTableRythm dt = this.getDtOfTextSegmentsAndStresses();
        List<SegmentOfPortion> listSegments = this.getListOfSegments();
        String nameOfFirstColumn = (String) dt.getNamesOfColumn().toArray()[1];

        for (int i = 0; i < listSegments.size(); i++) {
            Integer nSegment = listSegments.get(i).getSegmentIdentifier();
            outputAccumulation.append("Sentence #" + nSegment + "\n");
            List<String> words = (List<String>) dt.getValueFromColumnAndRowByCondition("Word", nameOfFirstColumn, nSegment);
            String line = words.stream().map(s -> s + " ").reduce("", String::concat).trim();
            String meterRepresentationWithSpaces = listSegments.get(i).getMeterRepresentationWithSpaces().trim();
            outputAccumulation.append(line + "\n");
            meterRepresentationOfPortion.add(listSegments.get(i).getSelectedMeterRepresentation());//all portion
            outputAccumulation.append("[" + meterRepresentationWithSpaces + "]\n");
        }
        outputAccumulation.append("==========================\n");
        outputAccumulation.append("\n");

        if (pathToFileOutput.isEmpty()) {//output to console
            System.out.println(outputAccumulation.toString());
        } else // writing to file
        {
            FileTreatment.outputResultToFile(outputAccumulation, pathToFileOutput);
        }
        //clear outputAccumulation before next portion
        outputAccumulation.delete(0, outputAccumulation.length() - 1);
    }


    //== public instance method ==

    /**
     * fill every segment with rythm schema
     */
    public void fillProsePortionWithCommonRythmCharacteristics() {
        List<SegmentOfPortion> preparedListOfSegment = getListOfSegments();
        if (preparedListOfSegment.isEmpty()) {
            getLog().error("There is empty list of segments. Impossible to define meter in portion.");
            throw new IllegalArgumentException("There is empty list of segments. Impossible to define meter in portion.");
        }

        for (SegmentOfPortion s : preparedListOfSegment) {
            List<String> schemaMeter = s.getMeterRepresentations();
            //select schema with maximal number of stresses
            String meterSchema = "";
            int maxNumberOfStresses = 0;
            for (String sch : schemaMeter) {
                if (SegmentOfPortion.getNumberOfSegmentStress(sch) > maxNumberOfStresses) {
                    meterSchema = sch.trim();
                }
            }
            s.setMeterRepresentationForUser(meterSchema);
            s.setSelectedMeterRepresentation(meterSchema);
        }
    }

}

