package edu.usc.liuyinhu.stocksearch;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import edu.usc.liuyinhu.R;
import edu.usc.liuyinhu.interfaces.VolleyCallbackListener;
import edu.usc.liuyinhu.models.StockName;
import edu.usc.liuyinhu.models.StockNews;
import edu.usc.liuyinhu.services.VolleyNetworkService;
import edu.usc.liuyinhu.util.DateConverter;


public class MainActivity extends AppCompatActivity{
    private static final String TAG = "MainActivity";
    private static final String GET_AUTOCOMPLETE = "GET_auto_complete";

    AutoCompleteTextView ac_stock_input;
    List<StockName> stockNameList;
    ArrayAdapter<StockName> acAdapter;
    VolleyCallbackListener callbackListener = null;
    VolleyNetworkService volleyNetworkService = null;
    boolean isComingFromSelect = false; //if actv's text changes because of select operation, do not call onTextChanged

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //test switch button
        configureSwitchButton();

        //volley init
        initVolleyCallback(); //call back
        volleyNetworkService = VolleyNetworkService.getInstance(callbackListener, MainActivity.this);

        //auto complete
        ac_stock_input = findViewById(R.id.ac_stock_input);
        ac_stock_input.setThreshold(1); //will start working from first character
        ac_stock_input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable editable) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                Log.i(TAG, "onTextChanged, isComingFromSelect: " + isComingFromSelect);
                if(isComingFromSelect){
                    isComingFromSelect = false;
                    return;
                }
                if(s.toString().trim().length() >= 1) {
                        requestAutoCompleteData(s.toString().trim().toUpperCase());
                }
            }
        });
        ac_stock_input.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View arg1, int position, long arg3) {
                isComingFromSelect = true;
                Object item = parent.getItemAtPosition(position);
                if (item instanceof StockName){
                    StockName stock = (StockName) item;
                    ac_stock_input.setText(stock.getSymbol());
//                    Log.i(TAG, "after select, isComingFromSelect: " + isComingFromSelect);
                }
            }
        });
    }



    private void configureSwitchButton() {
        //query btn
        Button btn_getQuote = findViewById(R.id.btn_getQuote);
        btn_getQuote.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), StockDetailsActivity.class);
                intent.putExtra("symbol", "AAPL");
                startActivity(intent);
            }
        });

        //test
        Button testBtn = findViewById(R.id.test_btn);
        testBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

//                requestAutoCompleteDataTest();
                requestNewsDataTest();


            }
        });
    }


    /*****************************volley*****************************/
    void initVolleyCallback(){
        callbackListener = new VolleyCallbackListener() {
            @Override
            public void notifySuccess(String requestType, Object response) {
                Log.d(TAG, "Volley requester " + requestType);
                Log.d(TAG, "Volley String: " + response);
                if(requestType.equals(GET_AUTOCOMPLETE)){
                    //parse autocomplete
                    parseStockNames(response.toString());
                    displayData();
                }else if(requestType.equals("GET_news")){
                    //parse news
                    parseStockNews(response.toString(), 5);
                }
            }
            @Override
            public void notifyError(String requestType,VolleyError error) {
                Log.d(TAG, "Volley requester " + requestType);
                Log.d(TAG, "Volley String: " + "That didn't work!");
            }
        };
    }



    private void requestAutoCompleteData(String symbol) {
        //http://10.0.2.2:3000/autocomplete?symbol=AAPL
        String feed = "autocomplete?symbol=" + symbol;
        volleyNetworkService.getDataAsString(GET_AUTOCOMPLETE, feed);
    }



    private void parseStockNames(String response){
        stockNameList = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(response.toString());
            for(int i = 0;i < jsonArray.length();i++){
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
    }


    private void displayData() {
        if (stockNameList != null) {
            acAdapter = new ArrayAdapter<>(
                    MainActivity.this, android.R.layout.simple_dropdown_item_1line, stockNameList);
            ac_stock_input.setAdapter(acAdapter);
            ac_stock_input.showDropDown(); //refresh
        }
    }




    private void requestAutoCompleteDataTest() {
        //http://10.0.2.2:3000/autocomplete?symbol=AAPL
        String feed = "autocomplete?symbol=AAPL";
        volleyNetworkService.getDataAsString(GET_AUTOCOMPLETE, feed);
    }


    private void requestNewsDataTest() {
        //http://localhost:3000/news?symbol=AAPL
        String feed = "news?symbol=AAPL";
        String GET_NEWS = "GET_news";
        volleyNetworkService.getDataAsString(GET_NEWS, feed);
    }



    private void parseStockNews(String response, Integer limit){
        DateConverter dateConverter = new DateConverter();

        try {
            JSONArray jsonArray = new JSONArray(response.toString());
            for(int i = 0;i < limit; i++){
                JSONObject news = jsonArray.getJSONObject(i);
                String title = news.getString("title").trim(); //["Apple: Smart iPhone Decision"]
                String guid = news.getJSONArray("guid").getJSONObject(0).getString("_");
                String link = news.getString("link"); //["https:\/\/seekingalpha.com\/symbol\/AAPL\/news?source=feed_symbol_AAPL"]
                String date = news.getString("pubDate"); //["Mon, 27 Nov 2017 09:00:54 -0500"]

                title = title.substring(2, title.length() - 2); //Apple: Smart iPhone Decision
                link = link.substring(2, link.length() - 2);
                date = date.substring(2, date.length() - 2); //Mon, 27 Nov 2017 09:00:54 -0500
                date = dateConverter.convertDateToLA(date);  //Mon, 27 Nov 2017 06:00:54 PST

                StockNews stockNews = new StockNews(title, guid, link, date);
                Log.i(TAG, stockNews.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }












    /*****************************retrofit, import retrofit2.Response;*****************************/
//    private void requestData(String symbol) {
//        Log.i(TAG, "request");
//
//        //type too fast will cause app crush, how to deal with it???
//        try {
//            sleep(300);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        AutoCompleteService webService =
//                AutoCompleteService.retrofit.create(AutoCompleteService.class);
//        Call<StockName[]> call = webService.stockNameItems(symbol);
//        call.enqueue(new Callback<StockName[]>() {
//            @Override
//            public void onResponse(Call<StockName[]> call, Response<StockName[]> response) {
//                StockName[] stockNameItems = response.body();
//                Toast.makeText(MainActivity.this,
//                        "Received " + stockNameItems.length + " items from service",
//                        Toast.LENGTH_SHORT).show();
//                stockNameList = Arrays.asList(stockNameItems);
//                displayData();
//                Log.i("MainActivity", stockNameItems[0].toString());
//            }
//            @Override
//            public void onFailure(Call<StockName[]> call, Throwable t) {
//            }
//        });
//    }


}
