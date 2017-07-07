package com.zhuoli.earning.PriceMonitor.stock;

import com.joanzapata.utils.Strings;
import org.json.JSONArray;
import org.json.JSONObject;
import com.zhuoli.earning.PriceMonitor.PriceMonitor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by zhuoli on 7/4/16.
 */
public class StockPriceMonitor extends PriceMonitor {

    // Referrence: http://dev.markitondemand.com/MODApis/#stockquote
    // Alternative url: http://finance.google.com/finance/info?client=ig&q=NASDAQ:%20AAPL
    static final String BASE_URL = "http://dev.markitondemand.com/MODApis/Api/v2";

    /**
     * Get stock price for the given Symbol.
     *
     * @param symbol
     * @return price
     */
    public double GetPrice(String symbol) throws Exception {
        String url = this.ComposeQuoteUrl(symbol);
        String response = this.GetHttpResponse(url);

        try {

            JSONObject jsonObj = new JSONObject(response);
            return jsonObj.getDouble("LastPrice");
        } catch (Exception exc) {
            throw new Exception("Error on parsing price of url + " + url + " for soap string " + response);
        }
    }

    /**
     * Company name look up.
     *
     * @param symbol
     * @return company name
     */
    public String[] CompanyLookUp(String symbol) throws Exception {
        String url = this.ComposeLookupUrl(symbol);
        String soapString = this.GetHttpResponse(url);
        return this.GetCompanyName(soapString, symbol);
    }

    protected String ComposeQuoteUrl(String symbol) {

        return Strings.format("{baseUrl}/{action}/{type}?symbol={symbol}").with("baseUrl", StockPriceMonitor.BASE_URL).with("action", "Quote").with("type", "json").with("symbol", symbol).build();
    }

    protected String ComposeLookupUrl(String symbol) {
        return Strings.format("{baseUrl}/{action}/{type}?input={symbol}").with("baseUrl", StockPriceMonitor.BASE_URL).with("action", "Lookup").with("type", "json").with("symbol", symbol).build();
    }

    /**
     * Get soap string from the given URL
     *
     * @param url : url of soap address
     * @return soap string
     */
    protected String GetHttpResponse(String url) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

        connection.setRequestMethod("GET");
        connection.connect();
        int status = connection.getResponseCode();

        // Raise exception if status not 200
        if (status != 200)
            throw new Exception("URL status not 200 for status: " + status + " url: " + url);

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuffer response = new StringBuffer();
        String inputLine;
        while ((inputLine = reader.readLine()) != null) {
            response.append(inputLine);
        }
        return response.toString();
    }

    /**
     * Parse Markitondemand Company Lookup JSON.
     * E.g:
     * [{"Symbol":"NFLX","Name":"Netflix Inc","Exchange":"NASDAQ"}]
     *
     * @param soapString
     * @return
     */
    protected String[] GetCompanyName(String soapString, String symbol) throws Exception {
        JSONArray jsonArray = new JSONArray(soapString);
        List<String> nameList = new LinkedList<>();
        try {

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObj = jsonArray.getJSONObject(i);

                // Add to result if symbols match
                if (jsonObj.getString("Symbol").equals(symbol))
                    nameList.add(jsonObj.getString("Name"));
            }
            return nameList.toArray(new String[nameList.size()]);
        } catch (Exception exc) {
            throw new Exception("Error on parsing company name from soap string: " + soapString);
        }
    }
}
