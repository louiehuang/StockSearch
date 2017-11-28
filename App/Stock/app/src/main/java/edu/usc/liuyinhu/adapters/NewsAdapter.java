package edu.usc.liuyinhu.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import edu.usc.liuyinhu.R;
import edu.usc.liuyinhu.models.StockNews;

/**
 * Created by hlyin on 27/11/2017.
 */

public class NewsAdapter extends BaseAdapter {

    private static final String TAG = NewsAdapter.class.getSimpleName();
    ArrayList<StockNews> newsArray;

    public NewsAdapter() {
        newsArray = new ArrayList<>();
        newsArray.add(new StockNews("title1", "1", "louie", "2017-11-27"));
        newsArray.add(new StockNews("title2", "1", "louie", "2017-11-27"));
        newsArray.add(new StockNews("title3", "1", "louie", "2017-11-27"));
        newsArray.add(new StockNews("title4", "1", "louie", "2017-11-27"));
        newsArray.add(new StockNews("title5", "1", "louie", "2017-11-27"));
        newsArray.add(new StockNews("title6", "1", "louie", "2017-11-27"));
        newsArray.add(new StockNews("title7", "1", "louie", "2017-11-27"));
        newsArray.add(new StockNews("title8", "1", "louie", "2017-11-27"));
    }

    @Override
    public int getCount() {
        return newsArray.size();    // total number of elements in the list
    }

    @Override
    public Object getItem(int i) {
        return newsArray.get(i);    // single item in the list
    }

    @Override
    public long getItemId(int i) {
        return i;                   // index number
    }

    @Override
    public View getView(int index, View view, final ViewGroup parent) {
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            view = inflater.inflate(R.layout.news_list_item, parent, false);
        }

        final StockNews dataModel = newsArray.get(index);

        TextView tv_news_title = view.findViewById(R.id.tv_news_title);
        tv_news_title.setText(dataModel.getTitle());

        TextView tv_news_author = view.findViewById(R.id.tv_news_author);
        tv_news_author.setText("...");

        TextView tv_news_date = view.findViewById(R.id.tv_news_date);
        tv_news_date.setText(dataModel.getPubDate());

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(parent.getContext(), "view clicked: " + dataModel.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}