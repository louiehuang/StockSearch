package edu.usc.liuyinhu.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import java.util.Timer;
import java.util.TimerTask;

import edu.usc.liuyinhu.R;

/**
 * Created by hlyin on 26/11/2017.
 */

public class StockHistoricalFragment  extends Fragment {
    private static final String TAG = "StockHistoricalFragment";
    private static final String ARG_STOCK_SYMBOL = "symbol";

    private String symbol;
    View rootView;
    ProgressBar pb_loadingStockChart;
    WebView wv_historical;

    Timer timer;
    TimerTask timerTask;
    boolean isFinishLoading = false;
    boolean isErrorInFetching = false;

    public StockHistoricalFragment() { }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static StockHistoricalFragment newInstance(String symbol) {
        StockHistoricalFragment fragment = new StockHistoricalFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STOCK_SYMBOL, symbol);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //if view has already been created, just return it
        if(rootView != null)
            return rootView;

        //else, create a new view
        rootView = inflater.inflate(R.layout.fragment_stock_details_historical, container, false);
        this.symbol = getArguments().getString(ARG_STOCK_SYMBOL); //"AAPL"

        Log.i(TAG, "symbol: " + symbol);

        pb_loadingStockChart = rootView.findViewById(R.id.pb_loadingStockChart);
        wv_historical = rootView.findViewById(R.id.wv_historical);

//        initVisibility(); //no need for this specific task (only a web view, it has no view before finish loading)

        //create webView
        wv_historical.getSettings().setJavaScriptEnabled(true);


        //interface to get variable in JS, for facebook sharing
        wv_historical.addJavascriptInterface(new historicalChartJSInterface(), "AndroidGetHistoricalLoadingStatus");
        wv_historical.addJavascriptInterface(new historicalChartJSInterface(), "AndroidGetHistoricalErrorStatus");

        //get status
        timer = new Timer();
        timerTask = new TimerTask(){
            @Override
            public void run() {
//                Log.i(TAG, "running...");
                wv_historical.post(new Runnable() {
                    @Override
                    public void run() {
                        wv_historical.loadUrl("javascript:getChartLoadingStatus()"); //it's async!!!
                        wv_historical.loadUrl("javascript:getChartErrorStatus()"); //it's async!!!
                        if(isFinishLoading || isErrorInFetching){ //finish or error, terminate timer, hide progress bar
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateProgressBar();
                                }
                            });
                            timer.cancel();
                        }
                    }
                });
            }
        };

        wv_historical.loadUrl("file:///android_asset/historicalChart.html"); //create html
        //notice that loadUrl is async!!! so to call function, make sure all functions have been loaded
        wv_historical.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                wv_historical.loadUrl("javascript:interfaceInitiate('" + symbol + "')");
                if(timer != null) {
                    timer.schedule(timerTask, 0, 1000);
                }
            }
        });


        return rootView;
    }


    private void updateProgressBar(){
        pb_loadingStockChart.setVisibility(ProgressBar.INVISIBLE);
//        wv_historical.setVisibility(WebView.VISIBLE); //no need
    }

    /**
     * For JS, this is the interface to get variable from JS
     */
    class historicalChartJSInterface {
        /**
         * This method is also in JS, as interface, to fetch the status whether chart has been loaded(variable in JS)
         */
        @JavascriptInterface
        public void getLoadingStatus(boolean finishLoading){
            isFinishLoading = finishLoading;
        }

        @JavascriptInterface
        public void getErrorStatus(boolean errorLoading){
            isErrorInFetching = errorLoading;
        }
    }


}
