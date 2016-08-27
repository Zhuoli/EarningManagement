package DataManager;

/**
 * Created by zhuoli on 8/24/16.
 */
public class Order {

    public String Symbol;

    public int Shares;

    public Double Price;

    public OrderType type;

    public Order(String symbol, int shares, Double price, OrderType type) {
        this.Symbol = symbol;
        this.Shares = shares;
        this.Price = price;
        this.type = type;
    }

    @Override
    public String toString() {
        return this.Symbol + "; " + this.Shares + "; " + this.Price + ";" + this.type;
    }
}
