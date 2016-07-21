package src.PriceMonitor.stock;

import java.time.LocalDateTime;

/**
 * Created by zhuoli on 7/16/16.
 */
public class StockItem {

    public String Symbol;
    public String CompanyName;
    public LocalDateTime LastUpdateTime;
    public double Price;

    public StockItem() {

    }

    public StockItem(String symbol) {
        this.Symbol = symbol;
    }
}
