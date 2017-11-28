package edu.usc.liuyinhu.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import edu.usc.liuyinhu.R;
import edu.usc.liuyinhu.adapters.NewsAdapter;

/**
 * important: need to add (android:layout_marginBottom="?attr/actionBarSize") to ViewPager
 * otherwise, last item in list view won't show completely because of the action bar
 * https://stackoverflow.com/questions/34449336/my-listview-doesnt-show-last-item      by Cedriga
 * Created by hlyin on 26/11/2017.
 */

public class StockNewsFragment  extends Fragment {
    private static final String TAG = "StockHistoricalFragment";
    private static final String ARG_STOCK_SYMBOL = "symbol";

    private String symbol;
    View rootView;
    ListView lv_news;


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

        //listView
        lv_news = rootView.findViewById(R.id.lv_news);
        NewsAdapter newsAdapter = new NewsAdapter();
        lv_news.setAdapter(newsAdapter);
//        lv_news.setScrollContainer(true);

        return rootView;
    }





}
