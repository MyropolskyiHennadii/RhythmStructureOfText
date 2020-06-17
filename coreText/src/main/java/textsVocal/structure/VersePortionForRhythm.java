package textsVocal.structure;

import textsVocal.config.CommonConstants;
import textsVocal.config.HeaderAndFooterListsForWebOutput;
import textsVocal.utilsCommon.DataTable;
import textsVocal.utilsCommon.FileTreatment;
import textsVocal.utilsCore.ErrorsInterractionWithWebUser;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class VersePortionForRhythm extends TextPortionForRhythm {

    //== fields ========================================================
    private String pText;//formatted text
    private String originalText;//original text
    private String mainMeter;//meter of whole text
    private String regularEndingsOfFirstStrophe;//ending's formula
    private String regularDurationOfFirstStrophe;//duration's formula
    private String regularNumberOfStressOfFirstStrophe;//number's of stress formula
    private String regularSpaceOnSyllable;//ceasure
    private double maxDuration = 0;//maximal duration in lines
    private double minDuration = Integer.MAX_VALUE;////minimal duration in lines
    private double averageDuration = 0;//average duration in lines

    // == constructor ==============================================================
    public VersePortionForRhythm() {
    }

    public VersePortionForRhythm(String pText) {
        this.pText = prepareStringForParsing(pText, SYMB_BRIEF_PUNCTUATION, SYMB_SPACE).toString();
        this.originalText = pText;
    }

    //== static methods ==

    /**
     * identify rated verse meter with patterns - enum verseMeterPatterns/ Without pentons
     *
     * @param s - stress schema (like 01010101...)
     * @return map with predefined keys and calculated values
     */
    public static Map<String, String> WhatIsTheMettersPatternForStringWithoutPentons(String s) {

        Map<String, String> descriptionMeter = new HashMap<>();
        //undefined content (we have not found pattern):
        descriptionMeter.put("meter", "Unknown");
        descriptionMeter.put("nTonicFoot", "0");
        descriptionMeter.put("nShiftRegularMeterOnSyllable", "0");

        //cut end of string
        String sWithoutEnding = s.trim();
        while (sWithoutEnding.endsWith("" + symbolOfNoStress) || sWithoutEnding.length() == 0) {
            sWithoutEnding = sWithoutEnding.substring(0, sWithoutEnding.length() - 1);
        }

        //if s - something like "00000"
        if (sWithoutEnding.isEmpty()) {
            return descriptionMeter;
        }
        //if s contains undefined (not 0 ot 1) sign "?"
        if (sWithoutEnding.contains("?")) {
            return descriptionMeter;
        }
        //if s contains spondee ("...11...")
        if (sWithoutEnding.contains("" + symbolOfStress + symbolOfStress)) {
            return descriptionMeter;
        }

        //only if s has non-empty and well-defined content we are trying to find pattern
        int minLength;
        int maxLength;
        int nShiftRegularMeterOnSyllable;
        int nTonicFoot;
        int nMistake;
        int nShift;

        String strPattern;

        for (verseMeterPatterns pattern : verseMeterPatterns.values()) {

            strPattern = pattern.getPattern().trim();

            //pass pentons: they stay alone
            if (strPattern.length() == 5) {
                break;
            }
            nShift = strPattern.length() - 1;

            minLength = Math.min(sWithoutEnding.length(), pattern.getDuration());
            if (minLength > 2 && (strPattern.charAt(0) == symbolOfStress || strPattern.charAt(1) == symbolOfStress)) {
                minLength = 2;//we are checking beginning Dactil and Amphibrach only for first 2 syllable
            }

            //pass off pentons with 2 foots (we consider pentons min as 3 foots
            if (strPattern.length() >= 5 && sWithoutEnding.length() / strPattern.length() <= 2) {
                continue;
            }

            //if doesn't match beginning
            boolean doesntMatchBeginning = false;
            for (int i = 0; i < minLength; i++) {
                if (sWithoutEnding.charAt(i) == symbolOfStress) {
                    if (strPattern.charAt(i) != sWithoutEnding.charAt(i)) {
                        doesntMatchBeginning = true;
                    }
                }
            }
            if (doesntMatchBeginning) {
                continue;
            }

            maxLength = Math.max(sWithoutEnding.length(), pattern.getDuration());

            for (int iShift = 1; iShift <= nShift; iShift++) {
                if (!descriptionMeter.get("meter").equals("Unknown") && descriptionMeter.get("nShiftRegularMeterOnSyllable").equals("0")) {
                    continue;
                }
                nShiftRegularMeterOnSyllable = cycleByPatternWhatIsTheMettersPatternForString(sWithoutEnding, pattern,
                        maxLength, minLength, iShift);

                if (nShiftRegularMeterOnSyllable > 999998) {
                    nMistake = nShiftRegularMeterOnSyllable;
                } else {
                    if (nShiftRegularMeterOnSyllable > 0) {
                        nMistake = 1;
                    } else {
                        nMistake = 0;
                    }
                }

                if (nMistake <= 1) {
                    //We allow write data to map, if it is the first try or the last try without mistakes
                    if (nMistake == 0 || descriptionMeter.get("meter").equals("Unknown")) {
                        descriptionMeter.put("meter", pattern.name());

                        nTonicFoot = SegmentOfPortion.getNumberOfSegmentStress(sWithoutEnding);
                        int footCorrection = sWithoutEnding.length() % strPattern.length();

                        if (footCorrection == 0) {
                            if (sWithoutEnding.length() / strPattern.length() > nTonicFoot) {
                                nTonicFoot = sWithoutEnding.length() / strPattern.length();
                            }
                        } else {
                            int iTemp = (strPattern.charAt(footCorrection - 1) == symbolOfStress ? 1 : 0) + sWithoutEnding.length() / strPattern.length();
                            if (iTemp > nTonicFoot) {
                                nTonicFoot = iTemp;
                            } else {
                                //How to define in other way???
                                if (strPattern.length() == 3 && sWithoutEnding.contains("000000")) {
                                    nTonicFoot = Math.max(1 + sWithoutEnding.length() / strPattern.length(), nTonicFoot);
                                }
                            }

                        }
                        descriptionMeter.put("nTonicFoot", "" + nTonicFoot);
                        descriptionMeter.put("nShiftRegularMeterOnSyllable", "" + nShiftRegularMeterOnSyllable);
                    }
                }
            }
        }
        return descriptionMeter;
    }

    /**
     * identify rated verse meter with patterns - enum verseMeterPatterns/ Only pentons
     *
     * @param s - stress schema (like 01010101...)
     * @return map with predefined keys and calculated values
     */
    public static Map<String, String> WhatIsTheMettersPatternForStringPentons(String s) {
        Map<String, String> descriptionMeter = new HashMap<>();
        //undefined content (we have not found pattern):
        descriptionMeter.put("meter", "Unknown");
        descriptionMeter.put("nTonicFoot", "0");
        descriptionMeter.put("nShiftRegularMeterOnSyllable", "0");

        //cut end of string
        String sWithoutEnding = s.trim();
        while (sWithoutEnding.endsWith("" + symbolOfNoStress) || sWithoutEnding.length() == 0) {
            sWithoutEnding = sWithoutEnding.substring(0, sWithoutEnding.length() - 1);
        }

        if (sWithoutEnding.length() <= 10) {
            return descriptionMeter;
        }
        for (verseMeterPatterns pattern : verseMeterPatterns.values()) {

            String strPattern = pattern.getPattern().trim();

            //pass no-pentons: they stay alone
            if (strPattern.length() < 5) {
                continue;
            }
            int positionStress = strPattern.indexOf("" + symbolOfStress);

            boolean wasDefined = true;
            int iTemp = 1 + sWithoutEnding.length() / strPattern.length();
            for (int i = 0; i < iTemp; i++) {

                if ((i * 5 + positionStress) > sWithoutEnding.length() - 1) {
                    break;
                }
                if (sWithoutEnding.charAt((i * 5 + positionStress)) != symbolOfStress) {
                    wasDefined = false;
                    break;
                }
            }

            if (wasDefined) {
                descriptionMeter.put("meter", pattern.name());
                descriptionMeter.put("nTonicFoot", "" + iTemp);
                descriptionMeter.put("nShiftRegularMeterOnSyllable", "" + 0);
                return descriptionMeter;
            }

        }
        return descriptionMeter;
    }

    /**
     * service function for rating meter by pattern (nested in WhatIsTheMettersPatternForStringWithoutPentons)
     *
     * @param sWithoutEnding string without ending
     * @param pattern        meter pattern
     * @param maxLength      maximal length
     * @param minLength      minimal length
     * @param nShift         shifting pattern meter on
     * @return number of mistakes or shifting regular meter on syllable N...
     */
    private static int cycleByPatternWhatIsTheMettersPatternForString(String sWithoutEnding, verseMeterPatterns pattern,
                                                                      int maxLength, int minLength, int nShift) {

        int nMistake = 0;
        int nShiftRegularMeterOnSyllable = 0;
        String strPattern = pattern.getPattern().trim();

        for (int i = 0; i < maxLength; i++) {

            if ((i < minLength) || (sWithoutEnding.length() == maxLength)) {
                if (sWithoutEnding.charAt(i) == symbolOfNoStress)//nothing to check
                {
                    continue;
                }
            }
            if ((i >= minLength) && (sWithoutEnding.length() == minLength)) {
                continue;
            }

            //here sWithoutEnding.charAt(i) always == '1'
            int iPattern = i;
            if (i >= strPattern.length()) {

                if (nShift < 2) {
                    if ((i % pattern.getDuration() + nMistake) >= pattern.getDuration()) {
                        iPattern = (i % pattern.getDuration() + nMistake) % pattern.getDuration();
                    } else {
                        iPattern = i % pattern.getDuration() + nMistake;
                    }
                } else {
                    if ((i % pattern.getDuration() + (nMistake > 0 ? 1 : 0) * nShift) >= pattern.getDuration()) {
                        iPattern = (i % pattern.getDuration() + (nMistake > 0 ? 1 : 0) * nShift) % pattern.getDuration();
                    } else {
                        iPattern = i % pattern.getDuration() + (nMistake > 0 ? 1 : 0) * nShift;
                    }
                }

            }

            if (strPattern.charAt(iPattern) == symbolOfNoStress) //defined content (we have not found pattern):
            {
                nMistake++;
                nShiftRegularMeterOnSyllable = i + 1;//+1 because i begins from 0
            }

            if (nMistake > 1) {
                // nothing to do. Too much mistakes
                return 999999;
            }

        }
        return nShiftRegularMeterOnSyllable;
    }

    /**
     * input changes/corrections in portion's characteristics from web-user
     *
     * @param checkedItems  static list with checked items (web module - ChangedValuesInHTMLTable class)
     * @param changedValues static list with checked values (web module - ChangedValuesInHTMLTable class)
     */
    public static void makeCorrectionInVerseCharacteristicFromWebUser(List<String> checkedItems, List<String> changedValues) {

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

                //if it was a mistake in new representation
                boolean itHappensMistake = false;
                for (int j = 0; j < newMeterRepresentation.length(); j++) {
                    if ((!("" + newMeterRepresentation.charAt(j)).equals("" + symbolOfStress) && !("" + newMeterRepresentation.charAt(j)).equals("" + symbolOfNoStress))) {
                        itHappensMistake = true;
                        for (CharSequence c : SYMB_SPACE) {
                            if (("" + c).equals("" + newMeterRepresentation.charAt(j))) {
                                itHappensMistake = false;
                            }
                        }
                    }
                }

                if (itHappensMistake) {
                    ErrorsInterractionWithWebUser.errors.add("Impossible meter schema from user "
                            + newMeterRepresentation + ". Portion #" + (numberPortion + 1)
                            + ", segment = " + (numberSegment + 1));
                    continue;
                }

                //we'll clean and fill all values for this segment

                //code from VocalAnalysisSegmentRu
                Set<String> setMeters = new HashSet<>();//set of represantations

                List<SegmentOfPortion> listSegments = AnalyserPortionOfText.getListOfInstance().get(numberPortion - 1).getListOfSegments();
                SegmentOfPortion segment = listSegments.get(numberSegment - 1);
                segment.setSelectedMeterRepresentation(newMeterRepresentation);
                segment.setMeterRepresentationForUser(newMeterRepresentation);
                segment.setDuration(newMeterRepresentation);
                segment.setNumberSyllable(newMeterRepresentation.length());
                segment.setEndingByRepresentation(newMeterRepresentation);

                Map<String, String> descriptionMeter = VersePortionForRhythm.WhatIsTheMettersPatternForStringWithoutPentons(newMeterRepresentation);
                DataTable dtSegment = segment.getTableOfMeterDefinitions();
                dtSegment.clearDynamicTable();

                if (!"Unknown".equals(descriptionMeter.get("meter").trim()) || newMeterRepresentation.length() <= 10) {//without pentons
                    String repr = newMeterRepresentation + ";" + descriptionMeter.get("meter").trim() + "-" + descriptionMeter.get("nTonicFoot").trim() + ";" + descriptionMeter.get("nShiftRegularMeterOnSyllable").trim();

                    if (!setMeters.contains(repr)) {//without duplicates
                        dtSegment.setRow(Stream.of(newMeterRepresentation, descriptionMeter.get("meter").trim(), Integer.valueOf(descriptionMeter.get("nTonicFoot").trim()), Integer.valueOf(descriptionMeter.get("nShiftRegularMeterOnSyllable").trim())).collect(Collectors.toList()));
                        segment.setMeter(descriptionMeter.get("meter").trim());
                        segment.setNumberOfTonicFoot(Integer.valueOf(descriptionMeter.get("nTonicFoot").trim()));
                        segment.setShiftRegularMeterOnSyllable(Integer.valueOf(descriptionMeter.get("nShiftRegularMeterOnSyllable").trim()));
                        setMeters.add(repr);
                    }
                } else {//pentons
                    descriptionMeter = VersePortionForRhythm.WhatIsTheMettersPatternForStringPentons(newMeterRepresentation);
                    String repr = newMeterRepresentation + ";" + descriptionMeter.get("meter").trim() + "-" + descriptionMeter.get("nTonicFoot").trim() + ";" + descriptionMeter.get("nShiftRegularMeterOnSyllable").trim();

                    if (!setMeters.contains(repr)) {//without duplicates
                        dtSegment.setRow(Stream.of(newMeterRepresentation, descriptionMeter.get("meter").trim(), Integer.valueOf(descriptionMeter.get("nTonicFoot").trim()), Integer.valueOf(descriptionMeter.get("nShiftRegularMeterOnSyllable").trim())).collect(Collectors.toList()));
                        setMeters.add(repr);
                    }
                }
                //if there is no row in dtSegment, that means: no word was recognized
                //Probably it was the line with foreign language
                if (dtSegment.getSize() == 0) {
                    dtSegment.setRow(Stream.of("0", "Unknown. Probably unknown language", 0, 0).collect(Collectors.toList()));
                    segment.setMeter("" + symbolOfNoStress);
                    segment.setSelectedMeterRepresentation("" + symbolOfNoStress);
                }
            }
        }

        //and at least refine
        if (numberPortion >= 0) {

            VersePortionForRhythm instance = (VersePortionForRhythm) AnalyserPortionOfText.getListOfInstance().get(numberPortion - 1);
            AnalyserPortionOfText.refineVerseCharacteristics(instance);

            //refine header
            HeaderAndFooterListsForWebOutput.getPortionHeaders().set(numberPortion - 1, instance.formHeaderLines(numberPortion));

            //refine footer
            List<String> footer = new ArrayList<>();
            instance.addFooterLinesWithAverageValues(footer);
            HeaderAndFooterListsForWebOutput.getPortionFooters().set(numberPortion-1, footer);
        }
    }

    /**
     * service function: sorting map by value
     *
     * @param unsortMap unsorted map
     * @param order     order of sorting
     * @return sorted map
     */
    private static Map<String, Integer> sortMapByComparator(Map<String, Integer> unsortMap, final boolean order) {

        List<Map.Entry<String, Integer>> list = new LinkedList<>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2) {
                if (order) {
                    return o1.getValue().compareTo(o2.getValue());
                } else {
                    return o2.getValue().compareTo(o1.getValue());

                }
            }
        });

        // Maintaining insertion order with the help of LinkedList
        Map<String, Integer> sortedMap = new LinkedHashMap<>();
        list.stream().forEach((entry) -> {
            sortedMap.put(entry.getKey(), entry.getValue());
        });

        return sortedMap;
    }

    /**
     * forms output lines
     *
     * @param out       StringBuilder with output
     * @param outputArr array we have appent to StringBuilder
     */
    public static void outputLineInResume(StringBuilder out, String[] outputArr) {
        int[] lengthInSymbols = new int[5];//length of columns in symbols
        lengthInSymbols[0] = 48;
        lengthInSymbols[1] = 24;
        lengthInSymbols[2] = 24;
        lengthInSymbols[3] = 24;
        lengthInSymbols[4] = 24;

        if (outputArr.length != lengthInSymbols.length) {
            getLog().error("Non-equal arrays!");
            throw new IllegalArgumentException();
        }

        for (int i = 0; i < lengthInSymbols.length; i++) {
            StringBuilder s = new StringBuilder(outputArr[i]);
            int l = lengthInSymbols[i];
            if (s.length() > l) {
                s = new StringBuilder(s.substring(0, l - 1));
            }
            if (s.length() < l) {
                while (s.length() < l) {
                    s.append(" ");
                }
            }
            out.append("| ").append(s);
        }
        out.append("\n");
    }
    //== setters and getters ==========================================

    /**
     * reset fields by initialisation
     */
    @Override
    public void reset(String pText) {
        this.mainMeter = "";
        this.regularEndingsOfFirstStrophe = "";
        this.regularDurationOfFirstStrophe = "";
        this.regularNumberOfStressOfFirstStrophe = "";
        this.regularSpaceOnSyllable = "";
        this.maxDuration = 0;
        this.averageDuration = 0;
        this.minDuration = Integer.MAX_VALUE;
        this.pText = prepareStringForParsing(pText, SYMB_BRIEF_PUNCTUATION, SYMB_SPACE).toString();
        this.originalText = pText;
    }

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    public String getMainMeter() {
        return mainMeter;
    }

    public void setMainMeter(String mainMeter) {
        this.mainMeter = mainMeter;
    }

    public String getRegularEndingsOfFirstStrophe() {
        return regularEndingsOfFirstStrophe;
    }

    public void setRegularEndingsOfFirstStrophe(String regularEndingsOfFirstStrophe) {
        this.regularEndingsOfFirstStrophe = regularEndingsOfFirstStrophe;
    }

    public String getRegularDurationOfFirstStrophe() {
        return regularDurationOfFirstStrophe;
    }

    public void setRegularDurationOfFirstStrophe(String regularDurationOfFirstStrophe) {
        this.regularDurationOfFirstStrophe = regularDurationOfFirstStrophe;
    }

    public String getRegularNumberOfStressOfFirstStrophe() {
        return regularNumberOfStressOfFirstStrophe;
    }

    public void setRegularNumberOfStressOfFirstStrophe(String regularNumberOfStressOfFirstStrophe) {
        this.regularNumberOfStressOfFirstStrophe = regularNumberOfStressOfFirstStrophe;
    }

    public String getRegularSpaceOnSyllable() {
        return regularSpaceOnSyllable;
    }

    public void setRegularSpaceOnSyllable(String regularSpaceOnSyllable) {
        this.regularSpaceOnSyllable = regularSpaceOnSyllable;
    }

    public double getMaxDuration() {
        return maxDuration;
    }

    public void setMaxDuration(double maxDuration) {
        this.maxDuration = maxDuration;
    }

    public double getMinDuration() {
        return minDuration;
    }

    public void setMinDuration(double minDuration) {
        this.minDuration = minDuration;
    }

    public double getAverageDuration() {
        return averageDuration;
    }

    public void setAverageDuration(double averageDuration) {
        this.averageDuration = averageDuration;
    }

    @Override
    public String getpText() {
        return pText;
    }

    public void setpText(String pText) {
        this.pText = pText;
    }

    /**
     * parse portion of text to the table with segments, words, stress schema and so on
     *
     * @return
     */
    @Override
    public DataTable parsePortionOfText() {

        // names of columns
        List<String> namesOfColumns = new ArrayList<>();
        namesOfColumns.add("Number of line");
        namesOfColumns.add("Line");
        namesOfColumns.add("Number of word");
        namesOfColumns.add("Word");
        namesOfColumns.add("Word-object / Stress form");

        // supliers for lists with data
        List<Supplier<?>> sup = new ArrayList<>();
        sup.add(ArrayList<Integer>::new);
        sup.add(ArrayList<String>::new);
        sup.add(ArrayList<Integer>::new);
        sup.add(ArrayList<String>::new);
        sup.add(ArrayList<Word>::new);

        DataTable prepareTable = new DataTable(namesOfColumns, sup);

        int NumberOfFragment = 1;
        int NumberOfWord = 1;
        StringBuilder sbText = new StringBuilder(pText);
        String fragment;
        String word;
        int posSymbol;
        List<Object> addData = new ArrayList<>();

        //for cleaning string from unusefull paragraph symbol
        List<CharSequence[]> allCharsParagraph = new ArrayList<>();
        allCharsParagraph.add(SYMB_PARAGRAPH);

        do {
            fragment = textFragmentToDelimiter(sbText, SYMB_PARAGRAPH);
            StringBuilder fragmentToWords = (fragment == null ? new StringBuilder(sbText.toString()) : new StringBuilder(fragment));

            //cleaning from unusefull paragraph symbols in empty lines
            if (fragment != null) {
                fragment = cleanTextFromSymbols(fragment, allCharsParagraph);
                if (fragment.isEmpty()) {
                    continue;
                }
            }

            do {
                word = textFragmentToDelimiter(fragmentToWords, SYMB_SPACE);
                StringBuilder wordToTable = (word == null ? fragmentToWords : new StringBuilder(word));

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
                addData.add(NumberOfFragment);
                addData.add((fragment == null ? sbText.toString().trim() : fragment.trim()));
                addData.add(NumberOfWord);
                addData.add(wordToTable.toString().trim());
                addData.add(new Word());

                if (!prepareTable.setRow(addData)) {
                    break;
                }
                NumberOfWord++;

            } while (word != null);

            NumberOfFragment++;

        } while (fragment != null);

        setDtOfTextSegmentsAndStresses(prepareTable);
        return prepareTable;
    }

    @Override
    public <T> void fillPortionWithCommonRythmCharacteristics(T t) {
        fillVersePortionWithCommonRythmCharacteristics((Map) t);
    }

    /**
     * What sre the most popular meters in portion
     */
    public Map<String, Integer> WhatAreMostPolularMetersInMultiplePossibleValues() {

        TreeMap<String, Integer> treeGroupOfMeters = new TreeMap<>();
        List<SegmentOfPortion> preparedListOfSegment = getListOfSegments();

        if (preparedListOfSegment.isEmpty()) {
            getLog().error("There is no segment in list. Impossible to define meter in segment.");
            throw new IllegalArgumentException("There is no segment in list. Impossible to define meter in segment.");
        }

        // prioritize
        // compose map with "rate" (frequency) meters in segments
        preparedListOfSegment.stream().map(SegmentOfPortion::getTableOfMeterDefinitions).map((dt) -> {
            // temporary set
            Set<String> tempSet = new HashSet<>();
            for (int i = 0; i < dt.getSize(); i++) {
                tempSet.add(dt.getValueFromColumnAndRow("Meter", i).toString().trim());
            }
            return tempSet;
        }).forEach((tempSet) -> tempSet.stream().forEach((groupMeter) -> {
            if (treeGroupOfMeters.containsKey(groupMeter)) {
                treeGroupOfMeters.put(groupMeter, treeGroupOfMeters.get(groupMeter) + 1);
            } else {
                treeGroupOfMeters.put(groupMeter, 1);
            }
        }));

        //now we have to sort the map by frequency (values in map)
        return sortMapByComparator(treeGroupOfMeters, false);
    }

    /**
     * fill portion with common rythm characteristics
     */
    public void fillVersePortionWithCommonRythmCharacteristics(Map priorityMap) {

        CommonConstants constants = CommonConstants.getApplicationContext().getBean(CommonConstants.class);
        List<SegmentOfPortion> preparedListOfSegment = getListOfSegments();
        if (preparedListOfSegment.isEmpty()) {
            getLog().error("There is empty list of segments. Impossible to define meter in portion.");
            throw new IllegalArgumentException("There is empty list of segments. Impossible to define meter in portion.");
        }
        if (priorityMap == null) {
            getLog().error("Null instead of priority map.");
            throw new IllegalArgumentException("Null instead of priority map.");
        }
        if (priorityMap.isEmpty()) {
            getLog().error("Empty priority map.");
            throw new IllegalArgumentException("Empty priority map.");
        }

        String mainGroupName = ((String) (priorityMap.keySet().stream().findFirst().get())).trim();
        int mainPart = 100 * (Integer) (priorityMap.get(mainGroupName)) / preparedListOfSegment.size();

        int secondPart = 0;
        String secondGroupName = "-------";
        Optional<String> secondGroup = priorityMap.keySet().stream().skip(1).findFirst();
        if (secondGroup.isPresent()) {
            secondPart = 100 * (Integer) (priorityMap.get(secondGroup.get())) / preparedListOfSegment.size();
            secondGroupName = (String) priorityMap.keySet().stream().skip(1).findFirst().get();
        }

        if ((mainPart >= constants.getValidLevelOfMainMeterGroupInVerseText()) && (mainPart - secondPart) >= constants.getValidDifferenceBetweenTwoMainGroupsInVerseText()
                && !mainGroupName.equals("Unknown")) {
            this.setMainMeter(mainGroupName);
        } else if ((mainPart + secondPart) >= constants.getValidLevelOfMainMeterGroupInVerseText()) {
            this.setMainMeter("Mixed or free verse");
        } else {
            this.setMainMeter("Unknown");
        }

        for (SegmentOfPortion s : preparedListOfSegment) {
            s.fillVerseSegmentWithMeterCharacteristics(mainGroupName, secondGroupName, mainPart, secondPart);
        }

    }

    /**
     * check regularity endings, durations, so on
     *
     * @param arr array with stress representations
     * @return is there any regularity or no
     */
    private boolean checkRegularity(String[] arr) {
        boolean regularity = false;

        if (arr.length == 4) {
            if (arr[0].equals(arr[1]) && arr[2].equals(arr[3])
                    || arr[0].equals(arr[2]) && arr[1].equals(arr[3])
                    || arr[0].equals(arr[3]) && arr[1].equals(arr[2])) {
                return true;
            }
        }

        if (arr.length >= 8) {
            if ((arr[0].equals(arr[1]) && arr[2].equals(arr[3])
                    || arr[0].equals(arr[2]) && arr[1].equals(arr[3])
                    || arr[0].equals(arr[3]) && arr[1].equals(arr[2]))
                    && (arr[4].equals(arr[5]) && arr[6].equals(arr[7])
                    || arr[4].equals(arr[6]) && arr[5].equals(arr[7])
                    || arr[4].equals(arr[7]) && arr[5].equals(arr[6]))) {
                return true;
            }
        }

        if (arr.length >= 6) {
            if ((arr[0].equals(arr[2]) && arr[2].equals(arr[4])
                    || arr[1].equals(arr[3]) && arr[3].equals(arr[5])
                    || arr[0].equals(arr[3]) && arr[1].equals(arr[4]) && arr[2].equals(arr[5]))
                    || (arr[0].equals(arr[1]) && arr[3].equals(arr[4]))) {
                return true;
            }
        }

        if (arr.length >= 5) {
            if (arr[0].equals(arr[2]) && arr[0].equals(arr[4]) && arr[1].equals(arr[3])
                    || arr[0].equals(arr[1]) && arr[0].equals(arr[2]) && arr[3].equals(arr[4])
                    || arr[0].equals(arr[1]) && arr[2].equals(arr[3]) && arr[3].equals(arr[4])
                    || arr[0].equals(arr[4]) && arr[1].equals(arr[2]) && arr[2].equals(arr[3])) {
                return true;
            }
        }

        return regularity;
    }

    /**
     * is there regular endings
     */
    public void IsThereRegularEndings() {

        List<SegmentOfPortion> listSegment = this.getListOfSegments();
        int linesCount = listSegment.size();
        String[] array = new String[1];

        if (linesCount == 4) {
            array = new String[4];
        }
        if (linesCount == 5) {
            array = new String[5];
        }
        if (linesCount == 6) {
            array = new String[6];
        }
        if (linesCount == 7) {
            array = new String[7];
        }
        if (linesCount == 8) {
            array = new String[8];
        }
        if (linesCount == 9) {
            array = new String[9];
        }
        if (linesCount >= 10) {
            array = new String[10];
        }
        for (int i = 0; i < array.length; i++) {
            array[i] = listSegment.get(i).getEnding();
        }

        if (checkRegularity(array)) {
            int iMin = array.length > 5 ? 6 : array.length;
            StringBuilder reg = new StringBuilder();
            for (int i = 0; i < iMin; i++) {
                reg.append(listSegment.get(i).getEnding()).append("|");
            }
            setRegularEndingsOfFirstStrophe(reg.toString());
        }
    }

    /**
     * is there regular duration
     */
    public void IsThereRegularDuration() {

        List<SegmentOfPortion> listSegment = this.getListOfSegments();
        int linesCount = listSegment.size();
        String[] array = new String[1];

        if (linesCount == 4) {
            array = new String[4];
        }
        if (linesCount == 5) {
            array = new String[5];
        }
        if (linesCount == 6) {
            array = new String[6];
        }
        if (linesCount == 7) {
            array = new String[7];
        }
        if (linesCount == 8) {
            array = new String[8];
        }
        if (linesCount == 9) {
            array = new String[9];
        }
        if (linesCount >= 10) {
            array = new String[10];
        }
        for (int i = 0; i < array.length; i++) {
            array[i] = "" + (int) listSegment.get(i).getDuration();
        }

        if (checkRegularity(array)) {
            int iMin = array.length > 5 ? 6 : array.length;
            StringBuilder reg = new StringBuilder();
            for (int i = 0; i < iMin; i++) {
                reg.append((int) listSegment.get(i).getDuration()).append("|");
            }
            setRegularDurationOfFirstStrophe(reg.toString());
        }

    }

    /**
     * is there regular number of stress
     */
    public void IsThereRegularNumberOfStress() {
        List<SegmentOfPortion> listSegment = this.getListOfSegments();
        int linesCount = listSegment.size();
        String[] array = new String[1];

        if (linesCount == 4) {
            array = new String[4];
        }
        if (linesCount == 5) {
            array = new String[5];
        }
        if (linesCount == 6) {
            array = new String[6];
        }
        if (linesCount == 7) {
            array = new String[7];
        }
        if (linesCount == 8) {
            array = new String[8];
        }
        if (linesCount == 9) {
            array = new String[9];
        }
        if (linesCount >= 10) {
            array = new String[10];
        }
        for (int i = 0; i < array.length; i++) {
            array[i] = "" + SegmentOfPortion.getNumberOfSegmentStress(listSegment.get(i).getSelectedMeterRepresentation());
        }

        if (checkRegularity(array)) {
            int iMin = array.length > 5 ? 6 : array.length;
            StringBuilder reg = new StringBuilder();
            for (int i = 0; i < iMin; i++) {
                reg.append(SegmentOfPortion.getNumberOfSegmentStress(listSegment.get(i).getSelectedMeterRepresentation())).append("|");
            }
            setRegularNumberOfStressOfFirstStrophe(reg.toString());
        }
    }

    /**
     * is there regular ceasure
     */
    public void IsThereRegularCaesura() {
        List<SegmentOfPortion> listSegment = this.getListOfSegments();
        int[] caesura = new int[12];//considerate for ceasure max 12 syllables
        int lineCount = Math.min(listSegment.size(), 10);


        for (int i = 0; i < lineCount; i++) {
            List<Integer> potentialCaesura = listSegment.get(i).getSchemaOfSpaces();
            int countSyllable = listSegment.get(i).getNumberSyllable();
            for (int j = 0; j < potentialCaesura.size(); j++) {
                if (j > 12 || potentialCaesura.get(j) > 12 || potentialCaesura.get(j) < 1
                        || potentialCaesura.get(j) > countSyllable - 1) {
                    continue;
                }
                caesura[potentialCaesura.get(j) - 1]++;
            }
        }
        StringBuilder reg = new StringBuilder();
        for (int i = 0; i < caesura.length; i++) {
            if (100 * caesura[i] / lineCount > 50) {
                reg.append(i + 1).append(" (").append(100 * caesura[i] / lineCount).append("%) | ");
            }
        }
        reg = new StringBuilder((reg.length() == 0) ? "Not regular" : reg.toString());
        setRegularSpaceOnSyllable(reg.toString());
    }

    /**
     * set maximal and minimal duration for all over the text
     */
    public void setMaxAndMinDuration() {

        List<SegmentOfPortion> listSegments = getListOfSegments();
        double max = 0;
        double min = Integer.MAX_VALUE;
        double sum = 0;
        for (SegmentOfPortion listSegment : listSegments) {
            double duration = listSegment.getDuration();
            sum += duration;
            if (listSegment.getDuration() > max) {
                max = duration;
            }
            if (listSegment.getDuration() < min) {
                min = duration;
            }
        }
        setMinDuration(min);
        setMaxDuration(max);
        if(listSegments.size()>0){
            setAverageDuration(sum/listSegments.size());
        }
    }

    /**
     * finishing: common portion characteristics
     */
    public void finalDefinitionOfPortionsMeter() {

        List<SegmentOfPortion> listSegment = this.getListOfSegments();

        //we have ground to take portion as verse
        boolean thatIsAccentVerse = false;
        boolean thatIsTaktovikVerse = false;

        int maxNumberOfStress = 0;

        for (SegmentOfPortion segmentOfPortion : listSegment) {

            // main meter definition
            if (segmentOfPortion.getSelectedMeterRepresentation().contains("0000")
                    || segmentOfPortion.getSelectedMeterRepresentation().contains("00000")) {
                thatIsAccentVerse = true;
            }
            if (segmentOfPortion.getSelectedMeterRepresentation().contains("000")) {
                thatIsTaktovikVerse = true;
            }
            if (SegmentOfPortion.getNumberOfSegmentStress(segmentOfPortion.getSelectedMeterRepresentation()) > maxNumberOfStress) {
                maxNumberOfStress = SegmentOfPortion.getNumberOfSegmentStress(segmentOfPortion.getSelectedMeterRepresentation());
            }
        }

        if (thatIsAccentVerse) {
            this.setMainMeter("Accent verse. Max. " + maxNumberOfStress + " stresses.");
        } else if (thatIsTaktovikVerse) {
            this.setMainMeter("Taktovik verse. Max. " + maxNumberOfStress + " stresses.");
        } else {
            this.setMainMeter("Dolnik verse. Max. " + maxNumberOfStress + " stresses.");
        }
    }

    /**
     * form header for output
     *
     * @param nPortion number of portion
     * @return list with header lines for output
     */
    public List<String> formHeaderLines(int nPortion) {
        ResourceBundle messages = CommonConstants.getResourceBundle();
        List<String> headerLines = new ArrayList<>();
        headerLines.add("\n");
        headerLines.add(messages.getString("portionNumber") + nPortion + "\n");
        headerLines.add(messages.getString("mainMeter") + this.getMainMeter() + "\n");
        headerLines.add(messages.getString("maxDuration") + (int) this.getMaxDuration() + "\n");
        headerLines.add(messages.getString("minDuration") + (int) this.getMinDuration() + "\n");
        headerLines.add(messages.getString("endingsFirstLines") + this.getRegularEndingsOfFirstStrophe() + "\n");
        headerLines.add(messages.getString("durationsFirstLines") + this.getRegularDurationOfFirstStrophe() + "\n");
        headerLines.add(messages.getString("numberStressesFirstLines") + this.getRegularNumberOfStressOfFirstStrophe() + "\n");
        headerLines.add(messages.getString("regularSpacesFirstLines") + this.getRegularSpaceOnSyllable() + "\n");
        headerLines.add("==========================\n");
        return headerLines;
    }

    /**
     * form footer for output without web
     *
     * @return list String with footer for output
     */
    public List<String> formFooterLinesWithoutWeb() {
        ResourceBundle messages = CommonConstants.getResourceBundle();
        List<String> footLines = new ArrayList<>();
        footLines.add(messages.getString("nameStressProfile") + "\n");
        footLines.add(messages.getString("nameNumberSyllable") + "\n");

        Function<SegmentOfPortion, String> funcGetMeter = (SegmentOfPortion::getSelectedMeterRepresentation);
        double[] stressProfile = getStressProfileFromSegments(funcGetMeter);
        for (int i = 0; i < stressProfile.length; i++) {
            footLines.add("\t" + (i + 1));
        }
        footLines.add("\n");
        footLines.add(messages.getString("namePercentStress") + "\n");
        for (double v : stressProfile) {
            footLines.add("\t" + (int) v);
        }
        footLines.add("\n");
        footLines.add("==========================\n");
        footLines.add(messages.getString("nameJunctureProfile") + "\n");
        footLines.add(messages.getString("nameNumberSyllable") + "\n");
        double[] junctureProfile = getJunctureProfileFromSegments(funcGetMeter);
        for (int i = 0; i < junctureProfile.length; i++) {
            footLines.add("\t" + (i + 1));
        }
        footLines.add("\n");
        footLines.add(messages.getString("namePercentJuncture") + "\n");
        for (double v : junctureProfile) {
            footLines.add("\t" + (int) v);
        }
        footLines.add("\n");
        footLines.add("==========================\n");

        //max, min, average length:
        addFooterLinesWithAverageValues(footLines);

        return footLines;
    }

    /**
     * form footer for output with web
     *
     * @return list String with footer for output
     */
    public List<String> formFooterLinesWithWeb() {
        List<String> footLines = new ArrayList<>();
        //max, min, average length:
        addFooterLinesWithAverageValues(footLines);
        return footLines;
    }

    /**
     * add to footer min, max and average values
     */
    public void addFooterLinesWithAverageValues(List<String> footLines) {
        ResourceBundle messages = CommonConstants.getResourceBundle();
        footLines.add(messages.getString("web.numberSentencesVerse") + this.getListOfSegments().size() + "\n");
        footLines.add(messages.getString("web.maxLengthLine") + (int)this.getMaxDuration() + "\n");
        footLines.add(messages.getString("web.minLengthLine") + (int)this.getMinDuration() + "\n");
        footLines.add(messages.getString("web.averageLengthSentenseVerse") + (double)((int)(100*this.getAverageDuration()))/100 + "\n");
        footLines.add("----------------------------\n");
    }

    /**
     * output resume without web
     */
    public void resumeOutputConsole(StringBuilder outputAccumulation, CommonConstants commonConstants) {

        List<String> headerLines = formHeaderLines(this.getNumberOfPortion());
        for (String line : headerLines) {
            outputAccumulation.append(line);
        }

        DataTable dt = this.getDtOfTextSegmentsAndStresses();
        List<SegmentOfPortion> listSegments = this.getListOfSegments();
        String nameOfFirstColumn = (String) dt.getNamesOfColumn().toArray()[1];

        outputLineInResume(outputAccumulation, new String[]{"Line", "Meter representation", "Meter-number of foots", "Shift meter (N syllable)", "Quantity of syllables"});
        for (SegmentOfPortion listSegment : listSegments) {
            Integer nSegment = listSegment.getSegmentIdentifier();
            List<String> words = (List<String>) dt.getValueFromColumnAndRowByCondition("Word", nameOfFirstColumn, nSegment);
            String line = words.stream().map(s -> s + " ").reduce("", String::concat).trim();
            String meterRepresentation = listSegment.getSelectedMeterRepresentation().trim();
            String meter = listSegment.getMeter().trim();
            int numberOfTonicFoot = listSegment.getNumberOfTonicFoot();
            int shiftRegularMeterOnSyllable = listSegment.getShiftRegularMeterOnSyllable();
            outputLineInResume(outputAccumulation, new String[]{line, meterRepresentation, "[" + numberOfTonicFoot + "-" + meter + "]",
                    "[" + shiftRegularMeterOnSyllable + "]", "[" + listSegment.getNumberSyllable() + "]"});
        }
        outputAccumulation.append("==========================\n");
        outputAccumulation.append("\n");

        List<String> footLines = formFooterLinesWithoutWeb();
        for (String line : footLines) {
            outputAccumulation.append(line);
        }

        //if it is last portion
        if (getNumberOfPortion() == AnalyserPortionOfText.getListOfInstance().size()) {
            outputAccumulation.append("==========================\n");
            LocalDateTime localDateTime = LocalDateTime.now();
            outputAccumulation.append("End: ").append(localDateTime).append(" ------------!\n");
        }

        if (!commonConstants.isReadingFromFile()) {//output to console
            System.out.println(outputAccumulation.toString());
        } else // writing to file
        {
            FileTreatment.outputResultToFile(outputAccumulation, commonConstants.getFileOutputDirectory() + commonConstants.getFileOutputName(), true);
        }
        //clear outputAccumulation before next portion
        outputAccumulation.delete(0, outputAccumulation.length() - 1);
    }

    /**
     * output resume with web
     */
    public void prepareResumeOutputWeb() {
        HeaderAndFooterListsForWebOutput.getPortionHeaders().add(formHeaderLines(getNumberOfPortion()));
        List<String> footer = new ArrayList<>();
        addFooterLinesWithAverageValues(footer);
        HeaderAndFooterListsForWebOutput.getPortionFooters().add(footer);
    }

    /**
     * output resume
     */
    @Override
    public void resumeOutput(StringBuilder outputAccumulation, CommonConstants commonConstants) {
        if (!commonConstants.isThisIsWebApp()) {
            resumeOutputConsole(outputAccumulation, commonConstants);
        } else {
            prepareResumeOutputWeb();
        }
    }
}
