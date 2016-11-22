package DataManager;

import JooqMap.tables.records.SharedstockitemsRecord;
import com.mysql.jdbc.CommunicationsException;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.junit.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static JooqMap.Tables.SHAREDSTOCKITEMS;


/**
 * MYSql Connector and Executor.
 * Created by zhuolil on 8/17/16.
 */
public class DatabaseManager extends DataManager{


    private String url;
    private String userName;
    private String password;


    /**
     * Initialize EmailMananger from XML configuration file.
     *
     * @param pathString : Configuration file path
     * @return: Emailmanager
     */
    public static DatabaseManager GetDatabaseManagerInstance(String pathString) {
        Path currentPath = Paths.get("./");
        System.out.println("Currrent path: " + currentPath.toAbsolutePath());
        Path path = Paths.get("src", "resources", pathString);

        if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS))
            path = Paths.get(pathString);

        if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
            try {

                // Create XML object and read values from the given path
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
        return this;
    }

    private DatabaseManager() {
        // Do nothing
    }

    /**
     * Initialize DatabaseManager with the given Server Address and credential.
     * @param dbUrl
     * @param database
     * @param userName
     * @param password
     */
    private DatabaseManager(String dbUrl, String database, String userName, String password)
    {
        Assert.assertNotNull(dbUrl);
        Assert.assertNotNull(database);
        Assert.assertNotNull(userName);
        Assert.assertNotNull(password);

        this.url = "jdbc:mysql://" + dbUrl + "/" + database;
        this.userName = userName;
        this.password = password;

        Logger.getGlobal().log(Level.INFO, "Database Server User: {0}, at Address: {1}", new Object[]{ this.userName, this.url});
    }

    public static void main(String[] args) {

    }


    @Override
    public void WriteSharedStocks(Order[] orders) throws Exception {
        this.getNewQueriedStockItemsFunc.get();

        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            Logger.getGlobal().log(Level.SEVERE, "", e);
        }

        // Connection is the only JDBC resource that we need
        // PreparedStatement and ResultSet are handled by jOOQ, internally
        try (Connection conn = DriverManager.getConnection(this.url, this.userName, this.password)) {
            DSLContext create = DSL.using(conn, SQLDialect.MYSQL);
            Optional<Table<?>> table = this.GetTable(create, SHAREDSTOCKITEMS.getName());
            if (!table.isPresent()) {
                create.createTable(SHAREDSTOCKITEMS).columns(SHAREDSTOCKITEMS.fields()).execute();
            }

            // Database query result
            Result<Record> result = create.select().from(SHAREDSTOCKITEMS).fetch();

            // Map query result to StockItems
            HashMap<String, SharedstockitemsRecord> stockMap = new HashMap<>();
            result.stream().map(r -> (SharedstockitemsRecord) r).forEach(stockItem -> stockMap.put(stockItem.getSymbol(), stockItem));

            // Check each new email order
            for (Order order : orders) {
                // If in table, update row
                if (stockMap.containsKey(order.Symbol)) {
                    SharedstockitemsRecord sharedStock = stockMap.get(orders);
                    sharedStock = this.UpdateStockShares(sharedStock, order);

                    // Delete shares
                    if (sharedStock.getShares() == 0)
                    {
                        // Deletes this record from the database, based on the value of the primary key or main unique key.
                        sharedStock.delete();
                    }

                    // Store this record back to the database using an UPDATE statement.
                    // http://www.jooq.org/javadoc/3.2.5/org/jooq/impl/UpdatableRecordImpl.html#update()
                    sharedStock.update();

                } else {
                    // Else, insert this row
                    create.insertInto(SHAREDSTOCKITEMS,
                            SHAREDSTOCKITEMS.SYMBOL, SHAREDSTOCKITEMS.SHARES, SHAREDSTOCKITEMS.SHAREDAVERAGECOST)
                            .values(order.Symbol, order.Shares, order.Price).execute();

                    System.out.println("New row inserted " + order);
                }
                break;
            }
        }
    }

    /**
     * Update shared stock database row, if new bought of this stock Symbol, insert new row,
     * else update the existing row, delete this row if it's close out.
     * @param sharedStock
     * @param newOrder
     * @return
     */
    private SharedstockitemsRecord UpdateStockShares(SharedstockitemsRecord sharedStock, Order newOrder)
    {
        Assert.assertNotNull(sharedStock);
        Assert.assertNotNull(newOrder);

        // Order type should always be set
        Assert.assertTrue(newOrder.type != OrderType.UNKNOWN);

        if(newOrder.type == OrderType.SELL)
            Assert.assertTrue("Existing shares should not less than the selling order", sharedStock.getShares() >= newOrder.Shares);

        // Get the number of shares
        int shares = sharedStock.getShares();

        // Get average cost of each share
        double aveCost = sharedStock.getSharedaveragecost();

        // Calculate the total cost after the new order
        double newSum = shares * aveCost;
        int newShares = shares;
        if (newOrder.type == OrderType.BUY) {
            newSum += newOrder.Price * newOrder.Shares;
            newShares += newOrder.Shares;
        }
        else {
            newSum -= newOrder.Price * newOrder.Shares;
            newShares -= newOrder.Shares;
        }

        // Divisor check
        if (newShares == 0)
        {
            sharedStock.setShares(0);
            return sharedStock;
        }

        // Calculate the new average cost
        double newAveCost = newSum / newShares;

        sharedStock.setShares(newShares);
        sharedStock.setSharedaveragecost(newAveCost);
        return sharedStock;
    }

    @Override
    public List<SharedstockitemsRecord> ReadSharedStocks() throws Exception{
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            Logger.getGlobal().log(Level.SEVERE, "jdbc Driver not found", e);
            throw e;
        }

        // Connection is the only JDBC resource that we need
        // PreparedStatement and ResultSet are handled by jOOQ, internally
        try (Connection conn = DriverManager.getConnection(this.url, this.userName, this.password)) {
            DSLContext create = DSL.using(conn, SQLDialect.MYSQL);
            Optional<Table<?>> table = this.GetTable(create, SHAREDSTOCKITEMS.getName());
            if (!table.isPresent()) {
                create.createTable(SHAREDSTOCKITEMS).columns(SHAREDSTOCKITEMS.fields()).execute();
            }
            Result<Record> result = create.select().from(SHAREDSTOCKITEMS).fetch();

            return result.stream().map(p -> (SharedstockitemsRecord) p).collect(Collectors.toList());
        }
    }
    public Optional<Table<?>> GetTable(DSLContext create, String tableName) {
        return create.meta().getTables().stream().filter(p -> p.getName().equals(tableName)).findFirst();
    }

}
