package PriceMonitor.CryptoCurrency;

import org.json.JSONObject;
import PriceMonitor.PriceMonitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by zhuoli on 6/23/16.
 */

public class CryptoCurrencyPriceMonitor extends PriceMonitor {

    /*
    {
        "symbol":"btc",
        "position":"1",
        "name":"Bitcoin",
        "market_cap":
            {"usd":10027475766.5,"eur":9022221320.908377,"cny":66609984565.49058,"gbp":7478220684.810005,"cad":12991998702.108059,"rub":640046658667.9008,"hkd":77800948345.17758,"jpy":1029790676044.674,"aud":13465145146.150362,"btc":"15715150.0"},
        "price":
            {"usd":638.077,"eur":574.10978075,"cny":4238.584077519,"gbp":475.86059852100004,"cad":826.71808428,"rub":40728.001875329996,"hkd":4950.697151829,"jpy":65528.529861300005,"aud":856.8257475249999,"btc":"1.0"},
        "supply":"15715150",
        "volume":
            {"usd":119284000,"eur":107325779.00000001,"cny":792373433.148,"gbp":88958786.532,"cad":154549121.76,"rub":7613813028.36,"hkd":925497955.6680001,"jpy":12250097019.6,"aud":160177537.29999998,"btc":"186909.0"},
        "change":"1.06","timestamp":"1467265624.828"
        }
     */

    // Base URL for SOAP query
    static final String BASE_URL = "http://coinmarketcap-nexuist.rhcloud.com/api";

    public void Start() {

        double priceJson = this.GetPriceForCurrency(CurrencyEnum.LiteCoin);
        System.out.println(priceJson);
    }

    public double GetPriceForCurrency(CurrencyEnum currencyEnum) {
        String str = this.GetSOAPString(CryptoCurrencyPriceMonitor.BASE_URL + "/" + currencyEnum.toString());
        return this.GetPriceString(str);
    }

    /**
     * Get soap string from the given URL
     *
     * @param url : url of soap address
     * @return soap string
     */
    public String GetSOAPString(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

            connection.setRequestMethod("GET");
            connection.connect();
            int status = connection.getResponseCode();

            if (status != 200) {
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuffer response = new StringBuffer();
            String inputLine;
            while ((inputLine = reader.readLine()) != null) {
                response.append(inputLine);
            }
            return response.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * @param soapString : SOAP string
     * @return Price in USD
     */
    public double GetPriceString(String soapString) {

        JSONObject jsonObj = new JSONObject(soapString);
        return jsonObj.getJSONObject("price").getDouble("usd");
    }
}
