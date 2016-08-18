package DataManager;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by zhuolil on 8/17/16.
 */
public class DatabaseManager extends DataManager{


    MysqlDataSource dataSource;
    private String url;
    private String databaseString;
    private String userName;
    private String password;

    private DatabaseManager() {

    }

    private DatabaseManager(String url, String database, String userName, String password)
    {
        this.url = url;
        this.databaseString = database;
        this.userName = userName;
        this.password = password;
    }

    public static void main(String[] args) {

    }

    /**
     * Initialize EmailMananger from XML configuration file.
     *
     * @param pathString : Configuration file path
     * @return: Emailmanager
     */
    public static DatabaseManager InitializeDatabaseManagerFromXML(String pathString) {
        Path currentPath = Paths.get("./");
        System.out.println("Currrent path: " + currentPath.toAbsolutePath());
        Path path = Paths.get("src", "resources", pathString);

        if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS))
            path = Paths.get(pathString);

        if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
            try {
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document doc = builder.parse(new DataInputStream(new FileInputStream(path.toFile())));
                Element documentElement = doc.getDocumentElement();
                Element databasesNode = (Element) documentElement.getElementsByTagName("Databases").item(0);

                String url = databasesNode.getElementsByTagName("Url").item(0).getTextContent();
                String database = databasesNode.getElementsByTagName("Database").item(0).getTextContent();
                String user = databasesNode.getElementsByTagName("User").item(0).getTextContent();
                String password = databasesNode.getElementsByTagName("Password").item(0).getTextContent();

                return new DatabaseManager(url, database, user, password);
            } catch (Exception e) {
                Logger.getGlobal().log(Level.SEVERE, "Failed to read configuration file from " + pathString, e);
            }
            return new DatabaseManager();
        } else {
            return new DatabaseManager();
        }
    }

    public DatabaseManager Authenticate() throws SQLException {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        this.dataSource = new MysqlDataSource();
        this.dataSource.setUser(this.userName);
        this.dataSource.setPassword(this.password);
        this.dataSource.setServerName(this.url);
        this.dataSource.setDatabaseName(this.databaseString);

        try {
            conn = dataSource.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM StockItem");
            return this;

        } catch (SQLException e) {
            throw e;
        } finally {
            try {
                stmt.close();
                conn.close();
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public List<JSONObject> UpdateStockItemsIfHasNew()
    {
        try {
            return this.ReadStockDatabase();
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.SEVERE, "SQL Failure", e);
        }
        return new LinkedList<>();
    }

    /**
     * Read stocks from CSV file.
     * @return stock item array.
     * @throws IOException
     */
    public List<JSONObject> ReadStockDatabase() throws SQLException {

        LinkedList<JSONObject> jsonObjectList = new LinkedList<>();

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = dataSource.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM StockItem");
            while (rs.next()) {
                String symbol = rs.getString(DatabaseManager.SYMBOL);
                double price = rs.getDouble(DatabaseManager.AVERAGECOST);
                int number = rs.getInt(DatabaseManager.SHARES);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(DataManager.SYMBOL, symbol).put(DataManager.AVERAGECOST, price).put(DataManager.SHARES, number);
                jsonObjectList.add(jsonObject);

            }
        } catch (SQLException e) {
            throw e;
        } finally {
            try {
                stmt.close();
                conn.close();
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return jsonObjectList;
    }
}
