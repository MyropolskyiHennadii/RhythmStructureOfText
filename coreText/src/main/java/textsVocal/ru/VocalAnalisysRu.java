package textsVocal.ru;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import textsVocal.config.CommonConstants;
import textsVocal.structure.SegmentOfPortion;
import textsVocal.structure.TextPortionForRythm;
import textsVocal.structure.VersePortionForRythm;
import textsVocal.structure.Word;
import textsVocal.utilsCommon.DataTable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static textsVocal.structure.TextPortionForRythm.symbolOfNoStress;
import static textsVocal.structure.TextPortionForRythm.symbolOfStress;

/**
 * class for all Russian vocal-analysis methods (mostly static) and near
 */
public class VocalAnalisysRu {

    //=== fields ============================================================
    private static final Logger log = LoggerFactory.getLogger(VocalAnalisysRu.class);

    //=== constructors ======================================================
    public VocalAnalisysRu() { }

    /**
     * receive data about stresses from database with stresses and return map with word as key and set of stresses as value
     *
     * @return String word with set of stress schema
     */
    public static Map<String, Set<String>> getStressSchemaForWordsList(List<String> words) {

        int n0 = 0;//start
        int nStep = 100;

        ApplicationContext context = CommonConstants.getApplicationContext();
        DB_RussianDictionary db = context.getBean(DB_RussianDictionary.class);

        String sql = "SELECT distinct textWord, meterRepresentation, partOfSpeech FROM " + DB_RussianDictionary.getDb_Table() + " WHERE textWord in (";
        Map<String, Set<String>> stressMap = new HashMap<>();//returnimg service map

        Stream<String> prepare = words.stream()
                .map(s -> s.toLowerCase().trim().replaceAll("ё", "е"))//in database there is only "е" but not "ё"
                .distinct();
        List<String> prepareWords = prepare.filter(s -> calculateDurationOnlyVocale(s).intValue() > 1)
                .collect(Collectors.toList());
        try {
            Connection conn = db.getConnectionMainStressTable();
            Statement stmt = conn.createStatement();
            StringBuilder sqlArray = new StringBuilder();
            for (int i = 0; i < prepareWords.size(); i++) {
                String prepWord = prepareWords.get(i).trim();
                // if word is absent in tempWordDictionary
                if (sqlArray.length() == 0) {
                    sqlArray.append("'").append(prepWord).append("'");
                } else {
                    sqlArray.append(", '").append(prepWord).append("'");
                }
                n0++;
                if ((n0 == nStep) || (i == (prepareWords.size() - 1))) {

                    ResultSet rs = stmt.executeQuery(sql + sqlArray + ")");
                    fillStressMapWithResultSet(rs, stressMap);

                    n0 = 0;
                    sqlArray = new StringBuilder();
                }
            }

        } catch (SQLException e) {
            log.error("Something wrong with SQL!" + e.getMessage());
        }

        return stressMap;
    }

    /**
     * receive meter definitions for Segment
     * @return map with number of Segment as a key and set of representations as possible values
     */
    public static Map<Integer, Set<String>> getMeterSchemaOfSegment(SegmentOfPortion segment) {

        String var;
        Set<String> setMeters = new HashSet<>();//set of represantations
        Map<Integer, Set<String>> mapMeters = new HashMap<>();

        DataTable dtSegment = segment.getTableOfMeterDefinitions();

        List<String> meterRepresentations = segment.getMeterRepresentations();

        for (String meterRepresentation : meterRepresentations) {

            var = meterRepresentation.trim();

            Map<String, String> descriptionMeter = VersePortionForRythm.WhatIsTheMettersPatternForStringWithoutPentons(var);

            if (!"Unknown".equals(descriptionMeter.get("meter").trim()) || var.length() <= 10) {//without pentons
                String repr = var + ";" + descriptionMeter.get("meter").trim() + "-" + descriptionMeter.get("nTonicFoot").trim() + ";" + descriptionMeter.get("nShiftRegularMeterOnSyllable").trim();

                if (!setMeters.contains(repr)) {//without duplicates
                    dtSegment.setRow(Stream.of(var, descriptionMeter.get("meter").trim(), Integer.valueOf(descriptionMeter.get("nTonicFoot").trim()), Integer.valueOf(descriptionMeter.get("nShiftRegularMeterOnSyllable").trim())).collect(Collectors.toList()));
                    setMeters.add(repr);
                }
            } else {//pentons
                descriptionMeter = VersePortionForRythm.WhatIsTheMettersPatternForStringPentons(var);
                String repr = var + ";" + descriptionMeter.get("meter").trim() + "-" + descriptionMeter.get("nTonicFoot").trim() + ";" + descriptionMeter.get("nShiftRegularMeterOnSyllable").trim();

                if (!setMeters.contains(repr)) {//without duplicates
                    dtSegment.setRow(Stream.of(var, descriptionMeter.get("meter").trim(), Integer.valueOf(descriptionMeter.get("nTonicFoot").trim()), Integer.valueOf(descriptionMeter.get("nShiftRegularMeterOnSyllable").trim())).collect(Collectors.toList()));
                    setMeters.add(repr);
                }
            }
        }

        //if there is no row in dtSegment, that means: no word was recognized
        //Probably it was the line with foreign language
        if (dtSegment.getSize() == 0) {
            dtSegment.setRow(Stream.of("0", "Unknown. May be unknown language", 0, 0).collect(Collectors.toList()));
            segment.setMeter("" + symbolOfNoStress);
            segment.setSelectedMeterRepresentation("" + symbolOfNoStress);
        }

        mapMeters.put(segment.getSegmentIdentifier(), setMeters);
        return mapMeters;
    }


    //=== public static methods ===

    /**
     * check in Russian: whether current representation have sense for every Word from list
     *
     * @param meterRepresentation - sctress schema
     * @param listWords - list of Words
     * @return true if all is OK with meter representation (representation has sense)
     */
    public static boolean checkSensibleRepresentationOfMeter(String meterRepresentation, List<Word> listWords, boolean thisIsVerse) {

        boolean allOK = true;
        int numSyllableSum = 0;
        int numSyllable;

        for (Word listWord : listWords) {
            numSyllable = listWord.getNumSyllable();
            numSyllableSum += numSyllable;

            if (numSyllable == 1 && numSyllableSum < meterRepresentation.length() - 1) {
                if (thisIsVerse) {
                    //one-syllable-word with stress and next word beginns with stress
                    if (meterRepresentation.charAt(numSyllableSum - 1) == symbolOfStress
                            && meterRepresentation.charAt(numSyllableSum) == symbolOfStress) {
                        return false;
                    }
                    //one-syllable-word with stress and previous word ends with stress
                    if (numSyllableSum >= 2) {
                        if (meterRepresentation.charAt(numSyllableSum - 1) == symbolOfStress
                                && meterRepresentation.charAt(numSyllableSum - 2) == symbolOfStress) {
                            return false;
                        }
                    }
                }
            }
        }

        return allOK;
    }

    /**
     * calculate duration with only vocale
     *
     * @param word any Russian string
     * @return duration of string (number of vocale)
     */
    public static Number calculateDurationOnlyVocale(String word) {
        int duration = 0;
        for (int i = 0; i < word.length(); i++) {
            for (vocals letter : vocals.values()) {
                if (("" + word.charAt(i)).equals(letter.name())) {

                    duration += letter.getDuration();
                }
            }
        }
        return duration;
    }

    /**
     * service static function for stress definitions for russian words with "ё"
     *
     * @param textWord any Russian string
     * @return string with replaced symbol (if it need)
     */
    public static String particularCasesOfWords(String textWord) {
        int positionЁ = textWord.indexOf("ё");
        StringBuilder repr = new StringBuilder();
        if (positionЁ >= 0) {
            for (int i = 0; i < textWord.length(); i++) {
                if (i == positionЁ) {
                    repr.append(symbolOfStress);
                } else {
                    for (vocals letter : vocals.values()) {
                        if (("" + textWord.charAt(i)).equals(letter.name())) {
                            repr.append(symbolOfNoStress);
                        }
                    }
                }
            }
        }
        return repr.toString();
    }

    /**
     * check whether all symbols in representation are 'normal'
     *
     * @param s any Russian string
     * @return whether all sumbols in representation are correct
     */
    private static boolean checkSymbolsOfRepresentation(String s) {
        boolean check = true;
        StringBuilder possibleRepresentations = new StringBuilder();
        for (int i = 0; i < TextPortionForRythm.stressRepresentations.length; i++) {
            possibleRepresentations.append(TextPortionForRythm.stressRepresentations[i]);
        }
        for (int i = 0; i < s.length(); i++) {
            if (!possibleRepresentations.toString().contains("" + s.charAt(i))) {
                check = false;
                break;
            }
        }
        return check;
    }

    /**
     * service function to map string word to set stressMap from resultset
     *
     * @param rs        - resultset from database
     * @param stressSet = set with various stresses
     */
    private static void fillStressMapWithResultSet(ResultSet rs, Map<String, Set<String>> stressSet) {

        try {
            while (rs.next()) {
                //there are duplicates in database, we are'nt need duplicates
                Set<String> serviceSet = stressSet.get(rs.getString(1).trim());
                if (serviceSet == null) {
                    serviceSet = new HashSet<>();
                }

                String currentRepresentation = rs.getString(2).trim();

                String partSpeech = rs.getString(3).trim();
                boolean servicePart = (partSpeech.startsWith("Союз") ||
                        partSpeech.startsWith("Междоме") ||
                        partSpeech.startsWith("Предлог") ||
                        partSpeech.startsWith("Частица") ||
                        partSpeech.startsWith("Местоим"));

                //it's a pity, but such thing happen:
                if (currentRepresentation.length() >= 3 && !currentRepresentation.contains("" + symbolOfStress)) {
                    continue;
                }

                int indBracketLeft = currentRepresentation.indexOf("(");
                if (indBracketLeft > -1)// there are such cases, it's a pity
                {
                    int indBracketRight = currentRepresentation.indexOf(")");
                    if (indBracketRight <= indBracketLeft) {
                        continue; //that's mistake
                    }

                    String currentRepresentationWithoutBrackets = currentRepresentation.substring(indBracketLeft + 1, indBracketRight);
                    if (checkSymbolsOfRepresentation(currentRepresentation.substring(indBracketLeft + 1, indBracketRight))) {
                        serviceSet.add(currentRepresentation.substring(indBracketLeft + 1, indBracketRight));
                    }
                    currentRepresentationWithoutBrackets = currentRepresentation.replace("(" + currentRepresentationWithoutBrackets + ")", "");
                    if (checkSymbolsOfRepresentation(currentRepresentationWithoutBrackets)) {
                        serviceSet.add(currentRepresentationWithoutBrackets);
                    }
                } else {
                    if (checkSymbolsOfRepresentation(currentRepresentation)) {
                        serviceSet.add(currentRepresentation);
                    }
                }
                if (serviceSet.size() > 0) {
                    stressSet.put(rs.getString(1).trim(), serviceSet);
                    //in the temporary dictionary:
                    CommonConstants.getTempWordDictionary().put(rs.getString(1).trim(), serviceSet);
                }
            }
        } catch (SQLException ex) {
            log.error("Something wrong with SQL ResultSet! " + ex.getMessage());
        }
    }

    //=== enums =============================================================
    public enum alphabetAndDuration {

        а(1), б(0), в(0), г(0), д(0), е(1), ё(1),
        ж(0), з(0), и(1), й(0), к(0), л(0), м(0),
        н(0), о(1), п(0), р(0), с(0), т(0), у(1),
        ф(0), х(0), ц(0), ч(0), ш(0), щ(0), ь(0),
        ъ(0), ы(1), э(1), ю(1), я(1);

        private final int duration;

        private alphabetAndDuration(int dur) {
            this.duration = dur;
        }

        public int getDuration() {
            return duration;
        }
    }

    public enum vocals {

        а(1), е(1), ё(1), и(1), о(1), у(1), ы(1), э(1), ю(1), я(1);

        private final int duration;

        vocals(int dur) {
            this.duration = dur;
        }

        public int getDuration() {
            return duration;
        }
    }

}
