
package src.PriceMonitor.CryptoCurrency;

/**
 * Created by zhuoli on 7/1/16.
 */
public enum CurrencyEnum {
    BitCoin {
        @Override
        public String toString() {
            return "btc";
        }
    },
    LiteCoin {
        @Override
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
