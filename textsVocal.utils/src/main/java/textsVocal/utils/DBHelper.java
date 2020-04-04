package textsVocal.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBHelper {

    //=== fields ==================================
    private static String db_NAME = "schema_word_stress";
    private static String db_HOST = "localhost";
    private static int db_PORT = 3306;
    private static String db_USER = "root";
    private static String db_PASSWORD = "1961";
    private static String db_Table = "stressworddictionaryru";
    private static final Logger log = LoggerFactory.getLogger(DBHelper.class);//logger
    private static Connection conn;

    //=== setters and getters ==========================

    public static String getDb_NAME() {
        return db_NAME;
    }

    public static String getDb_HOST() {
        return db_HOST;
    }

    public static int getDb_PORT() {
        return db_PORT;
    }

    public static String getDb_USER() {
        return db_USER;
    }

    public static String getDb_PASSWORD() {
        return db_PASSWORD;
    }

    public static String getDb_Table() {
        return db_Table;
    }

    public static void setDb_NAME(String db_NAME) {
        DBHelper.db_NAME = db_NAME;
    }

    public static void setDb_HOST(String db_HOST) {
        DBHelper.db_HOST = db_HOST;
    }

    public static void setDb_PORT(int db_PORT) {
        DBHelper.db_PORT = db_PORT;
    }

    public static void setDb_USER(String db_USER) {
        DBHelper.db_USER = db_USER;
    }

    public static void setDb_PASSWORD(String db_PASSWORD) {
        DBHelper.db_PASSWORD = db_PASSWORD;
    }

    public static void setDb_Table(String db_Table) {
        DBHelper.db_Table = db_Table;
    }

    public static int count;

    /**
     * return connection (one time new, than the same)
     *
     * @return
     * @throws SQLException
     */
    public Connection getConnection()  throws SQLException{
        if (conn == null) {
            try {
                conn = DriverManager.getConnection("jdbc:mysql://" + db_HOST + ":" + db_PORT + "/" + db_NAME + "?useSSL=false", db_USER, db_PASSWORD);
                ;
            } catch (SQLException e) {
                log.error("Can't create connection with DB!", e);
                throw e;
            }
        }
        return conn;
    }

    /**
     * before destroying
     * @throws SQLException
     */
    public void beforeDestoying() throws SQLException {
        if(conn != null){
            try {
            conn.close();}
            catch (SQLException e) {
                log.error("Can't close connection with DB!", e);
                throw e;
            }
        }
    }

}
