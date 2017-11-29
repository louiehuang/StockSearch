package edu.usc.liuyinhu.stocksearch;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import edu.usc.liuyinhu.R;
import edu.usc.liuyinhu.adapters.FavoriteListAdapter;
import edu.usc.liuyinhu.interfaces.VolleyCallbackListener;
import edu.usc.liuyinhu.models.FavoriteStock;
import edu.usc.liuyinhu.models.StockName;
import edu.usc.liuyinhu.services.StorageService;
import edu.usc.liuyinhu.services.VolleyNetworkService;
import edu.usc.liuyinhu.util.FavoriteStockComparator;


public class MainActivity extends AppCompatActivity{
    private static final String TAG = "MainActivity";
    private static final String GET_AUTOCOMPLETE = "GET_auto_complete";

    private String symbol; //user input symbol

    AutoCompleteTextView ac_stock_input;
    Integer autoCompleteLimit = 5;

    List<StockName> stockNameList;
    ArrayAdapter<StockName> acAdapter;
    VolleyCallbackListener callbackListener = null;
    VolleyNetworkService volleyNetworkService = null;
    boolean isComingFromSelect = false; //if actv's text changes because of select operation, do not call onTextChanged


    Switch switch_auto_refresh;
    ImageButton imgBtn_manual_refresh;

    Spinner spinner_sortBy;
    Spinner spinner_orderRule;
    private static final String SORT_DEFAULT = "Default";
    private static final String SORT_SYMBOL = "Symbol";
    private static final String SORT_PRICE = "Price";
    private static final String SORT_CHANGE = "Change";
    private static final String ORDER_ASC = "Ascending";
    private static final String ORDER_DESC = "Descending";
    HashMap<String, Integer> sortingMap; //"Symbol" => 1, key is sorting field, value is the position in spinner
    String selectedSortField = SORT_DEFAULT; //TIME
    HashMap<String, Integer> orderingMap; //"Ascending" => 1, key is ordering field, value is the position in spinner
    String selectedOrderRule = ORDER_ASC;

    ListView lv_favorite;
    List<FavoriteStock> favoriteStockList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        configureQueryButton(); //configure query and clear button

        configureAutoComplete(); //auto complete

        configureRefresh(); //refresh switch and image button

        configureSortingSpinner(); //sort and order spinner

        configureFavoriteStockList(); //favorite stock list view
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateFavoriteList();
    }

    /***************************** Button, Get Quote and Clear  *****************************/
    private void configureQueryButton() {
        //query btn
        Button btn_getQuote = findViewById(R.id.btn_getQuote);
        btn_getQuote.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(!isInputValidate()) //validation check
                    return;
                Intent intent = new Intent(getBaseContext(), StockDetailsActivity.class);
                intent.putExtra("symbol", symbol);
                startActivity(intent);
            }
        });
    }
    private boolean isInputValidate(){
        if(null == symbol || symbol.length() == 0)
            return false;

        int length = symbol.replaceAll("\\s+", "").length();
        return (length > 0);
    }
    /***************************** Button, Get Quote and Clear  *****************************/




    /***************************** AutoCompleteTextView, Using Volley*****************************/
    private void configureAutoComplete() {
        //volley init
        initVolleyCallback(); //call back
        volleyNetworkService = VolleyNetworkService.getInstance(callbackListener, MainActivity.this);

        //auto complete
        ac_stock_input = findViewById(R.id.ac_stock_input);
        ac_stock_input.setThreshold(1); //will start working from first character
        ac_stock_input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable editable) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                Log.i(TAG, "onTextChanged, isComingFromSelect: " + isComingFromSelect);
                if(isComingFromSelect){
                    isComingFromSelect = false;
                    return;
                }
                if(s.toString().trim().length() >= 1) {
                    symbol = s.toString().trim().toUpperCase();

                    //This is when click auto complete item, text now is like: "AAPL - APPLE INC (NASDAQ)"
                    if(symbol.contains(" - ")) //do not send request for this finished text
                        return;
                    requestAutoCompleteData(symbol);
                }
            }
        });
        ac_stock_input.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View arg1, int position, long arg3) {
                isComingFromSelect = true;
                Object item = parent.getItemAtPosition(position);
                if (item instanceof StockName){
                    StockName stock = (StockName) item;
                    symbol = stock.getSymbol();
//                    Log.i(TAG, "after select, isComingFromSelect: " + isComingFromSelect);
                }
            }
        });
    }

    void initVolleyCallback(){
        callbackListener = new VolleyCallbackListener() {
            @Override
            public void notifySuccess(String requestType, Object response) {
                Log.d(TAG, "Volley requester " + requestType);
                Log.d(TAG, "Volley String: " + response);
                //process auto complete returned data
                if(requestType.equals(GET_AUTOCOMPLETE) && null != response){
                    //parse autocomplete
                    parseStockNames(response.toString(), autoCompleteLimit);
                    displayData();
                }
            }
            @Override
            public void notifyError(String requestType,VolleyError error) {
                Log.d(TAG, "Volley requester " + requestType);
                Log.d(TAG, "Volley String: " + "That didn't work!");
            }
        };
    }

    /**
     * first requestAutoCompleteData, then date returned, to callbackListener => notifySuccess()
     * @param symbol
     */
    private void requestAutoCompleteData(String symbol) {
        //http://10.0.2.2:3000/autocomplete?symbol=AAPL
        String feed = "autocomplete?symbol=" + symbol;
        volleyNetworkService.getDataAsString(GET_AUTOCOMPLETE, feed);
    }

    private void parseStockNames(String response, Integer limit){
        stockNameList = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(response.toString());
            limit = limit < jsonArray.length() ? limit : jsonArray.length(); //make sure limit is valid
            if(limit == 0)
                return;
            for(int i = 0; i < limit; i++){
                JSONObject stock = jsonArray.getJSONObject(i);
                String stock_symbol = stock.getString("Symbol");
                String stock_name = stock.getString("Name");
                String stock_exchange = stock.getString("Exchange");
                StockName stockName = new StockName(stock_symbol, stock_name, stock_exchange);
                stockNameList.add(stockName);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void displayData() {
        if (stockNameList != null) {
            acAdapter = new ArrayAdapter<>(
                    MainActivity.this, android.R.layout.simple_dropdown_item_1line, stockNameList);
            ac_stock_input.setAdapter(acAdapter);
            ac_stock_input.showDropDown(); //refresh
        }
    }
    /***************************** AutoCompleteTextView, Using Volley*****************************/





    /***************************** Switch and ImageButton, Auto Refresh and Manual Refresh *****************************/
    private void configureRefresh() {

    }
    /***************************** Switch and ImageButton, Auto Refresh and Manual Refresh *****************************/





    /***************************** Spinner, for Sorting Favorite List *****************************/
    private void configureSortingSpinner() {
        /********** Sorting Field **********/
        spinner_sortBy = findViewById(R.id.spinner_sortBy);
        String[] sortingFields = new String[]{"Sort by", SORT_DEFAULT, SORT_SYMBOL, SORT_PRICE, SORT_CHANGE};
        sortingMap = new HashMap<>(); //<sortingFields, position>
        for(int i = 0; i < sortingFields.length; i++)
            sortingMap.put(sortingFields[i], i);

        ArrayAdapter<String> sortingAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_dropdown_item, sortingFields){
            @Override
            public boolean isEnabled(int position) {
                //position != 0 && position != current Selected Item
                return !(position == 0 || position == sortingMap.get(selectedSortField));
            }
            @Override
            public boolean areAllItemsEnabled() {
                return false;
            }
            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                //position == 0 || position == current Selected Item
                if(position == 0 || position == sortingMap.get(selectedSortField)) {
                    tv.setTextColor(Color.GRAY);  // Set the disable item text color
                }else{
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };

        spinner_sortBy.setAdapter(sortingAdapter);
        spinner_sortBy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0)
                    return;
                selectedSortField = parent.getItemAtPosition(position).toString();
                reSortFavoriteStockList(); //sort favoriteStockList
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });


        /********** Ordering Rule **********/
        spinner_orderRule = findViewById(R.id.spinner_orderRule);
        final String[] orderingRule = new String[]{"Order", ORDER_ASC, ORDER_DESC};
        orderingMap = new HashMap<>();
        for(int i = 0; i < orderingRule.length; i++)
            orderingMap.put(orderingRule[i], i);

        ArrayAdapter<String> orderingAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_dropdown_item, orderingRule){
            @Override
            public boolean isEnabled(int position) {
                //position != 0 && position != current Selected Item
                return !(position == 0 || position == orderingMap.get(selectedOrderRule));
            }
            @Override
            public boolean areAllItemsEnabled() {
                return false;
            }
            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                //position == 0 || position == current Selected Item
                if(position == 0 || position == orderingMap.get(selectedOrderRule)) {
                    tv.setTextColor(Color.GRAY);  // Set the disable item text color
                }else{
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };

        spinner_orderRule.setAdapter(orderingAdapter);
        spinner_orderRule.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0)
                    return;
                selectedOrderRule = parent.getItemAtPosition(position).toString();
                reSortFavoriteStockList(); //sort favoriteStockList
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

    }
    /***************************** Spinner, for Sorting Favorite List *****************************/




    /***************************** ListView, for Favorite Stock List *****************************/
    private void configureFavoriteStockList() {
        Log.i(TAG, selectedSortField + ", " + selectedOrderRule);
        lv_favorite = findViewById(R.id.lv_favorite);
        updateFavoriteList();
    }

    private void reSortFavoriteStockList(){
        Log.i(TAG, selectedSortField + ", " + selectedOrderRule);
        Collections.sort(favoriteStockList, new FavoriteStockComparator(selectedSortField, selectedOrderRule));
        FavoriteListAdapter favoriteListAdapter = new FavoriteListAdapter(favoriteStockList);
        favoriteListAdapter.notifyDataSetChanged();
        lv_favorite.setAdapter(favoriteListAdapter);
    }

    /**
     * after user click add/delete stock to/from its favorite list,
     * the favorite list here(homepage) should also be updated, update it in onResume()
     * fetch list from SharedPreferences
     */
    private void updateFavoriteList(){
        StorageService storageService = StorageService.getInstance(this); //get storage instance
        //fetch favoriteStockList
        favoriteStockList = storageService.getFavoriteStockList("favoriteStockList");
        if(favoriteStockList == null) //if no list
            favoriteStockList = new ArrayList<>();
        FavoriteListAdapter favoriteListAdapter = new FavoriteListAdapter(favoriteStockList);
        lv_favorite.setAdapter(favoriteListAdapter);
        lv_favorite.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final FavoriteStock dataModel = favoriteStockList.get(position);
                Toast.makeText(parent.getContext(), "view clicked: " + dataModel.toString(), Toast.LENGTH_SHORT).show();
                //Get Quote when clicked

            }
        });
    }

    /***************************** ListView, for Favorite Stock List *****************************/

}
