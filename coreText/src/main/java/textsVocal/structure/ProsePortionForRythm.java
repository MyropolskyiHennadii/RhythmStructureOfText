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
    public <T> void fillPortionWithCommonRythmCharacteristics(T t) {
        fillProsePortionWithCommonRythmCharacteristics();
    }

    @Override
    public void resumeOutput(int nPortion, StringBuilder outputAccumulation, String pathToFileOutput) {
   /*     outputAccumulation.append("\n");
        outputAccumulation.append("Portion #" + nPortion + "\n");
        outputAccumulation.append("==========================\n");

        DynamicTableRythm dt = this.getDtOfTextSegmentsAndStresses();
        List<SegmentOfPortion> listSegments = this.getListOfSegments();
        String nameOfFirstColumn = (String)dt.getNamesOfColumn().toArray()[1];

        outputLineInResume(outputAccumulation, new String[]{"Line", "Meter representation", "Meter-number of foots", "Shift meter (N syllable)", "Quantity of syllables"});
        for (int i = 0; i < listSegments.size(); i++) {
            Integer nSegment = listSegments.get(i).getSegmentIdentifier();
            List<String> words = (List<String>) dt.getValueFromColumnAndRowByCondition("Word", nameOfFirstColumn, nSegment);
            String line = words.stream().map(s -> s + " ").reduce("", String::concat).trim();
            String meterRepresentation = listSegments.get(i).getChoosedMeterRepresentation().trim();
            String meter = listSegments.get(i).getMeter().trim();
            int numberOfTonicFoot = listSegments.get(i).getNumberOfTonicFoot();
            int numberСaesuraSyllable = listSegments.get(i).getnumberCaesuraSyllable();
            outputLineInResume(outputAccumulation, new String[]{line, meterRepresentation, "[" + numberOfTonicFoot + "-" + meter + "]",
                    "[" + numberСaesuraSyllable + "]", "[" + listSegments.get(i).getNumberSyllable() + "]"});
        }
        outputAccumulation.append("==========================\n");
        outputAccumulation.append("\n");
        outputAccumulation.append("Stress profile\n");
        outputAccumulation.append("Number of syllable\n");
        double[] stressProfile = getStressProfile();
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
            try (FileWriter fw = new FileWriter(pathToFileOutput, true)) {
                fw.write(outputAccumulation.toString());
            }
            catch (FileNotFoundException e) {
                getLog().error("Something wrong with output!" + e.getMessage());
                e.getMessage();
            } catch (IOException e) {
                getLog().error("Something wrong with output!" + e.getMessage());
                e.getMessage();
            }
        }
        //clear outputAccumulation before next portion*/
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
            List<String> schema = s.getMeterRepresentations();
            for (int i = 0; i < schema.size(); i++) {
                System.out.println(s.getSegmentIdentifier()+":"+schema.get(i));
            }
        }
    }

    //== private instance methods
    private void outputLineInResume(StringBuilder out, String[] outputArr) {
  /*      int[] lengthInSymbols = new int[5];//length of columns in symbols
        lengthInSymbols[0] = 48;
        lengthInSymbols[1] = 24;
        lengthInSymbols[2] = 24;
        lengthInSymbols[3] = 24;
        lengthInSymbols[4] = 24;

        if (outputArr.length != lengthInSymbols.length){
            getLog().error("Non-equal arrays!");
            throw new IllegalArgumentException();
        }

        for (int i=0; i<lengthInSymbols.length;i++){
            String s = outputArr[i];
            int l = lengthInSymbols[i];
            if (s.length() > l){
                s = s.substring(0, l-1);
            }
            if (s.length() < l){
                while(s.length() < l){
                    s += " ";
                }
            }
            out.append("| " + s);
        }
        out.append("\n");*/
    }

}

