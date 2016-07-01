
package src.PriceMonitor;

/**
 * Created by zhuoli on 7/1/16.
 */
public enum CurrencyEnum {
    BitCoin {
        public String toString() {
            return "btc";
        }
    },
    LiteCoin {
        public String toString() {
            return "ltc";
        }
    },
    DogeCoin {
        @Override
        public String toString() {
            return "doge";
        }
    }
}
