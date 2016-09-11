package DataManager;

/**
 * Created by zhuolil on 8/26/16.
 */
public enum OrderType {
    BUY("BUY"),
    SELL("SELL"),
    UNKNOWN("Unknow");

    private final String type;

    OrderType(String type)
    {
        this.type = type;
    }

    @Override
    public String toString()
    {
        return this.type;
    }
}
