package edu.usc.liuyinhu.services;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.usc.liuyinhu.models.FavoriteStock;
import edu.usc.liuyinhu.models.StockName;
import edu.usc.liuyinhu.models.StockNews;
import edu.usc.liuyinhu.util.DateConverter;

/**
 * Created by hlyin on 29/11/2017.
 */

public class ParseService {


    /**
     * parse response to create stockNameList for auto complete
     * @param response auto complete potential stocks
     * @param limit how many items should be shown in the autoCompleteTextView
     * @return
     */
    public static List<StockName> parseStockNames(String response, Integer limit){
        List<StockName> stockNameList = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(response.toString());
            limit = limit < jsonArray.length() ? limit : jsonArray.length(); //make sure limit is valid
            if(limit <= 0)
                return null;
            for(int i = 0; i < limit; i++){
                JSONObject stock = jsonArray.getJSONObject(i);
                String stock_symbol = stock.getString("Symbol");
                String stock_name = stock.getString("Name");
                String stock_exchange = stock.getString("Exchange");
                StockName stockName = new StockName(stock_symbol, stock_name, stock_exchange);
                stockNameList.add(stockName);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return stockNameList;
    }

    /**
     * Used when updating FavoriteStock information (symbol, price, change, changePercent, time)
     * parse response(String) to get this stcok's new info, compare with older one to calculate change...
     * @param oldStockDetails stock details before updating
     * @param response stock's newer details
     * @return newStockDetails(with change and changePercent compared to oldStockDetails)
     */
    public static FavoriteStock parseStockDetailsAsFavoriteStock(FavoriteStock oldStockDetails, String response) {
        FavoriteStock newStockDetails = null;
        try {
            /******************** parse ********************/
            JSONObject stockDetailObj = new JSONObject(response);
            JSONObject jsonSeriesData = stockDetailObj.getJSONObject("Time Series (Daily)");
            String cur_key = null;  //first key, which is current date
            Iterator<String> keys = jsonSeriesData.keys();
            while(keys.hasNext()){
                cur_key = String.valueOf(keys.next());
                break;
            }

            JSONObject currentDateStockDate = jsonSeriesData.getJSONObject(cur_key);
            Double curOpen = Double.parseDouble(currentDateStockDate.getString("1. open"));
//            Double curClose = Double.parseDouble(currentDateStockDate.getString("4. close"));
            String curVolume = currentDateStockDate.getString("5. volume");
            Double changeNum = curOpen - oldStockDetails.getPrice();
            Double changePercent = (changeNum / oldStockDetails.getPrice()) * 100;

            //leave 2 decimal places, use 0 instead of # to make sure have the trailing 0's.
            DecimalFormat df = new DecimalFormat("0.00");
            df.setRoundingMode(RoundingMode.FLOOR);

            /******************** create current stock ********************/
            newStockDetails = new FavoriteStock(oldStockDetails.getSymbol(), Double.parseDouble(df.format(curOpen)),
                    Double.parseDouble(df.format(changeNum)), Double.parseDouble(df.format(changePercent))
                    , oldStockDetails.getAddTime()); //leave time
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return newStockDetails;
    }



    public static List<StockNews> parseStockNews(String response, Integer NEWS_ITEM_LIMIT){
        List<StockNews> newsList = new ArrayList<>();
        DateConverter dateConverter = new DateConverter();
        try {
            JSONArray jsonArray = new JSONArray(response);
            for(int i = 0;i < NEWS_ITEM_LIMIT; i++){
                JSONObject news = jsonArray.getJSONObject(i);
                String title = news.getString("title").trim(); //["Apple: Smart iPhone Decision"]
                String guid = news.getJSONArray("guid").getJSONObject(0).getString("_"); //https://seekingalpha.com/MarketCurrent:3314432
                String link = news.getString("link"); //["https:\/\/seekingalpha.com\/symbol\/AAPL\/news?source=feed_symbol_AAPL"]
                String date = news.getString("pubDate"); //["Mon, 27 Nov 2017 09:00:54 -0500"]
                String author = news.getString("sa:author_name"); //["Open Square Capital"]

                title = title.substring(2, title.length() - 2); //Apple: Smart iPhone Decision
                author = author.substring(2, author.length() - 2); //Open Square Capital

                //process date
                date = date.substring(2, date.length() - 2); //Mon, 27 Nov 2017 09:00:54 -0500
                date = dateConverter.convertDateToLA(date);  //Mon, 27 Nov 2017 06:00:54 PST

                //process guid
                guid = guid.substring(guid.length() - 7); //3314432

                //process link
                link = link.substring(2, link.length() - 2);
                if(!link.contains(guid)){
                    link = "https://seekingalpha.com/news/" + guid;
                    String conciseTitle = title.toLowerCase().replaceAll("\\s+", "-");
                    link += "-" + conciseTitle;
                }

                StockNews stockNews = new StockNews(title, author, guid, link, date);
                newsList.add(stockNews);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return newsList;
    }

}
