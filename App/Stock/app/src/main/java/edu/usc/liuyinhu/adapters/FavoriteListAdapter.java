package edu.usc.liuyinhu.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import edu.usc.liuyinhu.R;
import edu.usc.liuyinhu.models.FavoriteStock;

/**
 * Created by hlyin on 28/11/2017.
 */

public class FavoriteListAdapter extends BaseAdapter {

    private static final String TAG = FavoriteListAdapter.class.getSimpleName();
    List<FavoriteStock> favoriteStockList;

    public FavoriteListAdapter(List<FavoriteStock> favoriteStockList) {
        this.favoriteStockList = favoriteStockList;
    }

    @Override
    public int getCount() {
        return favoriteStockList.size();    // total number of elements in the list
    }

    @Override
    public Object getItem(int i) {
        return favoriteStockList.get(i);    // single item in the list
    }

    @Override
    public long getItemId(int i) {
        return i;                   // index number
    }

    @Override
    public View getView(int index, View view, final ViewGroup parent) {
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            view = inflater.inflate(R.layout.list_item_favorite_stock, parent, false);
        }

        final FavoriteStock dataModel = favoriteStockList.get(index);

        TextView tv_fav_stock_symbol = view.findViewById(R.id.tv_fav_stock_symbol);
        tv_fav_stock_symbol.setText(dataModel.getSymbol());

        TextView tv_fav_stock_price = view.findViewById(R.id.tv_fav_stock_price);
        String authorText = dataModel.getPrice().toString();
        tv_fav_stock_price.setText(authorText);

        TextView tv_fav_stock_change = view.findViewById(R.id.tv_fav_stock_change);
        String dateText = dataModel.getChange() + " (" + dataModel.getChangePercent() + "%)";
        tv_fav_stock_change.setText(dateText);

//        view.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Toast.makeText(parent.getContext(), "view clicked: " + dataModel.toString(), Toast.LENGTH_SHORT).show();
//            }
//        });

        return view;
    }
}