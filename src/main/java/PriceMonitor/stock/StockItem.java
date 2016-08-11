package PriceMonitor.stock;

import java.time.LocalDateTime;

/**
 * Created by zhuoli on 7/16/16.
 */
public class StockItem {

    // Stock Symbol
    public String Symbol;

    // Average buying cost
    public double AverageCost;

    // Number of share==
    public int Shares;

    // Last Update Time of Stock price
    public LocalDateTime LastUpdateTime;

    // Stock price
    public double Price;

    /**
     * StockItem constructor.
     *
     * @param symbol
     * @param averageCost: Should always be greater than zero
     * @param shares:      Should always be greater or equal to zero
     */
    public StockItem(String symbol, double averageCost, int shares) {
        this.Symbol = symbol;
        this.AverageCost = averageCost;
        this.Shares = shares;
    }
}
