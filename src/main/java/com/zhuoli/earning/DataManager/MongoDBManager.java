package com.zhuoli.earning.DataManager;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoWriteException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;
import com.zhuoli.earning.PriceMonitor.PriceMonitor;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.mongodb.client.model.Filters.eq;

/**
 * MongoDB Connector and Executor. Created by zhuolil on 8/17/16.
 */
public class MongoDBManager extends DataManager {
    public static final String DB_NAME = "stockdb";
    private static String CONNECTION_STRING = "mongodb://stockdbzhuoli:QrLUUzcspLOK2pjdEvVlevms5zCfvhQlChWOtrLVRI1r5HF1mKwAKwwFm296SBSWLoOPnAQ8apN8zaPPYA3inQ==@stockdbzhuoli.documents.azure.com:10255/?ssl=true&replicaSet=globaldb";
    private static String EARN_SHARE_TABLE_NAME = "earningmanagement";
    MongoClient mongoClient = null;
    private MongoDatabase mongoDatabase;

    public MongoDBManager() {

        this.mongoClient = new MongoClient(new MongoClientURI(MongoDBManager.CONNECTION_STRING));
        this.mongoDatabase = this.mongoClient.getDatabase(MongoDBManager.DB_NAME);
    }

    /**
     * Initialize MongoDBManager with the given Server Address and credential.
     *
     * @param dbUrl
     * @param database
     * @param userName
     * @param password
     */
    private MongoDBManager(String dbUrl, String database, String userName, String password) {
        Assert.assertNotNull(dbUrl);
        Assert.assertNotNull(database);
        Assert.assertNotNull(userName);
        Assert.assertNotNull(password);


        this.mongoClient = new MongoClient(new MongoClientURI(MongoDBManager.CONNECTION_STRING));
        this.mongoDatabase = this.mongoClient.getDatabase(MongoDBManager.DB_NAME);
    }

    /**
     * Initialize EmailMananger from XML configuration file.
     *
     * @param pathString
     *            : Configuration file path
     * @return: Emailmanager
     */
    public static MongoDBManager GetDatabaseManagerInstance(String pathString) {
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

                return new MongoDBManager(url, database, user, password);
            } catch (Exception e) {
                Logger.getGlobal().log(Level.SEVERE,
                        "Failed to read configuration file from " + pathString, e);
            }
            return new MongoDBManager();
        } else {
            return new MongoDBManager();
        }
    }

    public MongoDBManager Authenticate(){
        return this;
    }

    @Override
    public void updateHeartBeat() throws Exception {

    }

    public <T>  List<T> retrieveDcouments(String tablename, Class<T> classname){
        ArrayList<T> result = new ArrayList<>();
        Gson gson = new Gson();
        MongoCollection chineseStockTable = this.mongoDatabase.getCollection(tablename);
        FindIterable<org.bson.Document> queryResult = chineseStockTable.find();
        for (org.bson.Document document : queryResult) {
            T sharesQuote = gson.fromJson(document.toJson(), classname);
            result.add(sharesQuote);
        }
        return result;
    }

    @Override
    /**
     * Write shares back to MongoDB database.
     */
    public void writeNewOrders2DB(Order[] orders) throws Exception {

        if (orders.length == 0)
            return;

        // Database query result
        List<StockRecord> result = this.retrieveDcouments(MongoDBManager.EARN_SHARE_TABLE_NAME, StockRecord.class);

        // Map query result to StockItems
        HashMap<String, StockRecord> dbStockMap = new HashMap<>();
        result.stream().forEach(stockRecord -> dbStockMap.put(stockRecord.getSymbol(), stockRecord));

        // Check each email order
        for (Order order : orders) {
            StockRecord updatedStockRecord;
            // If in table, update row
            if (dbStockMap.containsKey(order.getSymbol())) {
                updatedStockRecord = dbStockMap.get(order.getSymbol());

                // Update local stock record from database table, this unlikely happen unless db be
                // changed in other way
                synchronized (PriceMonitor.stockPriceMap) {
                    StockRecord stockItem = PriceMonitor.stockPriceMap.get(order.getSymbol());
                    if (stockItem != null
                            && stockItem.getTimestamp().before(updatedStockRecord.getTimestamp()))
                        PriceMonitor.stockPriceMap.put(order.getSymbol(), updatedStockRecord);
                }

                updatedStockRecord = this.UpdateStockShares(updatedStockRecord, order);

                // Delete shares
                if (updatedStockRecord.getShares() == 0) {
                    // Deletes this record from the database, based on the value of the primary key
                    // or main unique key.
                    this.deleteStockRecord(updatedStockRecord.getSymbol());
                    continue;

                } else {

                    synchronized (PriceMonitor.stockPriceMap) {
                        StockRecord stockItem = PriceMonitor.stockPriceMap.get(order.getSymbol());
                        if (stockItem != null) {
                            // Write report date
                            updatedStockRecord.setReportDate(stockItem.getReportDate());

                        }
                    }

                    // Update time stamp on each DB update
                    updatedStockRecord.setTimestamp(new Date());

                    // Store this record back to the database using an UPDATE statement.
                    this.updateDocument(MongoDBManager.EARN_SHARE_TABLE_NAME, updatedStockRecord);
                }

            } else {
                updatedStockRecord = StockRecord.builder().symbol(order.getSymbol()).shares(order.getShares()).sharedAverageCost(order.getPrice()).timestamp(new Date()).build();
                this.insertDocument(MongoDBManager.EARN_SHARE_TABLE_NAME,updatedStockRecord);

                dbStockMap.put(updatedStockRecord.getSymbol(), updatedStockRecord);
                System.out.println("New row inserted " + order);
            }

            // Update local stock record to the latest one
            synchronized (PriceMonitor.stockPriceMap) {

                Assert.assertNotNull("updatedStockRecord should never be null", updatedStockRecord);

                PriceMonitor.stockPriceMap.put(order.getSymbol(), updatedStockRecord);
            }

        }
    }

    public void deleteStockRecord(String symbol) {
        MongoCollection earningManagerTable = this.mongoDatabase.getCollection(MongoDBManager.EARN_SHARE_TABLE_NAME);
        earningManagerTable.deleteOne(eq("_id", symbol));
    }

    public void writeReportDate(StockRecord[] stockItems) throws Exception {
        for(StockRecord stockRecord : stockItems) {
            // Define the update query:
            BasicDBObject updateQuery = new BasicDBObject();
            updateQuery.append("$set", new BasicDBObject().append("reportDate", stockRecord.getReportDate()));
            this.updateDocument(MongoDBManager.EARN_SHARE_TABLE_NAME, stockRecord, stockRecord.getSymbol(), updateQuery);
        }
    }

    public synchronized void insertDocument( String tableName, StockRecord... records){
        MongoCollection earningManagerTable = this.mongoDatabase.getCollection(tableName);
        Gson gson = new Gson();
        for(StockRecord record : records){
            Assert.assertNotNull("stock Id should not be null", record.getSymbol());

            String json = gson.toJson(record);
            org.bson.Document doc = org.bson.Document.parse(json);
            try {
                earningManagerTable.insertOne(doc);
            }catch (MongoWriteException exception){
                earningManagerTable.updateOne(eq("_id", record.getSymbol()), new org.bson.Document("$set", doc));
            }
        }
    }

    /**
     * Update first if not exits then insert this document.
     * @param tableName
     */
    public synchronized  void updateDocument(String tableName, StockRecord stockRecord, String symbolValue, BasicDBObject updateFields){
        MongoCollection earningManagerTable = this.mongoDatabase.getCollection(tableName);
        BasicDBObject searchQuery = new BasicDBObject("_id", symbolValue);
        UpdateResult result = earningManagerTable.updateOne(searchQuery, updateFields);
        if (result.getMatchedCount() == 0){
            this.insertDocument(tableName, stockRecord);
        }
    }

    /**
     * Update first if not exits then insert this document.
     * @param tableName
     */
    public synchronized  void updateDocument(String tableName, StockRecord... stockRecords){
        MongoCollection earningManagerTable = this.mongoDatabase.getCollection(tableName);
        Gson gson = new Gson();

        for(StockRecord stockRecord : stockRecords){
            String json = gson.toJson(stockRecord);
            org.bson.Document doc = org.bson.Document.parse(json);
            UpdateResult result = earningManagerTable.updateOne(eq("symbol", stockRecord.getSymbol()),  new org.bson.Document("$set",doc));
            if (result.getMatchedCount() == 0 ){
                this.insertDocument(tableName, stockRecord);
            }
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
        Assert.assertTrue(newOrder.getType() != OrderType.UNKNOWN);

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
        if (newOrder.getType() == OrderType.BUY) {
            newSum += newOrder.getPrice() * newOrder.getShares();
            newShares += newOrder.getShares();
        } else {
            newSum -= newOrder.getPrice() * newOrder.getShares();
            newShares -= newOrder.getShares();
        }

        // Divisor check
        if (newShares == 0) {
            sharedStock.setShares(0);
            return sharedStock;
        }

        // Calculate the new average cost
        double newAveCost = Math.round(newSum * 100 / newShares) / 100;

        sharedStock.setShares(newShares);
        sharedStock.setSharedAverageCost(newAveCost);
        return sharedStock;
    }

    public List<StockRecord> retriveSharedStocksFromDB() throws java.lang.Exception {
        return this.retrieveDcouments(MongoDBManager.EARN_SHARE_TABLE_NAME, StockRecord.class);
    }
}
