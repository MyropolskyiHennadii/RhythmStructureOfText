package textsVocal.structure;

import textsVocal.config.CommonConstants;
import textsVocal.config.HeaderAnFooterListsForWebOutput;
import textsVocal.utilsCommon.DataTable;
import textsVocal.utilsCommon.FileTreatment;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * class for prose portion
 */
public class ProsePortionForRythm extends TextPortionForRythm {

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

    /**
     * output footer for prose in console
     * @param outputAccumulation StringBuilder stores output
     * @param commonConstants commom app constants
     */
    public static void outputFootProseConsole(StringBuilder outputAccumulation, CommonConstants commonConstants) {

        outputAccumulation.append("Stress profile of whole text\n");
        outputAccumulation.append("Number of syllable\n");

        String[] stressProfile = AnalyserPortionOfText.getStressProfileOfAllPortions();
        for (int i = 0; i < stressProfile.length; i++) {
            outputAccumulation.append("\t").append(i + 1);
        }
        outputAccumulation.append("\n");
        outputAccumulation.append("% of stress.\n");
        for (String s : stressProfile) {
            outputAccumulation.append("\t").append(s);
        }
        outputAccumulation.append("\n");
        outputAccumulation.append("==========================\n");

        //count of sentences
        outputAccumulation.append("Number of sentence: ").append(AnalyserPortionOfText.getNumberOfSegments()).append("\n");
        outputAccumulation.append("Average length of sentence in syllables: ").append(AnalyserPortionOfText.getAverageLengthOfSegments()).append("\n");
        outputAccumulation.append("Maximal length of sentence in syllables: ").append(AnalyserPortionOfText.getMaxLengthSegment()).append("\n");
        outputAccumulation.append("==========================\n");

        outputAccumulation.append("Distribution by length\n");
        outputAccumulation.append("Number of syllable\n");
        String[] distributionProfile = AnalyserPortionOfText.getDistributionSegmentByLength();
        for (int i = 0; i < distributionProfile.length; i++) {
            outputAccumulation.append("\t").append(i + 1);
        }
        outputAccumulation.append("\n");
        outputAccumulation.append("% of all.\n");
        for (String s : distributionProfile) {
            outputAccumulation.append("\t").append(s);
        }
        outputAccumulation.append("\n");
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

    /**
     * input corrections in portion's characteristics from web-user
     */
    public static void makeCorrectionInProseCharacteristicFromWebUser(List<String> checkedItems, List<String> changedValues) {

        String id;
        int numberPortion = -1;
        for (String checkedItem : checkedItems) {
            //parse id
            id = checkedItem;
            int posPoint = id.indexOf(".");
            int numberSegment;
            if (posPoint > -1) {
                numberPortion = Integer.parseInt(id.substring(0, posPoint));
                numberSegment = Integer.parseInt(id.substring(posPoint + 1));

                String newMeterRepresentation = changedValues.get(numberSegment - 1).trim();
                if (newMeterRepresentation.isEmpty()) {
                    continue;
                }

                //we'll clean and fill all values for this segment

                //code from VocalAnalysisSegmentRu
                List<SegmentOfPortion> listSegments = AnalyserPortionOfText.getListOfInstance().get(numberPortion - 1).getListOfSegments();
                SegmentOfPortion segment = listSegments.get(numberSegment - 1);

                String newMeterRepresentationWithoutSpaces = newMeterRepresentation;

                //probably new space schema
                segment.getSchemaOfSpaces().clear();
                for (int j = 0; j < newMeterRepresentation.length(); j++) {
                    for (CharSequence c : SYMB_SPACE) {
                        if (("" + c).equals(""+newMeterRepresentation.charAt(j))) {
                            segment.getSchemaOfSpaces().add(j + 1);
                        }
                    }
                }

                //and cleaning from space
                for (CharSequence c : SYMB_SPACE) {
                    newMeterRepresentationWithoutSpaces = newMeterRepresentation.replaceAll("" + c, "");
                }

                //then set all:
                segment.setSelectedMeterRepresentation(newMeterRepresentationWithoutSpaces);
                segment.setMeterRepresentationForUser(newMeterRepresentationWithoutSpaces);
                segment.getMeterRepresentations().clear();
                segment.getMeterRepresentations().add(newMeterRepresentationWithoutSpaces);
                segment.setMeterRepresentationWithSpaces(newMeterRepresentation);
            }
        }

        //and at least refine
        if (numberPortion >= 0) {

            ProsePortionForRythm instance = (ProsePortionForRythm) AnalyserPortionOfText.getListOfInstance().get(numberPortion - 1);
            HeaderAnFooterListsForWebOutput.getPortionHeaders().set(numberPortion - 1, instance.formHeaderLines(numberPortion));
            //there is no footer in web-verse (we have there the table)
            //PortionsListsForWebOutput.getPortionFooters().set(numberPortion-1, instance.formFooterLinesWithoutWeb());
        }
    }

    //==getter ==
    public String getOriginalText() {
        return originalText;
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

    /**
     * parse portion of text to the table with segments, words, stress schema and so on
     * @return
     */
    @Override
    public DataTable parsePortionOfText() {

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

        DataTable prepareTable = new DataTable(namesOfColumns, sup);

        int NumberOfSentence = 1;
        int NumberOfWord = 1;
        //StringBuilder sbText = new StringBuilder(pText);
        //String fragment = "";
        String sentence;
        String word;
        int posSymbol;
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
     *
     * @return list with header's lines
     */
    public List<String> formHeaderLines(int nPortion) {
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
    public List<String> formFooterLines() {
        List<String> footerLines = new ArrayList<>();
        footerLines.add("\n");
        return footerLines;
    }

    /**
     * resume output web
     *
     */
    public void prepareResumeOutputWeb() {
        HeaderAnFooterListsForWebOutput.getPortionHeaders().add(formHeaderLines(getNumberOfPortion()));
        HeaderAnFooterListsForWebOutput.getPortionFooters().add(formFooterLines());
    }

    /**
     * resume output without web (console)
     *
     * @param outputAccumulation StringBuilder where output stores
     * @param commonConstants common app constants
     */
    public void resumeOutputConsole(StringBuilder outputAccumulation, CommonConstants commonConstants) {

        List<String> headerLines = formHeaderLines(getNumberOfPortion());
        for (String line : headerLines) {
            outputAccumulation.append(line);
        }

        DataTable dt = this.getDtOfTextSegmentsAndStresses();
        List<SegmentOfPortion> listSegments = this.getListOfSegments();
        String nameOfFirstColumn = (String) dt.getNamesOfColumn().toArray()[1];

        for (SegmentOfPortion listSegment : listSegments) {
            Integer nSegment = listSegment.getSegmentIdentifier();
            outputAccumulation.append("Sentence #").append(nSegment).append("\n");
            List<String> words = (List<String>) dt.getValueFromColumnAndRowByCondition("Word", nameOfFirstColumn, nSegment);
            String line = words.stream().map(s -> s + " ").reduce("", String::concat).trim();
            String meterRepresentationWithSpaces = listSegment.getMeterRepresentationWithSpaces().trim();
            outputAccumulation.append(line).append("\n");
            //meterRepresentationOfPortion.add(listSegments.get(i).getSelectedMeterRepresentation());//all portion
            outputAccumulation.append("[").append(meterRepresentationWithSpaces).append("]\n");
        }
        outputAccumulation.append("==========================\n");
        List<String> footerLines = formFooterLines();
        for (String line : footerLines) {
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
     * @param outputAccumulation StringBuilder where output stores
     * @param commonConstants common app constants
     */
    @Override
    public void resumeOutput(StringBuilder outputAccumulation, CommonConstants commonConstants) {
        if (!commonConstants.isThisIsWebApp()) {
            resumeOutputConsole(outputAccumulation, commonConstants);
        } else prepareResumeOutputWeb();
    }


    //== public instance method ==

    /**
     * fill each segment with rythm schema
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

