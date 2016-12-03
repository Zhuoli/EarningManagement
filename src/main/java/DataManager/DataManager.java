package DataManager;

import EmailManager.EmailManager;
import EmailManager.MonitorEmail;
import JooqORM.tables.Stock;
import JooqORM.tables.records.StockRecord;
import PriceMonitor.PriceMonitor;
import PriceMonitor.stock.StockItem;
import Utility.RetryManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
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
    protected Supplier<StockItem[]> getNewQueriedStockItemsFunc;

    protected EmailManager emailManager = EmailManager.GetAndInitializeEmailmanager("resourceConfig.xml");

    protected DataManager()
    {

    }

    /**
     * Constructor, create DATA_ROOT directory
     */
    public static DataManager GetDataManager(Consumer<StockRecord> stockItemRegister, Supplier<StockItem[]> getNewQueriedStockItemsFunc) throws IOException {
        try {
            DataManager manager = DatabaseManager.GetDatabaseManagerInstance("resourceConfig.xml").Authenticate();
            manager.stockItemRegister = stockItemRegister;
            manager.getNewQueriedStockItemsFunc = getNewQueriedStockItemsFunc;
            return manager;
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.SEVERE, "Failed on initialization database manager", e);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * To be override.
     * @return
     */
    public abstract List<StockRecord> ReadSharedStocks() throws Exception ;

    /**
     * To be override by the subclass.
     * @param orders
     */
    public abstract void WriteSharedStocks(Order[] orders) throws Exception ;


    // Start thread
    public void Start() {
        try {

            MonitorEmail[] seenRobinHoodEmails = emailManager.ReceiveEmailsFrom("notifications@robinhood.com", true);
            Order[] seenOrders = this.CheckForNewOrdersPlaced(seenRobinHoodEmails);
            this.WriteSharedStocks(seenOrders);

            while (true) {
                // Register stocks queried from database
                this.ReadSharedStocks().stream().forEach(stockItem -> this.stockItemRegister.accept(stockItem));
                MonitorEmail[] unseenRobinHoodEmails = emailManager.ReceiveEmailsFrom("notifications@robinhood.com", false);
                Order[] newOrders = this.CheckForNewOrdersPlaced(unseenRobinHoodEmails);

                this.WriteSharedStocks(newOrders);

                this.updateReportDateToDatabase();

                Thread.sleep(5 * 1000);
            }
        } catch (SQLException sqlexc) {
            Logger.getGlobal().log(Level.SEVERE, "SQL Exception", sqlexc);
            System.exit(1);
        } catch (Exception exc) {
            Logger.getGlobal().log(Level.SEVERE, "Price Prophet thread Interrupted", exc);
            System.exit(1);
        }
    }

    private void updateReportDateToDatabase() throws Exception{

        synchronized(PriceMonitor.stockPriceMap) {
            if(PriceMonitor.stockPriceMap.values().stream().anyMatch(stockitem -> stockitem.getEarningReportDate().isPresent()))
            {
                StockItem[] stockItems = PriceMonitor.stockPriceMap.values().stream().filter(stockItem -> stockItem.getEarningReportDate().isPresent()).toArray(size -> new StockItem[size]);
                this.writeReportDate(stockItems);
            }
        }

    }

    protected abstract void writeReportDate(StockItem[] stockItems) throws Exception;

    private static final String OrderToBuyString = "Your market order to buy";
    private static final String OrderToSellString = "Your market order to sell";

    /**
     * Query email box to see if has new stock order been placed.
     * @return orders.
     */
    private Order[] CheckForNewOrdersPlaced(MonitorEmail[] robinhoodEmails) {
        LinkedList<Order> orders = new LinkedList<>();
        try {
            // Execute each email from robinhood
            for (MonitorEmail email : robinhoodEmails) {
                Order order = new RetryManager<>(this::ParseEmail).Execute(email);
                if (order != null){
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
        if (email.Content.contains(DataManager.OrderToBuyString))
        {
            int index = email.Content.indexOf(DataManager.OrderToBuyString);
            orderType = OrderType.BUY;
            String sharesString = paragraph.substring(index + DataManager.OrderToBuyString.length(), paragraph.indexOf(" share")).trim();
            shares = Integer.parseInt(sharesString);
        }

        // Selling order
        else if(email.Content.contains(DataManager.OrderToSellString))
        {
            int index = email.Content.indexOf(DataManager.OrderToSellString);
            orderType = OrderType.SELL;
            String sharesString = paragraph.substring(index + DataManager.OrderToSellString.length(), paragraph.indexOf(" share")).trim();
            shares = Integer.parseInt(sharesString);
        }
        else{
            Logger.getGlobal().warning("Failed to parse order type : " + email.Content);
            return null;
        }

        // Getting Symbol
        int endindex = paragraph.indexOf("Your trade confirmation will be");

        paragraph = email.Content.substring(0, endindex);

        int symbolIndex = paragraph.indexOf("was executed at");
        String[] words = paragraph.substring(0, symbolIndex).trim().split(" ");
        String symbol = words[words.length-1];


        // Get price
        paragraph = paragraph.substring(paragraph.indexOf("of $"));
        double price = Double.parseDouble(paragraph.substring(4, paragraph.indexOf("on")).trim());
        return new Order(symbol, shares, price, orderType);
    }
}
