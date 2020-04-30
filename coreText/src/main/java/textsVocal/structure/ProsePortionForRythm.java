package textsVocal.structure;

import textsVocal.config.CommonConstants;
import textsVocal.config.HeaderAnFooterListsForWebOutput;
import textsVocal.utilsCommon.DynamicTableRythm;
import textsVocal.utilsCommon.FileTreatment;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static textsVocal.structure.PortionOfTextAnalyser.getStressProfileFromWholeText;

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
    public static void outputStressProfileOfWholeText(StringBuilder outputAccumulation, CommonConstants commonConstants) {

        outputAccumulation.append("Stress profile\n");
        outputAccumulation.append("Number of syllable\n");

        String[] stressProfile = getStressProfileFromWholeText();
        for (int i = 0; i < stressProfile.length; i++) {
            outputAccumulation.append("\t" + (i + 1));
        }
        outputAccumulation.append("\n");
        outputAccumulation.append("% of stress.\n");
        for (int i = 0; i < stressProfile.length; i++) {
            outputAccumulation.append("\t" + stressProfile[i]);
        }
        outputAccumulation.append("\n");
        outputAccumulation.append("==========================\n");
        //count of sentences
        int countLength = 0;
        int countSentences = 0;
        for (int i = 0; i < PortionOfTextAnalyser.getListOfInstance().size() ; i++) {
            List<SegmentOfPortion> listSegments = PortionOfTextAnalyser.getListOfInstance().get(i).getListOfSegments();
            for (int j = 0; j < listSegments.size(); j++) {
                countSentences++;
                countLength += listSegments.get(j).getSelectedMeterRepresentation().length();
            }
        }
        outputAccumulation.append("Number of sentence: " + countSentences + "\n");
        outputAccumulation.append("Average length of sentence in syllables: " + (int) countLength / countSentences + "\n");
        outputAccumulation.append("==========================\n");

        if (!commonConstants.isReadingFromFile()) {//output to console
            System.out.println(outputAccumulation.toString());
        } else // writing to file
        {
            FileTreatment.outputResultToFile(outputAccumulation, commonConstants.getFileOutputDirectory() + commonConstants.getFileOutputName());
        }
        //clear outputAccumulation before next portion
        outputAccumulation.delete(0, outputAccumulation.length() - 1);
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

    /**
     * form header for output
     * @return
     */
    public List<String> formHeaderLines(int nPortion){
        List<String> headerLines = new ArrayList<>();
        headerLines.add("\n");
        headerLines.add("Paragraph #" + nPortion + "\n");
        headerLines.add("==========================\n");
        return headerLines;
    }

    /**
     * form footer lines for output
     * * @return
     */
    public List<String> formFooterLines(){
        List<String> footerLines = new ArrayList<>();
        footerLines.add("\n");
        return footerLines;
    }

    /**
     * resume output web
     *
     * @param nPortion
     * @param outputAccumulation
     * @param commonConstants
     */
    public void prepareResumeOutputWeb(int nPortion, StringBuilder outputAccumulation, CommonConstants commonConstants) {
        HeaderAnFooterListsForWebOutput.getPortionHeaders().add(formHeaderLines(nPortion));
        HeaderAnFooterListsForWebOutput.getPortionFooters().add(formFooterLines());
    }

    /**
     * resume output without web (console)
     *
     * @param nPortion
     * @param outputAccumulation
     * @param commonConstants
     */
    public void resumeOutputConsole(int nPortion, StringBuilder outputAccumulation, CommonConstants commonConstants) {

        List<String> headerLines = formHeaderLines(nPortion);
        for(String line: headerLines){
            outputAccumulation.append(line);
        }

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
            //meterRepresentationOfPortion.add(listSegments.get(i).getSelectedMeterRepresentation());//all portion
            outputAccumulation.append("[" + meterRepresentationWithSpaces + "]\n");
        }
        outputAccumulation.append("==========================\n");
        List<String> footerLines = formFooterLines();
        for(String line: footerLines) {
            outputAccumulation.append(line);
        }

        if (!commonConstants.isReadingFromFile()) {//output to console
            System.out.println(outputAccumulation.toString());
        } else // writing to file
        {
            FileTreatment.outputResultToFile(outputAccumulation, commonConstants.getFileOutputDirectory() + commonConstants.getFileOutputName());
        }
        //clear outputAccumulation before next portion
        outputAccumulation.delete(0, outputAccumulation.length() - 1);
    }

    /**
     * resume output
     *
     * @param nPortion
     * @param outputAccumulation
     * @param commonConstants
     */
    @Override
    public void resumeOutput(int nPortion, StringBuilder outputAccumulation, CommonConstants commonConstants) {
        if (!commonConstants.isThisIsWebApp()) {
            resumeOutputConsole(nPortion, outputAccumulation, commonConstants);
        } else {
            prepareResumeOutputWeb(nPortion, outputAccumulation, commonConstants);
        }
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

