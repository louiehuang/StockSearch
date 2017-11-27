package edu.usc.liuyinhu.fragments;

/**
 * Created by hlyin on 26/11/2017.
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import edu.usc.liuyinhu.R;
import edu.usc.liuyinhu.services.StockPriceService;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A placeholder fragment containing a simple view.
 */
public class StockCurrentFragment extends Fragment {

    private static final String TAG = "StockCurrentFragment";

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_STOCK_SYMBOL = "symbol";

    private String symbol;
    private String selectedIndicator;

    View rootView;
    TextView textView;
    Spinner spinner_indicators;
    Button btn_change;
    WebView wv_indicator;


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

        textView = rootView.findViewById(R.id.tv_test);

        requestPrice(this.symbol);



        //spinner
        spinner_indicators = rootView.findViewById(R.id.spinner_indicators);
        String[] indicatorItems = new String[]{"Price", "SMA", "EMA", "STOCH", "MACD", "BBANDS", "RSI", "ADX", "CCI"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, indicatorItems);
        spinner_indicators.setAdapter(adapter);
        spinner_indicators.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                selectedIndicator = item;
                Toast.makeText(parent.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        btn_change = rootView.findViewById(R.id.btn_change);
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



        //For the chart data, you should call API using JS.
        //http://blog.csdn.net/carson_ho/article/details/64904691
        //https://stackoverflow.com/questions/23556794/pass-variables-from-android-activity-to-javascript

        //create webView
        wv_indicator = rootView.findViewById(R.id.wv_indicator);
        wv_indicator.getSettings().setJavaScriptEnabled(true);
        wv_indicator.loadUrl("file:///android_asset/currentChart.html"); //create html

        //notice that loadUrl is async!!! so to call function, make sure all functions have been loaded
        wv_indicator.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                wv_indicator.loadUrl("javascript:interfaceInitiate('" + symbol + "')");
            }
        });


        ImageButton imgBtn_fav = rootView.findViewById(R.id.imgBtn_fav);
        imgBtn_fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return rootView;
    }


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
                    JSONObject obj = new JSONObject(res);

                    textView.setText(obj.getJSONObject("Meta Data").getString("1. Information"));
                    Log.i(TAG, obj.getJSONObject("Meta Data").getString("1. Information"));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });
    }

}