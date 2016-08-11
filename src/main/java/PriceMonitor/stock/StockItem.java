package PriceMonitor.stock;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Created by zhuoli on 7/16/16.
 */
public class StockItem {

    // Stock Symbol
    public String Symbol;

    // Average buying cost
    public double AverageCost;

    // Number of share
    public int Shares;

    // Last Update Time of Stock price
    public LocalDateTime LastUpdateTime;

    // Stock price
    public double Price;
    // Earning report date if it has
    private Optional<LocalDate> EarningReportDate;

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
        this.EarningReportDate = Optional.empty();
    }

    public Optional<LocalDate> getEarningReportDate() {
        return EarningReportDate;
    }

    // Set earning report date
    public void setEarningReportDate(LocalDate earningReportDate) {
        // Returns an Optional describing the specified value, if non-null, otherwise returns an empty Optional.
        Optional.ofNullable(earningReportDate);
    }
}
