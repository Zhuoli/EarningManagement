package com.zhuoli.earning.DataManager;

import com.zhuoli.earning.EmailManager.EmailManager;
import com.zhuoli.earning.EmailManager.MonitorEmail;
import com.zhuoli.earning.PriceMonitor.PriceMonitor;
import com.zhuoli.earning.Utility.RetryManager;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by zhuolil on 8/17/16.
 */
public abstract class DataManager {

    /**
     * Updates current price to database.
     *
     * @throws java.lang.Exception
     */

    private static final String OrderToBuyString = "Your market order to buy";
    private static final String OrderToSellString = "Your market order to sell";
    /**
     * Get ne queried stock items function
     */
    protected EmailManager emailManager = EmailManager.GetAndInitializeEmailmanager("resourceConfig.xml");

    protected DataManager() {
    }

    // Start thread
    public void Start() {
        try {

            // Write shares from Database back to memory cache
//            this.retriveSharedStocksFromDB().stream().forEach(stockItem -> PriceMonitor.RegisterStockSymboles(stockItem));

            int errorCount = 0;
            while (true) {
                try {

                    // Check email for new orders
                    MonitorEmail[] unseenRobinHoodEmails = emailManager.ReceiveEmailsFrom("notifications@robinhood.com", false);
                    Order[] newOrders = this.CheckForNewOrdersPlaced(unseenRobinHoodEmails);

                    this.writeNewOrders2DB(newOrders);
                    StockRecord[] needUpdatedStockRecords = null;
                    synchronized (PriceMonitor.stockPriceMap) {
                        needUpdatedStockRecords = PriceMonitor.stockPriceMap.values().stream().filter(record -> record.isHasUpdate()).toArray(StockRecord[]::new);
                    }
                    Arrays.stream(needUpdatedStockRecords).forEach(stockRecord -> stockRecord.setHasUpdate(false));
                    this.updateStockRecords(needUpdatedStockRecords);

                    this.updateHeartBeat();

                    errorCount = 0;
                } catch (Exception e) {
                    if (++errorCount == 3)
                        throw e;
                }
                Thread.sleep(20 * 1000);
            }
        }catch (Exception exc) {
            Logger.getGlobal().log(Level.SEVERE, "Price Prophet thread Interrupted", exc);
            System.exit(1);
        }
    }

    public abstract void updateStockRecords(StockRecord... stockRecords);

    public abstract void updateHeartBeat() throws Exception;

    /**
     * Get shared stocks from Database.
     *
     * @return
     */
    public abstract List<StockRecord> retriveSharedStocksFromDB() throws Exception;

    /**
     * Write shares back to memory cache and database.
     *
     * @param orders
     */
    public abstract void writeNewOrders2DB(Order[] orders) throws Exception;

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
