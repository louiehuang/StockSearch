package edu.usc.liuyinhu.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import edu.usc.liuyinhu.R;
import edu.usc.liuyinhu.models.StockNews;

/**
 * Created by hlyin on 27/11/2017.
 */

public class NewsAdapter extends BaseAdapter {

    private static final String TAG = NewsAdapter.class.getSimpleName();
    List<StockNews> newsArray;

    public NewsAdapter(List<StockNews> newsList) {
        this.newsArray = newsList;
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
            view = inflater.inflate(R.layout.list_item_stock_news, parent, false);
        }

        final StockNews dataModel = newsArray.get(index);

        TextView tv_news_title = view.findViewById(R.id.tv_news_title);
        tv_news_title.setText(dataModel.getTitle());

        TextView tv_news_author = view.findViewById(R.id.tv_news_author);
        String authorText = "Author: " + dataModel.getAuthor();
        tv_news_author.setText(authorText);

        TextView tv_news_date = view.findViewById(R.id.tv_news_date);
        String dateText = "Date: " + dataModel.getPubDate();
        tv_news_date.setText(dateText);

//        view.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //Toast.makeText(parent.getContext(), "view clicked: " + dataModel.toString(), Toast.LENGTH_SHORT).show();
//                String news_url = dataModel.getLink();
//                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(news_url));
//                startActivity(intent);
//            }
//        });

        return view;
    }
}