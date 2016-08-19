package DataManager;

import org.json.JSONObject;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by zhuolil on 8/17/16.
 */
public class DataManager {


    static final public String SYMBOL = "Symbol";
    static final public String AVERAGECOST = "AverageCost";
    static final public String SHARES = "Shares";

    protected Consumer<JSONObject> stockItemRegister;

    protected DataManager()
    {

    }

    /**
     * Constructor, create DATA_ROOT directory
     */
    public static DataManager GetDataManager(Consumer<JSONObject> stockItemRegister) throws IOException {
        try {
            DatabaseManager manager = DatabaseManager.InitializeDatabaseManagerFromXML("resourceConfig.xml").Authenticate();
            manager.stockItemRegister = stockItemRegister;
            return manager;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return  new CSVDataManager(stockItemRegister);
    }

    public List<JSONObject> UpdateStockItemsIfHasNew()
    {
        try {
            throw new Exception("Not implemented.");
        } catch (Exception e) {
            Logger.getGlobal().log(Level.SEVERE, "Not implemented", e);
        }
        return new LinkedList<>();
    }


    // Start thread
    public void Start() {
        try {
            while (true) {
                this.UpdateStockItemsIfHasNew().stream().forEach(stockItem -> this.stockItemRegister.accept(stockItem));
                Thread.sleep(3 * 1000);
            }
        } catch (Exception exc) {
            Logger.getGlobal().severe("Price Prophet thread Interrupted: " + exc.getMessage());
        }
    }

}
