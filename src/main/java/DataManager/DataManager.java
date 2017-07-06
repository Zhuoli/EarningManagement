package DataManager;

import EmailManager.EmailManager;
import EmailManager.MonitorEmail;
import PriceMonitor.PriceMonitor;
import Utility.RetryManager;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by zhuolil on 8/17/16.
 */
public abstract class DataManager {


    /**
     * Stock Item Register method
     */
    protected Consumer<StockRecord> stockItemRegister;

    /**
     * Get ne queried stock items function
     */
    protected Supplier<StockRecord[]> getNewQueriedStockItemsFunc;

    protected EmailManager emailManager = EmailManager.GetAndInitializeEmailmanager("resourceConfig.xml");

    protected DataManager() {

    }

    /**
     * Constructor, create DATA_ROOT directory
     */
    public static Optional<DataManager> GetDataManager(Consumer<StockRecord> stockItemRegister, Supplier<StockRecord[]> getNewQueriedStockItemsFunc){
        try {
            DataManager manager = MongoDBManager.GetDatabaseManagerInstance("resourceConfig.xml").Authenticate();
            manager.stockItemRegister = stockItemRegister;
            manager.getNewQueriedStockItemsFunc = getNewQueriedStockItemsFunc;
            return Optional.of(manager);
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.SEVERE, "Failed on initialization database manager", e);
            e.printStackTrace();
        }
        return Optional.empty();
    }


    // Start thread
    public void Start() {
        try {

            MonitorEmail[] seenRobinHoodEmails = emailManager.ReceiveEmailsFrom("notifications@robinhood.com", true);
            Order[] seenOrders = this.CheckForNewOrdersPlaced(seenRobinHoodEmails);
            this.recordSharedStocks(seenOrders);

            int errorCount = 0;
            while (true) {
                try {
                    this.task();
                    errorCount = 0;
                } catch (Exception e) {
                    if (++errorCount == 3)
                        throw e;
                }
                Thread.sleep(20 * 1000);
            }
        } catch (SQLException sqlexc) {
            Logger.getGlobal().log(Level.SEVERE, "SQL Exception", sqlexc);
            System.exit(1);
        } catch (Exception exc) {
            Logger.getGlobal().log(Level.SEVERE, "Price Prophet thread Interrupted", exc);
            System.exit(1);
        }
    }

    private void task() throws Exception {
        // Write/Override shares from Database back to memory cache
        this.retriveSharedStocks().stream().forEach(stockItem -> this.stockItemRegister.accept(stockItem));

        // Check email for new orders
        MonitorEmail[] unseenRobinHoodEmails = emailManager.ReceiveEmailsFrom("notifications@robinhood.com", false);
        Order[] newOrders = this.CheckForNewOrdersPlaced(unseenRobinHoodEmails);

        this.recordSharedStocks(newOrders);

        this.updateReportDateToDatabase();

        this.updateCurrentPrice(this.getNewQueriedStockItemsFunc.get());

        this.updateHeartBeat();

    }

    public abstract void updateHeartBeat() throws Exception;

    /**
     * Get shared stocks from Database.
     *
     * @return
     */
    public abstract List<StockRecord> retriveSharedStocks() throws Exception;

    /**
     * Write shares back to memory cache and database.
     *
     * @param orders
     */
    public abstract void recordSharedStocks(Order[] orders) throws Exception;

    /**
     * Updates current price to database.
     *
     * @throws java.lang.Exception
     */
    public abstract void updateCurrentPrice(StockRecord[] stockRecords) throws java.lang.Exception;

    private void updateReportDateToDatabase() throws Exception {

        StockRecord[] stockRecords = this.getNewQueriedStockItemsFunc.get();
        if (Arrays.stream(stockRecords).anyMatch(stockitem -> stockitem.getReportDate() != null)) {
            StockRecord[] stockItems = PriceMonitor.stockPriceMap.values().stream().filter(stockItem -> stockItem.getReportDate() != null).toArray(size -> new StockRecord[size]);
            this.writeReportDate(stockItems);
        }
    }

    protected abstract void writeReportDate(StockRecord[] stockItems) throws Exception;

    private static final String OrderToBuyString = "Your market order to buy";
    private static final String OrderToSellString = "Your market order to sell";

    /**
     * Query email box to see if has new stock order been placed.
     *
     * @return orders.
     */
    private Order[] CheckForNewOrdersPlaced(MonitorEmail[] robinhoodEmails) {
        LinkedList<Order> orders = new LinkedList<>();
        try {
            // Execute each email from robinhood
            for (MonitorEmail email : robinhoodEmails) {
                Order order = new RetryManager<>(this::ParseEmail).Execute(email);
                if (order != null) {
                    orders.add(order);
                }
            }
            return orders.toArray(new Order[0]);
        } catch (Exception e) {
            Logger.getGlobal().log(Level.SEVERE, "Error on querrying email", e);
        }
        return new Order[0];
    }

    public Order ParseEmail(MonitorEmail email) {
        String paragraph = email.Content;
        int shares = 0;
        OrderType orderType = OrderType.BUY;

        // Buying order
        if (email.Content.contains(DataManager.OrderToBuyString)) {
            int index = email.Content.indexOf(DataManager.OrderToBuyString);
            orderType = OrderType.BUY;
            String sharesString = paragraph.substring(index + DataManager.OrderToBuyString.length(), paragraph.indexOf(" share")).trim();
            shares = Integer.parseInt(sharesString);
        }

        // Selling order
        else if (email.Content.contains(DataManager.OrderToSellString)) {
            int index = email.Content.indexOf(DataManager.OrderToSellString);
            orderType = OrderType.SELL;
            String sharesString = paragraph.substring(index + DataManager.OrderToSellString.length(), paragraph.indexOf(" share")).trim();
            shares = Integer.parseInt(sharesString);
        } else {
            Logger.getGlobal().warning("Failed to parse order type : " + email.Content);
            return null;
        }

        // Getting Symbol
        int endindex = paragraph.indexOf("Your trade confirmation will be");

        paragraph = email.Content.substring(0, endindex);

        int symbolIndex = paragraph.indexOf("was executed at");
        String[] words = paragraph.substring(0, symbolIndex).trim().split(" ");
        String symbol = words[words.length - 1];


        // Get price
        paragraph = paragraph.substring(paragraph.indexOf("of $"));
        double price = Double.parseDouble(paragraph.substring(4, paragraph.indexOf("on")).trim());
        return new Order(symbol, shares, price, orderType);
    }
}
