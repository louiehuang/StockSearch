package edu.usc.liuyinhu.fragments;

/**
 * Created by hlyin on 26/11/2017.
 */

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.facebook.CallbackManager;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import edu.usc.liuyinhu.R;
import edu.usc.liuyinhu.interfaces.ParamConfigurations;
import edu.usc.liuyinhu.interfaces.StockPriceService;
import edu.usc.liuyinhu.interfaces.VolleyCallbackListener;
import edu.usc.liuyinhu.models.FavoriteStock;
import edu.usc.liuyinhu.services.StorageService;
import edu.usc.liuyinhu.services.VolleyNetworkService;
import edu.usc.liuyinhu.util.DateConverter;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A placeholder fragment containing a simple view.
 */
public class StockCurrentFragment extends Fragment implements ParamConfigurations{
    private static final String TAG = "StockCurrentFragment";

    private String symbol;
    private String selectedIndicator;

    VolleyCallbackListener callbackListener = null;
    VolleyNetworkService volleyNetworkService = null;

    View rootView;

    TableLayout tableLayout_details;
    TextView tv_errorMsg; //initially, android:visibility = "gone"
    ProgressBar pb_loadingTable;

    /**** FB and Favorite ImageButton ****/
    ImageButton imgBtn_fb_share;
    String chartImgURL; //chart image url to share
    CallbackManager callbackManager;
    ShareDialog shareDialog;
    ImageButton imgBtn_fav;
    FavoriteStock currentStock = null; //current stock in FavoriteStock format (get ready to add to favorite list)
    boolean isInFavorite = false; //is current stock is in favorite stock list?
    List<FavoriteStock> favoriteStockList;
    StorageService storageService;
    /**** FB and Favorite ImageButton ****/

    /**** Table TextView ****/
    TextView tv_symbol; TextView tv_lastPrice; TextView tv_change; TextView tv_timestamp;
    TextView tv_open; TextView tv_close; TextView tv_dayRange; TextView tv_volume;
    /**** Table TextView  ****/

    /**** Spinner ****/
    Spinner spinner_indicators;
    Button btn_change;
    /**** Spinner ****/

    /**** WebView, chart ****/
    WebView wv_indicator;
    /**** WebView, chart ****/


    public StockCurrentFragment() { }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static StockCurrentFragment newInstance(String symbol) {
        StockCurrentFragment fragment = new StockCurrentFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STOCK_SYMBOL, symbol);
        fragment.setArguments(args);
        return fragment;
    }


    //https://stackoverflow.com/questions/23333092/right-approach-to-call-web-serviceapi-from-fragment-class
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //if view has already been created, just return it
        if(rootView != null)
            return rootView;

        //else, create a new view
        rootView = inflater.inflate(R.layout.fragment_stock_details_current, container, false);
        this.symbol = getArguments().getString(ARG_STOCK_SYMBOL); //"AAPL"

        Log.i(TAG, "symbol: " + symbol);

        /*************************** Volley Init ************************/
        initVolleyCallback(); //call back
        volleyNetworkService = new VolleyNetworkService(callbackListener, getContext());

        /*************************** TextView findViewById ************************/
        initAllTextView();

        /**************************** FB and Favorite ImageButton ****************************/
        initFacebook();
        configureShareAndFav();
        /**************************** FB and Favorite ImageButton ****************************/

        /**************************** Spinner ****************************/
        configureSpinnerBar();
        /**************************** Spinner ****************************/

        /**************************** WebView ****************************/
        configureWebView();
        /**************************** WebView ****************************/


        /**************************** ProgressBar ****************************/
        configureProgressBar();
        /**************************** ProgressBar ****************************/

        /*************************** Request Data ************************/
        requestStockDetails(this.symbol); //volley, timeout=15s
        /*************************** Request Data ************************/

        return rootView;
    }


    private void configureProgressBar() {
        pb_loadingTable = rootView.findViewById(R.id.pb_loadingTable);
        pb_loadingTable.setVisibility(ProgressBar.VISIBLE);
        tableLayout_details.setVisibility(TableLayout.INVISIBLE);
    }

    private void updateProgressBar(){
        pb_loadingTable.setVisibility(ProgressBar.INVISIBLE);
        tableLayout_details.setVisibility(TableLayout.VISIBLE);
    }



    /********************************** FB Sharing **********************************/
    private void initFacebook() {
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);
    }

    /**
     * For JS, this is the interface to get variable from JS
     */
    class IndicatorChartJSInterface {
        /**
         * This method is also in JS, as interface, to fetch chartImgURL(variable in JS)
         */
        @JavascriptInterface
        public void getImageURL(String chartURL){
            chartImgURL = chartURL;
            postToFacebook(); //post to facebook
        }
    }

    /**
     * http://www.jianshu.com/p/0085a0e28e2b
     */
    private void postToFacebook(){
        if(chartImgURL == null || chartImgURL.length() == 0){
            Toast.makeText(getContext(), "No data, cannot post", Toast.LENGTH_LONG).show();
            return;
        }else if(chartImgURL.contains("Too many requests")){
            Toast.makeText(getContext(), "Too many requests, you have been rate limited", Toast.LENGTH_LONG).show();
        }
        Log.i(TAG, chartImgURL);
        if (ShareDialog.canShow(ShareLinkContent.class)) {
            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                    .setContentUrl(Uri.parse(chartImgURL))
                    .build();
            shareDialog.show(linkContent);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //coming back from FB sharing page
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
    /********************************** FB Sharing **********************************/


    //https://stackoverflow.com/questions/22984696/storing-array-list-object-in-sharedpreferences
    private void configureShareAndFav() {
        /**************************** FB ImageButton ****************************/
        imgBtn_fb_share = rootView.findViewById(R.id.imgBtn_fb_share);
        imgBtn_fb_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentStock == null){
                    Toast.makeText(getContext(), "No stock data, cannot post", Toast.LENGTH_LONG).show();
                    return;
                }
                //get chart url from js and post current chart in interface getImageURL()
                wv_indicator.loadUrl("javascript:getChartImageURL()"); //it's async!!!
            }
        });


        /**************************** Favorite ImageButton ****************************/
        storageService = StorageService.getInstance(getContext()); //get storage instance
        imgBtn_fav = rootView.findViewById(R.id.imgBtn_fav);
        //fetch favoriteStockList
        favoriteStockList = storageService.getFavoriteStockList("favoriteStockList");
        if(favoriteStockList == null) //if no list
            favoriteStockList = new ArrayList<>();

        //check whether it's already in favorite list
        if(isStockInFavoriteList())
            isInFavorite = true;
        else
            isInFavorite = false;

//        Log.i(TAG, "init, is in favoriteStockList: " + isInFavorite);
        updateFavoriteButtonImage(); //change image
        imgBtn_fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentStock == null){
                    Toast.makeText(getContext(), "No stock data, cannot add", Toast.LENGTH_LONG).show();
                    return;
                }

                //update favorite list
                if(isInFavorite){
                    removeCurrentStockToFavList(); //delete
                }else{
                    //modify current stock object, add time property
                    currentStock.setAddTime(new Date().getTime());
                    addCurrentStockToFavList(); //add
                }

                //update stock status
                isInFavorite = !isInFavorite;
                updateFavoriteButtonImage();

                //store updated list
                storageService.setFavoriteStockList("favoriteStockList", favoriteStockList);

                //test read new favoriteStockList
                List<FavoriteStock> readList = storageService.getFavoriteStockList("favoriteStockList");
                Log.i(TAG, readList.toString());
            }
        });
    }

    private void removeCurrentStockToFavList() {
        List<FavoriteStock> updatedList = new ArrayList<>();
        for(FavoriteStock favoriteStock : favoriteStockList){
            if(favoriteStock.getSymbol().equals(this.symbol)){
                continue;
            }
            updatedList.add(favoriteStock);
        }
        this.favoriteStockList = updatedList;
    }

    private void addCurrentStockToFavList() {
        favoriteStockList.add(currentStock);
    }

    private void updateFavoriteButtonImage() {
        if(isInFavorite){
            imgBtn_fav.setImageResource(R.drawable.star_filled);
        }else{
            imgBtn_fav.setImageResource(R.drawable.star_empty);
        }
    }

    /**
     * Init all TextViews
     */
    private void initAllTextView() {
        tableLayout_details = rootView.findViewById(R.id.tableLayout_details);
        tv_errorMsg = rootView.findViewById(R.id.tv_errorMsg);
        tv_symbol = rootView.findViewById(R.id.tv_symbol);
        tv_lastPrice = rootView.findViewById(R.id.tv_lastPrice);
        tv_change = rootView.findViewById(R.id.tv_change);
        tv_timestamp = rootView.findViewById(R.id.tv_timestamp);
        tv_open = rootView.findViewById(R.id.tv_open);
        tv_close = rootView.findViewById(R.id.tv_close);
        tv_dayRange = rootView.findViewById(R.id.tv_dayRange);
        tv_volume = rootView.findViewById(R.id.tv_volume);

        tableLayout_details.setVisibility(TableLayout.VISIBLE);
        tv_errorMsg.setVisibility(TextView.INVISIBLE);
    }


    /**
     * Init spinner and change Button
     */
    private void configureSpinnerBar() {
        spinner_indicators = rootView.findViewById(R.id.spinner_indicators);
        btn_change = rootView.findViewById(R.id.btn_change);
        String[] indicatorItems = new String[]{"Price", "SMA", "EMA", "STOCH", "MACD", "BBANDS", "RSI", "ADX", "CCI"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, indicatorItems);
        spinner_indicators.setAdapter(adapter);
        spinner_indicators.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                selectedIndicator = item;
//                Toast.makeText(parent.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
        btn_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "selected: " + selectedIndicator);
                //have to use another thread to call js function
                wv_indicator.post(new Runnable() {
                    @Override
                    public void run() {
                        wv_indicator.loadUrl("javascript:interfaceCreateChart('" + selectedIndicator +"')");
                    }
                });
            }
        });
    }

    /**
     * init WebView <br/>
     * reference: http://blog.csdn.net/carson_ho/article/details/64904691 <br/>
     * reference: https://stackoverflow.com/questions/23556794/pass-variables-from-android-activity-to-javascript
     */
    @SuppressLint("SetJavaScriptEnabled")
    private void configureWebView() {
        wv_indicator = rootView.findViewById(R.id.wv_indicator);
        //For the chart data, you should call API using JS.
        wv_indicator.getSettings().setJavaScriptEnabled(true);

        //interface to get variable in JS, for facebook sharing
        wv_indicator.addJavascriptInterface(new IndicatorChartJSInterface(), "AndroidGetChart");

        wv_indicator.loadUrl("file:///android_asset/currentChart.html"); //create html
        //notice that loadUrl is async!!! so to call function, make sure all functions have been loaded
        wv_indicator.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                wv_indicator.loadUrl("javascript:interfaceInitiate('" + symbol + "')");
            }
        });
    }


    /**
     * request stock details, call this method when creating fragment
     * @param symbol
     */
    private void requestStockDetails(String symbol) {
        //http://localhost:3000/price?symbol=AAPL
        String feed = "price?symbol=" + symbol;
        volleyNetworkService.getDataAsString(GET_STOCK_DETAILS, feed);
    }


    /**
     * call back, update stock details table
     */
    void initVolleyCallback(){
        callbackListener = new VolleyCallbackListener() {
            @Override
            public void notifySuccess(String requestType, Object response) {
//                Log.d(TAG, "Volley Stock Details: " + response);
                if(requestType.equals(GET_STOCK_DETAILS)){
                    updateStockDetailsTable(response.toString()); //parse and update, and create currentStock
                    updateProgressBar();
                }
            }
            @Override
            public void notifyError(String requestType,VolleyError error) {
                Log.d(TAG, "Volley Error: " + error.getMessage());
                tableLayout_details.setVisibility(TableLayout.INVISIBLE);
                pb_loadingTable.setVisibility(ProgressBar.INVISIBLE);
                tv_errorMsg.setVisibility(TextView.VISIBLE);
                currentStock = null;
            }
        };
    }

    /**
     * parse response String to update stock table
     * @param response
     */
    private void updateStockDetailsTable(String response) {
        try {
            if(null == response)
                return;

            /******************** parse ********************/
            JSONObject stockDetailObj = new JSONObject(response);

            JSONObject jsonMetaData = stockDetailObj.getJSONObject("Meta Data");
            String lastRefreshedTime = jsonMetaData.getString("3. Last Refreshed");
            if(lastRefreshedTime.length() <= 12){ //"2017-11-07"
                lastRefreshedTime += " 16:00:00"; //if closed, "2017-11-07 16:00:00"
            }
            String timezone = jsonMetaData.getString("5. Time Zone");
            lastRefreshedTime = DateConverter.convertDateWithTimeZone(lastRefreshedTime, timezone); //"2017-11-07 16:00:00" EST

            JSONObject jsonSeriesData = stockDetailObj.getJSONObject("Time Series (Daily)");
            String cur_key = null;  //first key, which is current date
            String prev_key = null; //second key, which is previous date
            Iterator<String> keys = jsonSeriesData.keys();
            int cnt = 0;
            while(keys.hasNext()){
                if(cnt == 0) cur_key = String.valueOf(keys.next());
                else if(cnt == 1) prev_key = String.valueOf(keys.next());
                else break;
                cnt++;
            }

            JSONObject currentDateStockDate = jsonSeriesData.getJSONObject(cur_key);
            JSONObject previousDateStockDate = jsonSeriesData.getJSONObject(prev_key);

            Double curOpen = Double.parseDouble(currentDateStockDate.getString("1. open"));
            Double curLow = Double.parseDouble(currentDateStockDate.getString("2. high"));
            Double curHigh = Double.parseDouble(currentDateStockDate.getString("3. low"));
            Double curClose = Double.parseDouble(currentDateStockDate.getString("4. close"));
            String curVolume = currentDateStockDate.getString("5. volume");
            String curRange = curLow + " - " + curHigh;

            Double prevClose = Double.parseDouble(previousDateStockDate.getString("4. close"));
            Double changeNum = curClose - prevClose;
            Double changePercent = (changeNum / prevClose) * 100;

            //leave 2 decimal places, use 0 instead of # to make sure have the trailing 0's.
            DecimalFormat df = new DecimalFormat("0.00");
            df.setRoundingMode(RoundingMode.FLOOR);
            String textChange = df.format(changeNum) + " (" + df.format(changePercent) + ")";

            /******************** update ********************/
            tv_symbol.setText(this.symbol);
            tv_lastPrice.setText(df.format(prevClose));
            tv_change.setText(textChange);
            tv_timestamp.setText(lastRefreshedTime);
            tv_open.setText(df.format(curOpen));
            tv_close.setText(df.format(curClose));
            tv_dayRange.setText(curRange);
            tv_volume.setText(curVolume);

            /******************** create current stock ********************/
            currentStock = new FavoriteStock(symbol, Double.parseDouble(df.format(curClose)),
                    Double.parseDouble(df.format(changeNum)), Double.parseDouble(df.format(changePercent))); //leave time

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * check whether currentStock is already in favoriteStockList
     * @return
     */
    private boolean isStockInFavoriteList(){
        if(favoriteStockList == null || favoriteStockList.size() == 0)
            return false;
        for(FavoriteStock favoriteStock : favoriteStockList){
//            Log.i(TAG, "list symbol: " + favoriteStock.getSymbol() + ", symbol: " + this.symbol);
            if(favoriteStock.getSymbol().equals(this.symbol))
                return true;
        }
        return false;
    }




    /**************************** Retrofit ****************************/
    private void requestPrice(String symbol) {
        Log.i(TAG, "requestPrice");
        StockPriceService webService =
                StockPriceService.retrofit.create(StockPriceService.class);
        Call<ResponseBody> call = webService.stockPrice(symbol);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> rawResponse) {
                try {
                    String res = rawResponse.body().string();
                    Log.i(TAG, "response: " + res);
                    updateStockDetailsTable(res); //parse and update, and create currentStock
                    updateProgressBar();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.i(TAG, "error requesting price， " + t.getMessage());
            }
        });
    }

}