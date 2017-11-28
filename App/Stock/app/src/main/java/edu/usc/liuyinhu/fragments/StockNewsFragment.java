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

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import edu.usc.liuyinhu.R;
import edu.usc.liuyinhu.adapters.NewsAdapter;
import edu.usc.liuyinhu.interfaces.VolleyCallbackListener;
import edu.usc.liuyinhu.models.StockNews;
import edu.usc.liuyinhu.services.VolleyNetworkService;
import edu.usc.liuyinhu.util.DateConverter;

/**
 * important: need to add (android:layout_marginBottom="?attr/actionBarSize") to ViewPager
 * otherwise, last item in list view won't show completely because of the action bar
 * https://stackoverflow.com/questions/34449336/my-listview-doesnt-show-last-item      by Cedriga
 * Created by hlyin on 26/11/2017.
 */

public class StockNewsFragment  extends Fragment {
    private static final String TAG = "StockNewsFragment";
    private static final String ARG_STOCK_SYMBOL = "symbol";
    private static final String GET_NEWS = "GET_news";

    VolleyCallbackListener callbackListener = null;
    VolleyNetworkService volleyNetworkService = null;

    private String symbol;
    View rootView;
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

        //volley init
        initVolleyCallback(); //call back
        volleyNetworkService = new VolleyNetworkService(callbackListener, getContext());

        //else, create a new view
        rootView = inflater.inflate(R.layout.fragment_stock_details_news, container, false);
        this.symbol = getArguments().getString(ARG_STOCK_SYMBOL); //"AAPL"

        Log.i(TAG, this.symbol);

        //get news data
        requestNewsData(this.symbol);

        return rootView;
    }


    void initVolleyCallback(){
        callbackListener = new VolleyCallbackListener() {
            @Override
            public void notifySuccess(String requestType, Object response) {
                Log.d(TAG, "Volley requester " + requestType);
                Log.d(TAG, "Volley Stock News: " + response);
                if(requestType.equals(GET_NEWS)){
                    parseStockNews(response.toString(), 5); //parse news
                    updateListView();
                }
            }
            @Override
            public void notifyError(String requestType,VolleyError error) {
                Log.d(TAG, "Volley requester " + requestType);
                Log.d(TAG, "Volley: " + "That didn't work!");
            }
        };
    }


    private void updateListView(){
        lv_news = rootView.findViewById(R.id.lv_news);
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


    private void parseStockNews(String response, Integer limit){
        Log.i(TAG, response);
        newsList = new ArrayList<>();
        DateConverter dateConverter = new DateConverter();
        try {
            JSONArray jsonArray = new JSONArray(response.toString());
            for(int i = 0;i < limit; i++){
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
                Log.i(TAG, stockNews.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
