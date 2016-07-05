package src.PriceMonitor;

import com.joanzapata.utils.Strings;

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
    final String BASE_URL = "http://dev.markitondemand.com/MODApis/Api/v2/Quote/jsonp?symbol=AAPL&callback=hellworld";

    public double GetPrice(String symbol) {
        String url = this.ComposeUrl(symbol);
        String response = this.GetHttpResponse(url);
        return this.QuoteStockPrice(response);
    }

    public String ComposeUrl(String symbol) {

        return Strings.format("http://dev.markitondemand.com/MODApis/Api/v2/Quote/{type}?symbol={symbol}&callback=helloworld").with("type", "jsonp").with("symbol", symbol).build();
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

    public double QuoteStockPrice(String str) {

        return 0;
    }
}
