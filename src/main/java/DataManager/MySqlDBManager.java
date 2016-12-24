package DataManager;

import JooqORM.tables.records.StockRecord;
import PriceMonitor.PriceMonitor;
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
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static JooqORM.Tables.STOCK;


/**
 * MYSql Connector and Executor.
 * Created by zhuolil on 8/17/16.
 */
public class MySqlDBManager extends DataManager {

    private String url;
    private String userName;
    private String password;

    /**
     * Initialize EmailMananger from XML configuration file.
     *
     * @param pathString
     *            : Configuration file path
     * @return: Emailmanager
     */
    public static MySqlDBManager GetDatabaseManagerInstance(String pathString) {
        Path currentPath = Paths.get("./");
        System.out.println("Currrent path: " + currentPath.toAbsolutePath());
        Path path = Paths.get("src", "resources", pathString);

        if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS))
            path = Paths.get(pathString);

        if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
            try {

                // Create XML object and read values from the given path
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document doc =
                        builder.parse(new DataInputStream(new FileInputStream(path.toFile())));
                Element documentElement = doc.getDocumentElement();
                Element databasesNode =
                        (Element) documentElement.getElementsByTagName("Databases").item(0);

                String url = databasesNode.getElementsByTagName("Url").item(0).getTextContent();
                String database =
                        databasesNode.getElementsByTagName("Database").item(0).getTextContent();
                String user = databasesNode.getElementsByTagName("User").item(0).getTextContent();
                String password =
                        databasesNode.getElementsByTagName("Password").item(0).getTextContent();

                return new MySqlDBManager(url, database, user, password);
            } catch (Exception e) {
                Logger.getGlobal().log(Level.SEVERE,
                        "Failed to read configuration file from " + pathString, e);
            }
            return new MySqlDBManager();
        } else {
            return new MySqlDBManager();
        }
    }

    public MySqlDBManager Authenticate() throws SQLException {
        return this;
    }

    private MySqlDBManager() {
        // Do nothing
    }

    /**
     * Initialize MySqlDBManager with the given Server Address and credential.
     *
     * @param dbUrl
     * @param database
     * @param userName
     * @param password
     */
    private MySqlDBManager(String dbUrl, String database, String userName, String password) {
        Assert.assertNotNull(dbUrl);
        Assert.assertNotNull(database);
        Assert.assertNotNull(userName);
        Assert.assertNotNull(password);

        this.url = "jdbc:mysql://" + dbUrl + "/" + database;
        this.userName = userName;
        this.password = password;
    }

    private Connection conn = null;
    private DSLContext globalCreate = null;

    /**
     * Sets up and maintains SQL connection
     *
     * @return DSLContext instance
     * @throws Exception
     */
    public DSLContext getDBJooqCreate() throws Exception {

        // Reuse sql connection
        if (this.conn != null && !this.conn.isClosed() && this.globalCreate != null)
            return this.globalCreate;

        this.conn = DriverManager.getConnection(this.url, this.userName, this.password);
        this.globalCreate = DSL.using(this.conn, SQLDialect.MYSQL);
        Optional<Table<?>> table = this.GetTable(this.globalCreate, STOCK.getName());
        if (!table.isPresent()) {
            this.globalCreate.createTable(STOCK).columns(STOCK.fields()).execute();
        }
        return this.globalCreate;
    }

    @Override
    /**
     * Write shares back to mysql database.
     */
    public void recordSharedStocks(Order[] orders) throws Exception {

        if (orders.length == 0)
            return;

        this.getNewQueriedStockItemsFunc.get();

        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            Logger.getGlobal().log(Level.SEVERE, "", e);
            throw e;
        }

        // Database query result
        Result<Record> result = this.getDBJooqCreate().select().from(STOCK).fetch();

        // Map query result to StockItems
        HashMap<String, StockRecord> dbStockMap = new HashMap<>();
        result.stream().map(r -> (StockRecord) r).forEach(stockRecord -> dbStockMap.put(stockRecord.getSymbol(), stockRecord));

        // Check each email order
        for (Order order : orders) {

            StockRecord updatedStockRecord;

            // If in table, update row
            if (dbStockMap.containsKey(order.Symbol)) {
                updatedStockRecord = dbStockMap.get(order.Symbol);

                // Update local stock record from database table, this unlikely happen unless db be changed in other way
                synchronized (PriceMonitor.stockPriceMap){
                    StockRecord stockItem = PriceMonitor.stockPriceMap.get(order.Symbol);
                    if (stockItem!=null && stockItem.getTimestamp().before(updatedStockRecord.getTimestamp()))
                        PriceMonitor.stockPriceMap.put(order.Symbol, updatedStockRecord);
                }

                updatedStockRecord = this.UpdateStockShares(updatedStockRecord, order);

                // Delete shares
                if (updatedStockRecord.getShares() == 0)
                {
                    // Deletes this record from the database, based on the value of the primary key or main unique key.
                    updatedStockRecord.delete();
                    continue;

                }
                else {

                    synchronized (PriceMonitor.stockPriceMap) {
                        StockRecord stockItem = PriceMonitor.stockPriceMap.get(order.Symbol);
                        if (stockItem != null) {
                            // Write report date
                            updatedStockRecord.setReportDate(stockItem.getReportDate());

                        }
                    }

                    // Update time stamp on each DB update
                    updatedStockRecord.setTimestamp(Timestamp.valueOf(LocalDateTime.now()));

                    // Store this record back to the database using an UPDATE statement.
                    // http://www.jooq.org/javadoc/3.2.5/org/jooq/impl/UpdatableRecordImpl.html#update()
                    updatedStockRecord.update();
                }

            } else {
                // Else, insert this row
                this.getDBJooqCreate()
                        .insertInto(STOCK, STOCK.SYMBOL, STOCK.SHARES, STOCK.SHARED_AVERAGE_COST,
                                STOCK.CURRENT_PRICE_LATEST_UPDATE_TIME, STOCK.TIMESTAMP)
                        .values(order.Symbol, order.Shares, order.Price,
                                Timestamp.valueOf(LocalDateTime.now()),
                                Timestamp.valueOf(LocalDateTime.now()))
                        .execute();

                updatedStockRecord = this.getDBJooqCreate().fetchOne(STOCK, STOCK.SYMBOL.equal(order.Symbol));
                Assert.assertNotNull("Failed to write stock instance back to Database table", updatedStockRecord);
                dbStockMap.put(updatedStockRecord.getSymbol(), updatedStockRecord);
                System.out.println("New row inserted " + order);
            }

            // Update local stock record to the latest one
            synchronized (PriceMonitor.stockPriceMap) {

                Assert.assertNotNull("updatedStockRecord should never be null", updatedStockRecord);

                PriceMonitor.stockPriceMap.put(order.Symbol, updatedStockRecord);
            }

        }
    }

    public void writeReportDate(StockRecord[] stockItems) throws Exception {
        this.getNewQueriedStockItemsFunc.get();

        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            Logger.getGlobal().log(Level.SEVERE, "", e);
            throw e;
        }

        // Check each email order
        for (StockRecord stockItem : stockItems) {
            StockRecord updatedStock =
                    this.getDBJooqCreate().fetchOne(STOCK,
                            STOCK.SYMBOL.equal(stockItem.getSymbol()));

            // If in table, update row
            if (updatedStock != null) {
                updatedStock.setReportDate(stockItem.getReportDate());

                // Store this record back to the database using an UPDATE statement.
                // http://www.jooq.org/javadoc/3.2.5/org/jooq/impl/UpdatableRecordImpl.html#update()
                updatedStock.update();

            } else {
                // Else, insert this row
                this.getDBJooqCreate()
                        .insertInto(STOCK, STOCK.SYMBOL, STOCK.SHARES, STOCK.SHARED_AVERAGE_COST,
                                STOCK.REPORT_DATE)
                        .values(stockItem.getSymbol(), stockItem.getShares(),
                                stockItem.getTargetPrice(), stockItem.getReportDate())
                        .execute();

                StockRecord result =
                        this.getDBJooqCreate().fetchOne(STOCK,
                                STOCK.SYMBOL.equal(stockItem.getSymbol()));
                Assert.assertNotNull("Failed to write stock instance back to Database table",
                        updatedStock);
            }
        }
    }

    @Override
    public void updateCurrentPrice(StockRecord[] stockRecords) throws java.lang.Exception{
        for(StockRecord stockRecord : stockRecords){
            if(stockRecord.getCurrentPrice()==0)
                continue;
            this.getDBJooqCreate()
                    .update(STOCK)
                    .set(STOCK.CURRENT_PRICE, stockRecord.getCurrentPrice())
                    .set(STOCK.CURRENT_PRICE_LATEST_UPDATE_TIME, stockRecord.getCurrentPriceLatestUpdateTime())
                    .where(STOCK.SYMBOL.equal(stockRecord.getSymbol()))
                    .execute();

        }
    }

    /**
     * Update shared stock database row, if new bought of this stock Symbol, insert new row, else
     * update the existing row, delete this row if it's close out.
     *
     * @param sharedStock
     * @param newOrder
     * @return
     */
    private StockRecord UpdateStockShares(StockRecord sharedStock, Order newOrder) {
        Assert.assertNotNull(sharedStock);
        Assert.assertNotNull(newOrder);

        // Order type should always be set
        Assert.assertTrue(newOrder.type != OrderType.UNKNOWN);

        // if(newOrder.type == OrderType.SELL)
        // Assert.assertTrue(String.format("Existing shares $s should not less than the selling order $s for symbol: '$s'",
        // sharedStock.getShares(), newOrder.Shares, newOrder.Symbol), sharedStock.getShares() >=
        // newOrder.Shares);

        // Get the number of shares
        int shares = sharedStock.getShares();

        // Get average cost of each share
        double aveCost = sharedStock.getSharedAverageCost();

        // Calculate the total cost after the new order
        double newSum = shares * aveCost;
        int newShares = shares;
        if (newOrder.type == OrderType.BUY) {
            newSum += newOrder.Price * newOrder.Shares;
            newShares += newOrder.Shares;
        } else {
            newSum -= newOrder.Price * newOrder.Shares;
            newShares -= newOrder.Shares;
        }

        // Divisor check
        if (newShares == 0) {
            sharedStock.setShares(0);
            return sharedStock;
        }

        // Calculate the new average cost
        double newAveCost = newSum / newShares;

        sharedStock.setShares(newShares);
        sharedStock.setSharedAverageCost(newAveCost);
        return sharedStock;
    }

    @Override
    public List<StockRecord> RetriveSharedStocks() throws java.lang.Exception {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            Logger.getGlobal().log(Level.SEVERE, "", e);
            return new LinkedList<>();
        }
        // Connection is the only JDBC resource that we need
        // PreparedStatement and ResultSet are handled by jOOQ, internally
        DSLContext global = this.getDBJooqCreate();
        Result<Record> result = global.select().from(STOCK).fetch();

        return result.stream().map(p -> (StockRecord) p).collect(Collectors.toList());
    }

    public Optional<Table<?>> GetTable(DSLContext create, String tableName) {
        return create
                .meta()
                .getTables()
                .stream()
                .filter(p -> p.getName().equals(tableName))
                .findFirst();
    }
}
