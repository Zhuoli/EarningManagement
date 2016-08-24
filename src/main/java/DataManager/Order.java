package DataManager;

/**
 * Created by zhuoli on 8/24/16.
 */
public class Order {

    public String Symbol;

    public int Shares;

    public Double Price;

    public Order(String symbol, int shares, Double price) {
        this.Symbol = symbol;
        this.Shares = shares;
        this.Price = price;
    }

    @Override
    public String toString() {
        return this.Symbol + "; " + this.Shares + "; " + this.Price;
    }
}
