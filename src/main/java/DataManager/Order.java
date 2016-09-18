package DataManager;

/**
 * Created by zhuoli on 8/24/16.
 */
public class Order {

    /**
     * Stock Symbol
     */
    public String Symbol;

    /**
     * Numbers of shares.
     */
    public int Shares;

    /**
     * Order price.
     */
    public Double Price;

    /**
     * Is this order Buying or Selling.
     */
    public OrderType type;

    /**
     * Constructor.
     * @param symbol
     * @param shares
     * @param price
     * @param type
     */
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
