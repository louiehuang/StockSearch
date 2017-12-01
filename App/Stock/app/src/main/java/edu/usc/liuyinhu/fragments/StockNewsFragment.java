package edu.usc.liuyinhu.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.VolleyError;

import java.util.List;

import edu.usc.liuyinhu.R;
import edu.usc.liuyinhu.adapters.NewsAdapter;
import edu.usc.liuyinhu.interfaces.ParamConfigurations;
import edu.usc.liuyinhu.interfaces.VolleyCallbackListener;
import edu.usc.liuyinhu.models.StockNews;
import edu.usc.liuyinhu.services.ParseService;
import edu.usc.liuyinhu.services.VolleyNetworkService;

/**
 * important: need to add (android:layout_marginBottom="?attr/actionBarSize") to ViewPager
 * otherwise, last item in list view won't show completely because of the action bar
 * https://stackoverflow.com/questions/34449336/my-listview-doesnt-show-last-item      by Cedriga
 * Created by hlyin on 26/11/2017.
 */

public class StockNewsFragment extends Fragment implements ParamConfigurations {
    private static final String TAG = "StockNewsFragment";

    VolleyCallbackListener callbackListener = null;
    VolleyNetworkService volleyNetworkService = null;

    private String symbol;
    View rootView;
    TextView tv_errorMsg;
    ProgressBar pb_loadingNews;
    ListView lv_news;
    List<StockNews> newsList;

    public StockNewsFragment() { }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static StockNewsFragment newInstance(String symbol) {
        StockNewsFragment fragment = new StockNewsFragment();
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
        rootView = inflater.inflate(R.layout.fragment_stock_details_news, container, false);
        this.symbol = getArguments().getString(ARG_STOCK_SYMBOL); //"AAPL"

        lv_news = rootView.findViewById(R.id.lv_news);
        tv_errorMsg = rootView.findViewById(R.id.tv_errorMsg);
        pb_loadingNews = rootView.findViewById(R.id.pb_loadingNews);
        pb_loadingNews.setVisibility(ProgressBar.VISIBLE);
        lv_news.setVisibility(ListView.GONE);
        tv_errorMsg.setVisibility(TextView.GONE);

        //volley init
        initVolleyCallback(); //call back
        volleyNetworkService = new VolleyNetworkService(callbackListener, getContext());

        Log.i(TAG, this.symbol);

        //get news data
        requestNewsData(this.symbol);

        return rootView;
    }


    void initVolleyCallback(){
        callbackListener = new VolleyCallbackListener() {
            @Override
            public void notifySuccess(String requestType, Object response) {
//                Log.d(TAG, "Volley requester " + requestType + ", Volley Stock News: " + response);
                if(requestType.equals(GET_NEWS)){
                    if(response.toString().contains("Error Message")){
                        //{"Error Message":"Invalid API call. Please retry or visit the documentation (https://www.alphavantage.co/documentation/) for TIME_SERIES_DAILY."}
                        handleError();
                    }else {
                        newsList = ParseService.parseStockNews(response.toString(), NEWS_ITEM_LIMIT); //parse news, NEWS_ITEM_LIMIT = 5
                        updateListView();
                        pb_loadingNews.setVisibility(ProgressBar.GONE);
                        lv_news.setVisibility(ListView.VISIBLE);
                    }
                }
            }
            @Override
            public void notifyError(String requestType,VolleyError error) {
                Log.d(TAG, "Volley requester " + requestType + " with Error: " + error.getMessage());
                handleError();
            }
        };
    }


    private void handleError(){
        tv_errorMsg.setVisibility(TextView.VISIBLE);
        lv_news.setVisibility(ListView.GONE);
        pb_loadingNews.setVisibility(ProgressBar.GONE);
    }

    private void updateListView(){
        NewsAdapter newsAdapter = new NewsAdapter(newsList);
        lv_news.setAdapter(newsAdapter);
        lv_news.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Uri uri = Uri.parse(newsList.get(position).getLink());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
    }

    private void requestNewsData(String symbol) {
        //http://localhost:3000/news?symbol=AAPL
        String feed = "news?symbol=" + symbol;
        volleyNetworkService.getDataAsString(GET_NEWS, feed);
    }
}
