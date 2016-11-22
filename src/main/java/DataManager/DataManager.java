package DataManager;

import EmailManager.EmailManager;
import EmailManager.MonitorEmail;
import JooqMap.tables.records.SharedstockitemsRecord;
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
public class DataManager {


    protected Consumer<SharedstockitemsRecord> stockItemRegister;
    protected Supplier<StockItem[]> getNewQueriedStockItemsFunc;

    protected DataManager()
    {

    }

    /**
     * Constructor, create DATA_ROOT directory
     */
    public static DataManager GetDataManager(Consumer<SharedstockitemsRecord> stockItemRegister, Supplier<StockItem[]> getNewQueriedStockItemsFunc) throws IOException {
        try {
            DatabaseManager manager = DatabaseManager.GetDatabaseManagerInstance("resourceConfig.xml").Authenticate();
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
    public List<SharedstockitemsRecord> ReadSharedStocks() throws SQLException
    {
        throw new SQLException("Not implemented.");
    }

    /**
     * To be override.
     * @param orders
     */
    public void WriteSharedStocks(Order[] orders){

        try {
            throw new Exception("Not implemented.");
        } catch (Exception e) {
            Logger.getGlobal().log(Level.SEVERE, "Not implemented", e);
        }
    }


    // Start thread
    public void Start() {
        try {
            while (true) {
                // Register stocks queried from database
                this.ReadSharedStocks().stream().forEach(stockItem -> this.stockItemRegister.accept(stockItem));
                Order[] newOrders = this.CheckForNewOrdersPlaced();

                this.WriteSharedStocks(newOrders);

                Thread.sleep(5 * 1000);
            }
        } catch (SQLException sqlexc){
            Logger.getGlobal().log(Level.SEVERE, "SQL Exception", sqlexc);
            System.exit(1);
        } catch (Exception exc) {
            Logger.getGlobal().log(Level.SEVERE, "Price Prophet thread Interrupted", exc);
        }
    }

    /**
     * Query email box to see if has new stock order been placed.
     * @return orders.
     */
    private static final String OrderToBuyString = "Your market order to buy";
    private static final String OrderToSellString = "Your market order to sell";

    private Order[] CheckForNewOrdersPlaced() {
        LinkedList<Order> orders = new LinkedList<>();
        try {
            EmailManager emailManager = EmailManager.GetAndInitializeEmailmanager("resourceConfig.xml");
            MonitorEmail[] robinhoodEmails = emailManager.ReceiveEmailsFrom("notifications@robinhood.com", false);
            robinhoodEmails = Arrays.stream(robinhoodEmails).filter(email -> email.Subject.contains("Your order has been executed")).toArray(size -> new MonitorEmail[size]);

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
