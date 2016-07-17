package src.DataManager;

/**
 * Created by zhuoli on 7/13/16.
 */
public class DataItem {
    public String Symbol;
    public double Price;
    public int Number;

    public DataItem(String symbol, double price, int number) {
        this.Symbol = symbol;
        this.Price = price;
        this.Number = number;
    }
}
