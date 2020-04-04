package textsVocal.structure.ru;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import textsVocal.structure.Alphabetable;
import textsVocal.structure.TextForRythm;
import textsVocal.utils.DBHelper;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static textsVocal.structure.TextForRythm.symbolOfNoStress;
import static textsVocal.structure.TextForRythm.symbolOfStress;

public class VocalAnalisysWordRu implements Alphabetable {

    //=== fields ============================================================
    private static Map<String, Set<String>> tempWordDictionary = new HashMap<>();//temporary dictionary ("cash")
    private List<String> listWord;
    private static final Logger log = LoggerFactory.getLogger(VocalAnalisysWordRu.class);

    //=== constructors ======================================================
    public VocalAnalisysWordRu(List<String> listWord) {
        this.listWord = listWord;
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

        private vocals(int dur) {
            this.duration = dur;
        }

        public int getDuration() {
            return duration;
        }
    }

    //=== overriden methods =================================================
    @Override
    /**
     * calculate duration
     */
    public Number calculateDuration(String word) {
        int duration = 0;
        for (int i = 0; i < word.length(); i++) {
            for (alphabetAndDuration letter : alphabetAndDuration.values()) {
                if (("" + word.charAt(i)).equals(letter.name())) {

                    duration += letter.getDuration();
                }
            }
        }

        return duration;
    }

    @Override
    public Map getRythmSchemaOfTheText() {
        return getMeterSchemaFromDataBase(listWord);
    }

    //=== public static methods =============================================
    public static Map<String, Set<String>> getTempWordDictionary() {
        return tempWordDictionary;
    }

    public static void setTempWordDictionary(Map<String, Set<String>> tempWordDictionary) {
        VocalAnalisysWordRu.tempWordDictionary = tempWordDictionary;
    }

    /**
     * service static function for stress definitions for russian words with "ё"
     *
     * @param textWord
     * @return
     */
    public static String particularCasesOfWords(String textWord) {
        int positionЁ = textWord.indexOf("ё");
        String repr = "";
        if (positionЁ >= 0) {
            for (int i = 0; i < textWord.length(); i++) {
                if (i == positionЁ) {
                    repr += symbolOfStress;
                } else {
                    for (vocals letter : vocals.values()) {
                        if (("" + textWord.charAt(i)).equals(letter.name())) {
                            repr += symbolOfNoStress;
                            ;
                        }
                    }
                }
            }
        }
        return repr;
    }

    /**
     * calculate duration with only vocale
     *
     * @param word
     * @return
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

    //=== private instance methods ====================================

    /**
     * check whether all symbols in representation are 'normal'
     *
     * @param s
     * @return
     */
    private boolean checkSymbolsOfRepresentation(String s) {
        boolean check = true;
        String possibleRepresentations = "";
        for (int i = 0; i < TextForRythm.stressRepresentations.length; i++) {
            possibleRepresentations += TextForRythm.stressRepresentations[i];
        }
        for (int i = 0; i < s.length(); i++) {
            if (!possibleRepresentations.contains("" + s.charAt(i))) {
                check = false;
                break;
            }
        }
        return check;
    }

    /**
     * service function to fill Map stressMap from resultset
     *
     * @param rs        - resultset from database
     * @param stressMap = set with various stresses
     */
    private void fillStressMapWithResultSet(ResultSet rs, Map<String, Set<String>> stressMap) {

        try {
            while (rs.next()) {

                //System.out.println(rs.getString(1).trim() + ": " + rs.getString(2).trim());
                //there are duplicates in database, we are'nt need duplicates
                Set<String> serviceSet = stressMap.get(rs.getString(1).trim());
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

                //it's a pitty, but such thing happen:
                if (currentRepresentation.length() >= 3 && !currentRepresentation.contains("" + symbolOfStress)) {
                    continue;
                }

                int indBracketLeft = currentRepresentation.indexOf("(");
                if (indBracketLeft > -1)// there are such cases, it's a pitty
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
                    stressMap.put(rs.getString(1).trim(), serviceSet);
                    //in the temporary dictionary:
                    tempWordDictionary.put(rs.getString(1).trim(), serviceSet);
                }
            }
        } catch (SQLException ex) {
            ex.getMessage();
        }
    }

    /**
     * receive data from database with stresses and return map with stresses
     *
     * @param words (list of words)
     * @return
     */
    private Map<String, Set<String>> getMeterSchemaFromDataBase(List words) {
        int n0 = 0;//start
        int nStep = 100;

        ApplicationContext context = new ClassPathXmlApplicationContext("beansTextsVocalUtil.xml");
        DBHelper db = context.getBean(DBHelper.class);

        String sql = "SELECT distinct textWord, meterRepresentation, partOfSpeech FROM " + db.getDb_Table() + " WHERE textWord in (";
        Map<String, Set<String>> stressMap = new HashMap<>();//returnimg service map

        Stream<String> prepare = words.stream()
                .map(s -> s.toString().toLowerCase().trim().replaceAll("ё", "е"))//in database there is only "е" but not "ё"
                .distinct();
        List<String> prepareWords = prepare.filter(s -> {
            return calculateDurationOnlyVocale(s).intValue() > 1;
        })
                .collect(Collectors.toList());

        try {
            Connection conn = db.getConnection();
            Statement stmt = conn.createStatement();
            String sqlArray = "";

            for (int i = 0; i < prepareWords.size(); i++) {
                String prepWord = prepareWords.get(i).trim();
                //if word is in temporary dictionary - that's enough
                if (tempWordDictionary.get(prepWord) != null) {
                    stressMap.put(prepWord, tempWordDictionary.get(prepWord));
                    n0++;
                    if (i != (prepareWords.size() - 1)) {
                        continue;
                    }
                }

                // if word is absent in tempWordDictionary
                if (sqlArray.isEmpty()) {
                    sqlArray += "'" + prepWord + "'";
                } else {
                    sqlArray += ", '" + prepWord + "'";
                }
                n0++;
                if ((n0 == nStep) || (i == (prepareWords.size() - 1))) {
                    ResultSet rs = stmt.executeQuery(sql + sqlArray + ")");
                    //System.out.println(sqlArray);
                    fillStressMapWithResultSet(rs, stressMap);

                    n0 = 0;
                    sqlArray = "";
                }
            }

        } catch (SQLException e) {
            log.error("Something wrong with SQL!", e);
            e.getMessage();
        }

        return stressMap;
    }


}
