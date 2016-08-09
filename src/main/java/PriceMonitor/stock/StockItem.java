package PriceMonitor.stock;

import java.time.LocalDateTime;

/**
 * Created by zhuoli on 7/16/16.
 */
public class StockItem {

    public String Symbol;
    public double BuyingPrice;
    public int Number;
    public String CompanyName;
    public LocalDateTime LastUpdateTime;
    public double Price;

    public StockItem() {

    }

    public StockItem(String symbol, double buyingPrice, int number) {
        this.Symbol = symbol;
        this.BuyingPrice = buyingPrice;
        this.Number = number;
    }
}
