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
import android.widget.TextView;

import edu.usc.liuyinhu.R;
import edu.usc.liuyinhu.models.StockName;
import edu.usc.liuyinhu.services.AutoCompleteService;
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
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String ARG_STOCK_SYMBOL = "symbol";
    private String symbol;

    TextView textView;

    public StockCurrentFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static StockCurrentFragment newInstance(int sectionNumber, String symbol) {
        StockCurrentFragment fragment = new StockCurrentFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        args.putString(ARG_STOCK_SYMBOL, symbol);
        fragment.setArguments(args);
        return fragment;
    }

    //https://stackoverflow.com/questions/23333092/right-approach-to-call-web-serviceapi-from-fragment-class
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stock_details_current, container, false);
        this.symbol = getArguments().getString(ARG_STOCK_SYMBOL); //"AAPL"

        Log.i(TAG, "symbol: " + symbol);

        textView = rootView.findViewById(R.id.tv_test);
        requestData(this.symbol);

        return rootView;
    }


    private void requestData(String symbol) {
        Log.i(TAG, "request");

        AutoCompleteService webService =
                AutoCompleteService.retrofit.create(AutoCompleteService.class);
        Call<StockName[]> call = webService.stockNameItems(symbol);
        call.enqueue(new Callback<StockName[]>() {
            @Override
            public void onResponse(Call<StockName[]> call, Response<StockName[]> response) {
                StockName[] stockNameItems = response.body();
                Log.i(TAG, stockNameItems[0].toString());

                textView.setText(stockNameItems[0].toString());
            }
            @Override
            public void onFailure(Call<StockName[]> call, Throwable t) {
            }
        });
    }

}