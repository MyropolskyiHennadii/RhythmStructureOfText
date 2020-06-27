package textsVocal.ru;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import textsVocal.config.CommonConstants;

import javax.annotation.PreDestroy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * maintenance Russian dictionary
 */
public class DB_RussianDictionary {

    private static final Logger log = LoggerFactory.getLogger(DB_RussianDictionary.class);//logger
    //=== fields ==================================
    private static String db_NAME = "schema_word_stress";
    private static String db_HOST = "localhost";
    private static int db_PORT = 3306;
    //private static String db_USER = "root";
    private static String db_USER = "guest";
    private static String db_PASSWORD = "1961";
    private static String db_Table = "stressworddictionaryru";
    private static String db_TableUnKnownWords = "unknownwords";//temporary table for unknown words
    private static Connection mainConnection;//main connection with stress table

    //=== setters and getters ==========================

    public static String getDb_NAME() {
        return db_NAME;
    }

    public static void setDb_NAME(String db_NAME) {
        DB_RussianDictionary.db_NAME = db_NAME;
    }

    public static String getDb_HOST() {
        return db_HOST;
    }

    public static void setDb_HOST(String db_HOST) {
        DB_RussianDictionary.db_HOST = db_HOST;
    }

    public static int getDb_PORT() {
        return db_PORT;
    }

    public static void setDb_PORT(int db_PORT) {
        DB_RussianDictionary.db_PORT = db_PORT;
    }

    public static String getDb_USER() {
        return db_USER;
    }

    public static void setDb_USER(String db_USER) {
        DB_RussianDictionary.db_USER = db_USER;
    }

    public static String getDb_PASSWORD() {
        return db_PASSWORD;
    }

    public static void setDb_PASSWORD(String db_PASSWORD) {
        DB_RussianDictionary.db_PASSWORD = db_PASSWORD;
    }

    public static String getDb_Table() {
        return db_Table;
    }

    public static void setDb_Table(String db_Table) {
        DB_RussianDictionary.db_Table = db_Table;
    }

    public static String getDb_TableUnKnownWords() {
        return db_TableUnKnownWords;
    }

    public static void setDb_TableUnKnownWords(String db_TableUnKnownWords) {
        DB_RussianDictionary.db_TableUnKnownWords = db_TableUnKnownWords;
    }

    /**
     * add record to service database with unknown words
     * @param word any string (word)
     * @param schema stress-schema for word
     */
    public static void addNewRecordToUnknownWordsDataBase(String word, String schema){
        ApplicationContext context = CommonConstants.getApplicationContext();
        DB_RussianDictionary db = context.getBean(DB_RussianDictionary.class);
        String sql = "INSERT INTO " + getDb_TableUnKnownWords() + " (textWord, meterRepresentation) " + "VALUES ('" + word + "', '";
        try {
            Connection conn = db.getConnectionMainStressTable();
            Statement stmt = conn.createStatement();
            int nRecords = stmt.executeUpdate(sql + schema + "')");
            log.info("Added records "+nRecords + " in unknown words database. Word = " + word + ", schema = " + schema);
        } catch (SQLException e) {
            log.error("Something wrong with SQL! {}", e.getMessage());
        }
    }

    /**
     * return main connection with stress table (one time new, than the same)
     *
     * @return connection with database
     * @throws SQLException
     */
    public Connection getConnectionMainStressTable()  throws SQLException{
        if (mainConnection == null) {
            try {
                mainConnection = DriverManager.getConnection("jdbc:mysql://" + db_HOST + ":" + db_PORT + "/" + db_NAME + "?useSSL=false", db_USER, db_PASSWORD);
            } catch (SQLException e) {
                log.error("Can't create connection with DB!" + e.getMessage());
                throw e;
            }
        }
        return mainConnection;
    }

    /**
     * before destroying
     * @throws SQLException
     */
    @PreDestroy
    public void beforeDestoying() throws SQLException {
        if(mainConnection != null){
            try {
                mainConnection.close();}
            catch (SQLException e) {
                log.error("Can't close connection with DB!" + e.getMessage());
                throw e;
            }
        }
    }

}
