package textsVocal.structure;

import textsVocal.utils.DynamicTableRythm;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;

public class VersePortionForRythm extends TextForRythm {

    //== fields ========================================================
    private String pText;//formatted text
    private String originalText;//original text
    private static int validLevelOfMainMeterGroupInVerseText = 65;
    private static int validDifferenceBetweenTwoMainGroupsInVerseText = 30;
    private String mainMeter;//meter of whole text
    private String regularEndingsOfFirstStrophe;//ending's formula
    private String regularDurationOfFirstStrophe;//duration's formula
    private String regularNumberOfStressOfFirstStrophe;//number's of stress formula
    private double maxDuration = 0;//maximal duration in lines
    private double minDuration = Integer.MAX_VALUE;////minimal duration in lines


    // == constructor ==============================================================
    public VersePortionForRythm(String pText) {
        this.pText = prepareStringForParsing(pText, SYMB_BRIEF_PUNCTUATION, SYMB_SPACE).toString();
        this.originalText = pText;
    }

    public VersePortionForRythm(){

    }

    //== setters and getters ==========================================

    /**
     * reset fields by initialisation
     */
    @Override
    public void reset(String pText){
        this.mainMeter = "";
        this.regularEndingsOfFirstStrophe = "";
        this.regularDurationOfFirstStrophe = "";
        this.regularNumberOfStressOfFirstStrophe = "";
        this.maxDuration = 0;
        this.minDuration = Integer.MAX_VALUE;
        this.pText = prepareStringForParsing(pText, SYMB_BRIEF_PUNCTUATION, SYMB_SPACE).toString();
        this.originalText = pText;
    }

    public void setpText(String pText) {
        this.pText = pText;
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

    //== public static methods ========================================
    public static int getValidLevelOfMainMeterGroupInVerseText() {
        return validLevelOfMainMeterGroupInVerseText;
    }

    public static void setValidLevelOfMainMeterGroupInVerseText(int validLevelOfMainMeterGroupInVerseText) {
        validLevelOfMainMeterGroupInVerseText = validLevelOfMainMeterGroupInVerseText;
    }

    public static int getValidDifferenceBetweenTwoMainGroupsInVerseText() {
        return validDifferenceBetweenTwoMainGroupsInVerseText;
    }

    public static void setValidDifferenceBetweenTwoMainGroupsInVerseText(int validDifferenceBetweenTwoMainGroupsInVerseText) {
        validDifferenceBetweenTwoMainGroupsInVerseText = validDifferenceBetweenTwoMainGroupsInVerseText;
    }

    /**
     * identify rated verse meter with patterns - enum verseMeterPatterns/ Without pentons
     * @param s
     * @return
     */
    public static Map WhatIsTheMettersPatternForStringWithoutPentons(String s) {

        Map<String, String> descriptionMeter = new HashMap<>();
        //undefined content (we have not found pattern):
        descriptionMeter.put("meter", "Unknown");
        descriptionMeter.put("nTonicFoot", "0");
        descriptionMeter.put("nСaesuraSyllable", "0");

        //cut end of string
        String sWithoutEnding = s.trim();
        while (sWithoutEnding.endsWith("" + symbolOfNoStress) || sWithoutEnding.length() == 0) {
            sWithoutEnding = sWithoutEnding.substring(0, sWithoutEnding.length() - 1);
        }

        //if s - something like "00000"
        if (sWithoutEnding.length() == 0) {
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
        int nСaesuraSyllable = 0;
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

            minLength = sWithoutEnding.length() > pattern.getDuration() ? pattern.getDuration() : sWithoutEnding.length();
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

            maxLength = sWithoutEnding.length() <= pattern.getDuration() ? pattern.getDuration() : sWithoutEnding.length();

            for (int iShift = 1; iShift <= nShift; iShift++) {
                if (!descriptionMeter.get("meter").equals("Unknown") && descriptionMeter.get("nСaesuraSyllable").equals("0")) {
                    continue;
                }
                nMistake = 0;
                nСaesuraSyllable = CircleByPatternWhatIsTheMettersPatternForString(descriptionMeter, sWithoutEnding, pattern,
                        maxLength, minLength, iShift);

                if (nСaesuraSyllable > 999998) {
                    nMistake = nСaesuraSyllable;
                } else {
                    if (nСaesuraSyllable > 0) {
                        nMistake = 1;
                    } else {
                        nMistake = 0;
                    }
                }

                //System.out.println(sWithoutEnding + "; " + pattern.name() + ", mistakes " + nMistake);
                if (nMistake <= 1) {
                    //We allow write data to map, if it is the first try or the last try without mistakes
                    if (nMistake == 0 || descriptionMeter.get("meter") == "Unknown") {
                        descriptionMeter.put("meter", pattern.name());

                        nTonicFoot = SegmentOfPortion.getNumberOfStress(sWithoutEnding);
                        int footCorrection = sWithoutEnding.length() % strPattern.length();
                        //System.out.println(sWithoutEnding+"; start nTonic " + nTonicFoot+", foot correction "+footCorrection);
                        if (footCorrection == 0) {
                            if (sWithoutEnding.length() / strPattern.length() > nTonicFoot) {
                                nTonicFoot = sWithoutEnding.length() / strPattern.length();
                                // System.out.println("1/"+sWithoutEnding+"; nTonic " + nTonicFoot+", foot correction "+footCorrection);
                            }
                        } else {
                            if ((strPattern.charAt(footCorrection - 1) == symbolOfStress ? 1 : 0) + sWithoutEnding.length() / strPattern.length() > nTonicFoot) {
                                nTonicFoot = (strPattern.charAt(footCorrection - 1) == symbolOfStress ? 1 : 0) + sWithoutEnding.length() / strPattern.length();
                                //System.out.println("2/"+sWithoutEnding+"; nTonic " + nTonicFoot+", foot correction "+footCorrection);
                            } else {
                                //How to define in other way???
                                if (strPattern.length() == 3 && sWithoutEnding.contains("000000")) {
                                    nTonicFoot = (1 + sWithoutEnding.length() / strPattern.length() > nTonicFoot) ? 1 + sWithoutEnding.length() / strPattern.length() : nTonicFoot;
                                    //System.out.println("3/" + sWithoutEnding + "; nTonic " + nTonicFoot + ", foot correction " + footCorrection);
                                }
                            }

                        }
                        descriptionMeter.put("nTonicFoot", "" + nTonicFoot);
                        descriptionMeter.put("nСaesuraSyllable", "" + nСaesuraSyllable);
                    }
                }
            }
        }
        return descriptionMeter;
    }

    /**
     * identify rated verse meter with patterns - enum verseMeterPatterns/ Only pentons
     * @param s
     * @return
     */
    public static Map WhatIsTheMettersPatternForStringPentons(String s) {
        Map<String, String> descriptionMeter = new HashMap<>();
        //undefined content (we have not found pattern):
        descriptionMeter.put("meter", "Unknown");
        descriptionMeter.put("nTonicFoot", "0");
        descriptionMeter.put("nСaesuraSyllable", "0");

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
            for (int i = 0; i < 1 + sWithoutEnding.length() / strPattern.length(); i++) {

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
                descriptionMeter.put("nTonicFoot", "" + (1 + sWithoutEnding.length() / strPattern.length()));
                descriptionMeter.put("nСaesuraSyllable", "" + 0);
                return descriptionMeter;
            }

        }
        return descriptionMeter;
    }

    /**
     * service function for rating meter by pattern (nested in WhatIsTheMettersPatternForStringWithoutPentons)
     * @param descriptionMeter
     * @param sWithoutEnding
     * @param pattern
     * @param maxLength
     * @param minLength
     * @param nShift
     * @return
     */
    private static int CircleByPatternWhatIsTheMettersPatternForString(Map<String, String> descriptionMeter, String sWithoutEnding, verseMeterPatterns pattern,
                                                                       int maxLength, int minLength, int nShift) {

        int nMistake = 0;
        int nCaesureSyllable = 0;
        String strPattern = pattern.getPattern().trim();

        //System.out.println(pattern.name() + " nMistake before: " + nMistake);
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
                nCaesureSyllable = i + 1;//+1 because i begins of 0
            }

            if (nMistake > 1) {
                // nothing to do. Too much mistakes
                return 999999;
            }

        }
        return nCaesureSyllable;
    }

    // == public instance overriden methods ========================================
    @Override
    public String getpText() {
        return pText;
    }

    @Override
    /**
     * fill DynamicTableRythm with all segments, linew, word (without charackteristocs)
     */
    public DynamicTableRythm parsePortionOfText() {

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

        DynamicTableRythm prepareTable = new DynamicTableRythm(namesOfColumns, sup);

        int NumberOfFragment = 1;
        int NumberOfWord = 1;
        StringBuilder sbText = new StringBuilder(pText);
        String fragment = "";
        String word = "";
        int posSymbol = -1;
        List<Object> addData = new ArrayList<>();

        do {
            fragment = textFragmentToDelimiter(sbText, SYMB_SEGMENT);
            StringBuilder fragmentToWords = (fragment == null ? new StringBuilder(sbText.toString()) : new StringBuilder(fragment));

            do {
                word = textFragmentToDelimiter(fragmentToWords, SYMB_SPACE);
                StringBuilder wordToTable = (word == null ? fragmentToWords : new StringBuilder(word));

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
                addData.add((fragment == null ? sbText.toString().trim() : fragment.trim()));
                addData.add(NumberOfWord);
                addData.add(wordToTable.toString().trim());
                //addData.add(cleanWordFromPunctuation(wordToTable, SYMB_PUNCTUATION));
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

    /**
     * What sre the most popular meters in portion
     */
    public Map<String, Integer> WhatAreMostPolularMetersInMultiplePossibleValues() {

        TreeMap<String, Integer> treeGroupOfMeters = new TreeMap<>();
        List<SegmentOfPortion> preparedListOfSegment = getListOfSegments();

        if (preparedListOfSegment.isEmpty()) {
            getLog().error("There is no segments. Impossible to define meter in segment.", IllegalArgumentException.class);
            throw new IllegalArgumentException("There is no segments. Impossible to define meter in segment.");
        }

        // prioritize
        // compose map with "rate" (frequency) meters in segments
        preparedListOfSegment.stream().map((s) -> s.getTableOfMeterDefinitions()).map((dt) -> {
            // temporary set
            Set<String> tempSet = new HashSet<>();
            for (int i = 0; i < dt.getSize(); i++) {
                tempSet.add(dt.getValue("Meter", i).toString().trim());
            }
            return tempSet;
        }).forEach((tempSet) -> {
            tempSet.stream().forEach((groupMeter) -> {
                if (treeGroupOfMeters.containsKey(groupMeter)) {
                    treeGroupOfMeters.put(groupMeter, treeGroupOfMeters.get(groupMeter) + 1);
                } else {
                    treeGroupOfMeters.put(groupMeter, 1);
                }
            });
        });

        //now we have to sort the map by frequency (values in map)
        Map<String, Integer> sortedTreeGroupOfMetersWithFrequency = sortMapByComparator(treeGroupOfMeters, false);
        return sortedTreeGroupOfMetersWithFrequency;

    }

    /**
     * service function: sorting map by value
     * @param unsortMap
     * @param order
     * @return
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
     * fill portion with common rythm characteristics
     */
    public void fillPortionWithCommonRythmCharacteristics(Map priorityMap) {

        List<SegmentOfPortion> preparedListOfSegment = getListOfSegments();
        if (preparedListOfSegment.isEmpty()) {
            getLog().error("There is empty list of segments. Impossible to define meter in portion.", IllegalArgumentException.class);
            throw new IllegalArgumentException("There is empty list of segments. Impossible to define meter in portion.");
        }
        if (priorityMap == null) {
            getLog().error("Null instead of priority map.", IllegalArgumentException.class);
            throw new IllegalArgumentException("Null instead of priority map.");
        }
        if (priorityMap.isEmpty()) {
            getLog().error("Empty priority map.", IllegalArgumentException.class);
            throw new IllegalArgumentException("Empty priority map.");
        }

        String mainGroupName = ((String) (priorityMap.keySet().stream().findFirst().get())).trim();
        int mainPart = 100 * (Integer) (priorityMap.get(mainGroupName)) / preparedListOfSegment.size();

        //System.out.println("main group " + mainGroupName + "part " + mainPart);
        int secondPart = 0;
        String secondGroupName = "-------";
        Optional<String> secondGroup = priorityMap.keySet().stream().skip(1).findFirst();
        if (secondGroup.isPresent()) {
            secondPart = 100 * (Integer) (priorityMap.get(secondGroup.get())) / preparedListOfSegment.size();
            secondGroupName = (String) priorityMap.keySet().stream().skip(1).findFirst().get();
        }

        //System.out.println("second group " + secondGroupName + "part " + secondPart);
        if ((mainPart >= getValidLevelOfMainMeterGroupInVerseText()) && (mainPart - secondPart) >= getValidDifferenceBetweenTwoMainGroupsInVerseText()
                && !mainGroupName.equals("Unknown")) {
            this.setMainMeter(mainGroupName);
        } else if ((mainPart + secondPart) >= getValidLevelOfMainMeterGroupInVerseText()) {
            this.setMainMeter("Mixed or free verse");
        } else {
            this.setMainMeter("Unknown");
        }

        for (SegmentOfPortion s : preparedListOfSegment) {
            s.fillSegmentWithMeterCharacteristics(priorityMap, mainGroupName, secondGroupName, mainPart, secondPart);
        }

    }

    /**
     * check regularity endings, durations, so on
     * @param arr
     * @return
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
            String reg = "";
            for (int i = 0; i < iMin; i++) {
                reg += listSegment.get(i).getEnding() + "|";
            }
            setRegularEndingsOfFirstStrophe(reg);
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
            String reg = "";
            for (int i = 0; i < iMin; i++) {
                reg += (int) listSegment.get(i).getDuration() + "|";
            }
            setRegularDurationOfFirstStrophe(reg);
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
            array[i] = "" + SegmentOfPortion.getNumberOfStress(listSegment.get(i).getChoosedMeterRepresentation());
        }

        if (checkRegularity(array)) {
            int iMin = array.length > 5 ? 6 : array.length;
            String reg = "";
            for (int i = 0; i < iMin; i++) {
                reg += SegmentOfPortion.getNumberOfStress(listSegment.get(i).getChoosedMeterRepresentation()) + "|";
            }
            setRegularNumberOfStressOfFirstStrophe(reg);
        }
    }

    /**
     * set maximal and minimal duration for all over the text
     */
    public void setMaxAndMinDuration() {

        List<SegmentOfPortion> listSegments = getListOfSegments();
        double max = 0;
        double min = Integer.MAX_VALUE;
        for (int i = 0; i < listSegments.size(); i++) {
            if (listSegments.get(i).getDuration() > max) {
                max = listSegments.get(i).getDuration();
            }
            if (listSegments.get(i).getDuration() < min) {
                min = listSegments.get(i).getDuration();
            }
        }
        setMinDuration(min);
        setMaxDuration(max);
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

        for (int i = 0; i < listSegment.size(); i++) {
            if (listSegment.get(i).getChoosedMeterRepresentation().contains("0000")
                    || listSegment.get(i).getChoosedMeterRepresentation().contains("00000")) {
                thatIsAccentVerse = true;
            }
            if (listSegment.get(i).getChoosedMeterRepresentation().contains("000")) {
                thatIsTaktovikVerse = true;
            }
            if (SegmentOfPortion.getNumberOfStress(listSegment.get(i).getChoosedMeterRepresentation()) > maxNumberOfStress) {
                maxNumberOfStress = SegmentOfPortion.getNumberOfStress(listSegment.get(i).getChoosedMeterRepresentation());
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

    @Override
    /**
     * output resume
     */
    public void resumeOutput(int nPortion, StringBuilder outputAccumulation, String pathToFileOutput) {

        outputAccumulation.append("\n");
        outputAccumulation.append("Portion #" + nPortion + "\n");
        outputAccumulation.append("Main meter: " + this.getMainMeter() + "\n");
        outputAccumulation.append("Max. duration (in syllables): " + (int) this.getMaxDuration() + "\n");
        outputAccumulation.append("Min. duration (in syllables): " + (int) this.getMinDuration() + "\n");
        outputAccumulation.append("Endings of the first lines: " + this.getRegularEndingsOfFirstStrophe() + "\n");
        outputAccumulation.append("Duration of the first lines: " + this.getRegularDurationOfFirstStrophe() + "\n");
        outputAccumulation.append("Quantity of stresses in the first lines: " + this.getRegularNumberOfStressOfFirstStrophe() + "\n");
        outputAccumulation.append("\n");
        outputAccumulation.append("==========================\n");

        DynamicTableRythm dt = this.getDtOfTextSegmentsAndStresses();
        List<SegmentOfPortion> listSegments = this.getListOfSegments();

        outputLineInResume(outputAccumulation, new String[]{"Line", "Meter representation", "Meter-number of foots", "Shift meter (N syllable)", "Quantity of syllables"});
        for (int i = 0; i < listSegments.size(); i++) {
            Integer nSegment = listSegments.get(i).getSegmentIdentifier();
            List<String> words = (List<String>) dt.getValue("Word", "Number of line", nSegment);
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
                getLog().error("Something wrong with output!", e);
                e.getMessage();
            } catch (IOException e) {
                getLog().error("Something wrong with output!", e);
                e.getMessage();
            }
        }
        //clear outputAccumulation before next portion
        outputAccumulation.delete(0, outputAccumulation.length()-1);
    }

    /**
     * service function: forming output line
     * @param out
     * @param outputArr
     */
    private void outputLineInResume(StringBuilder out, String[] outputArr){
        int[] lengthInSymbols = new int[5];//length of columns in symbols
        lengthInSymbols[0] = 48;
        lengthInSymbols[1] = 24;
        lengthInSymbols[2] = 24;
        lengthInSymbols[3] = 24;
        lengthInSymbols[4] = 24;

        if (outputArr.length != lengthInSymbols.length){
            getLog().error("Non-equal arrays!", IllegalArgumentException.class);
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
        out.append("\n");
    }
}
