package DataManager;

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


    static final public String SYMBOL = "Symbol";
    static final public String AVERAGECOST = "AverageCost";
    static final public String EARNING_REPORT_DATETIME = "EarningReportDatetime";
    static final public String SHARES = "Shares";
    static final public String PRICE = "Price";
    static final public String OneYearTargetPrice = "OneYearTargetNasdaq";

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

    public void WriteStockItemBackToDB(StockItem item) throws SQLException {

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
                this.ReadSharedStocksFromDB().stream().forEach(stockItem -> this.stockItemRegister.accept(stockItem));
                Arrays.stream(this.getNewQueriedStockItemsFunc.get()).forEach(stockItem -> {
                    try {
                        this.WriteStockItemBackToDB(stockItem);
                    } catch (java.sql.SQLException exc) {
                        Logger.getGlobal().log(Level.SEVERE, "SQL ERROR on writing back", exc);
                    }
                });
                Thread.sleep(3 * 1000);
            }
        } catch (Exception exc) {
            Logger.getGlobal().log(Level.SEVERE, "Price Prophet thread Interrupted", exc);
        }
    }

}
