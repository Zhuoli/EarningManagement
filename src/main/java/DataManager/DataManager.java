package DataManager;

import EmailManager.EmailManager;
import EmailManager.MonitorEmail;
import JooqMap.tables.records.SharedstockitemsRecord;
import PriceMonitor.stock.StockItem;

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
            DatabaseManager manager = DatabaseManager.InitializeDatabaseManagerFromXML("resourceConfig.xml").Authenticate();
            manager.stockItemRegister = stockItemRegister;
            manager.getNewQueriedStockItemsFunc = getNewQueriedStockItemsFunc;
            return manager;
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.SEVERE, "Failed on initialization database manager", e);
            e.printStackTrace();
        }
        return null;
    }

    public List<SharedstockitemsRecord> ReadSharedStocksFromDB()
    {
        try {
            throw new Exception("Not implemented.");
        } catch (Exception e) {
            Logger.getGlobal().log(Level.SEVERE, "Not implemented", e);
        }
        return new LinkedList<>();
    }

    public void WriteStockItemBackToDB(Order order) throws SQLException {

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
                this.ReadSharedStocksFromDB().stream().forEach(stockItem -> this.stockItemRegister.accept(stockItem));
                Order[] newOrders = this.Help();


                // Check email recipient to update stock database
                Arrays.stream(newOrders).forEach(order -> {
                    try {
                        this.WriteStockItemBackToDB(order);
                    } catch (java.sql.SQLException exc) {
                        Logger.getGlobal().log(Level.SEVERE, "SQL ERROR on writing back", exc);
                    }
                });
                Thread.sleep(5 * 1000);
            }
        } catch (Exception exc) {
            Logger.getGlobal().log(Level.SEVERE, "Price Prophet thread Interrupted", exc);
        }
    }

    private Order[] Help() {
        LinkedList<Order> orders = new LinkedList<>();
        try {
            EmailManager emailManager = EmailManager.GetAndInitializeEmailmanager("resourceConfig.xml");
            MonitorEmail[] robinhoodEmails = emailManager.ReceiveEmailsFrom("zhuoliseattle@gmail.com", false);
            robinhoodEmails = Arrays.stream(robinhoodEmails).filter(email -> email.Subject.contains("Your order has been executed")).toArray(size -> new MonitorEmail[size]);
            for (MonitorEmail email : robinhoodEmails) {

                int index = email.Content.indexOf("Your market order to buy");
                int endindex = email.Content.indexOf("Your trade confirmation will be");
                String paragraph = email.Content.substring(index, endindex);

                String sharesString = paragraph.substring("Your market order to buy".length(), paragraph.indexOf(" shares of ")).trim();
                int shares = Integer.parseInt(sharesString);
                String symbol = paragraph.substring(paragraph.indexOf("shares of") + "shares of".length(), paragraph.indexOf(" was executed at")).trim();
                paragraph = paragraph.substring(paragraph.indexOf("of $"));
                double price = Double.parseDouble(paragraph.substring(4, paragraph.indexOf("on")));
                orders.add(new Order(symbol, shares, price));
                System.out.println(paragraph);
            }
            return orders.toArray(new Order[0]);
        } catch (Exception e) {
            Logger.getGlobal().log(Level.SEVERE, "Error on querrying email", e);
        }

        return new Order[0];
    }
}
