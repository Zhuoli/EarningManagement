package src.PriceMonitor;

import com.joanzapata.utils.Strings;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by zhuoli on 7/4/16.
 */
public class StockPriceMonitor {

    // Referrence: http://dev.markitondemand.com/MODApis/#stockquote
    static final String BASE_URL = "http://dev.markitondemand.com/MODApis/Api/v2/Quote";

    public double GetPrice(String symbol) {
        String url = this.ComposeUrl(symbol);
        String response = this.GetHttpResponse(url);
        return this.ParseMarkitondemandPrice(response);
    }

    public String ComposeUrl(String symbol) {

        return Strings.format("{baseUrl}/{type}?symbol={symbol}").with("baseUrl", StockPriceMonitor.BASE_URL).with("type", "json").with("symbol", symbol).build();
    }

    /**
     * Get soap string from the given URL
     *
     * @param url : url of soap address
     * @return soap string
     */
    public String GetHttpResponse(String url) {
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
     * Parse Markitondemand JSON Price.
     * E.g:
     * hellworld({"Status":"SUCCESS","Name":"Apple Inc","Symbol":"AAPL","LastPrice":95.895,"Change":0.295000000000002,"ChangePercent":0.308577405857742,"Timestamp":"Fri Jul 1 15:59:00 UTC-04:00 2016","MSDate":42552.6659722222,"MarketCap":525257670375,"Volume":2562784,"ChangeYTD":105.26,"ChangePercentYTD":-8.89701691050732,"High":96.46,"Low":95.33,"Open":95.48})
     *
     * @param JSON String
     * @return Price.
     */
    protected double ParseMarkitondemandPrice(String soapString) {
        JSONObject jsonObj = new JSONObject(soapString);
        return jsonObj.getDouble("LastPrice");
    }
}
