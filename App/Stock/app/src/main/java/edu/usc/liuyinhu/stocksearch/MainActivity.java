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
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import edu.usc.liuyinhu.R;
import edu.usc.liuyinhu.adapters.FavoriteListAdapter;
import edu.usc.liuyinhu.interfaces.ParamConfigurations;
import edu.usc.liuyinhu.interfaces.VolleyCallbackListener;
import edu.usc.liuyinhu.models.FavoriteStock;
import edu.usc.liuyinhu.models.StockName;
import edu.usc.liuyinhu.services.ParseService;
import edu.usc.liuyinhu.services.StorageService;
import edu.usc.liuyinhu.services.VolleyNetworkService;
import edu.usc.liuyinhu.util.FavoriteStockComparator;


public class MainActivity extends AppCompatActivity implements ParamConfigurations {
    private static final String TAG = "MainActivity";

    private String symbol; //user input symbol

    /*********** Volley Network, for autocomplete and favorite list updating ***********/
    VolleyCallbackListener callbackListener = null;
    VolleyNetworkService volleyNetworkService = null;
    /*********** Volley Network, for autocomplete and favorite list updating ***********/

    /*************************** AutoComplete ************************/
    AutoCompleteTextView ac_stock_input;
    Integer autoCompleteLimit = 5; //show 5 items
    List<StockName> stockNameList;
    ArrayAdapter<StockName> acAdapter;
    boolean isComingFromSelect = false; //if actv's text changes because of selection operation, do not call onTextChanged
    /*************************** AutoComplete ************************/


    /*************************** Sort and Order ************************/
    Spinner spinner_sortBy;
    Spinner spinner_orderRule;
    HashMap<String, Integer> sortingMap; //"Symbol" => 1, key is sorting field, value is the position in spinner
    String selectedSortField = SORT_DEFAULT; //TIME
    HashMap<String, Integer> orderingMap; //"Ascending" => 1, key is ordering field, value is the position in spinner
    String selectedOrderRule = ORDER_ASC;
    /*************************** Sort and Order ************************/


    /*************************** Favorite List ************************/
    ListView lv_favorite;
    List<FavoriteStock> favoriteStockList;
    /*************************** Favorite List ************************/

    /*************************** Refresh Favorite List ************************/
    Switch switch_auto_refresh;
    ImageButton imgBtn_manual_refresh;
    Integer favoriteSize; //size of favoriteStockList, used when updating, making sure fetch all possibble data
    Integer finishNum; //number of finished new request (for favorite stock updating)
    ProgressBar pb_refreshing;
    List<FavoriteStock> oldFavoriteStockList; //old one, deep copy, to update favoriteStockList directly
    /*************************** Refresh Favorite List ************************/



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //volley init
        initVolleyCallback(); //init call back
        volleyNetworkService = VolleyNetworkService.getInstance(callbackListener, MainActivity.this);

        configureQueryButton(); //configure query and clear button

        configureAutoComplete(); //auto complete

        configureRefresh(); //refresh switch and image button

        configureSortingSpinner(); //sort and order spinner

        configureFavoriteStockList(); //favorite stock list view
    }


    @Override
    protected void onResume() {
        super.onResume();
        constructFavoriteStockListFromStorage();
        updateFavoriteList();
    }

    /***************************** Button, Get Quote and Clear  *****************************/
    private void configureQueryButton() {
        //query btn
        Button btn_getQuote = findViewById(R.id.btn_getQuote);
        btn_getQuote.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                queryStockInfo(symbol);
            }
        });

        //clear btn
        Button btn_clear = findViewById(R.id.btn_clear);
        btn_clear.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ac_stock_input.setText("");
            }
        });
    }

    /**
     * start activity, query symbol's details
     */
    private void queryStockInfo(String querySymbol){
        Log.i(TAG, querySymbol);
        if(!isInputValidate()) //validation check, invalid input
            return;
        //if full name, make it acronym, AAPL - Apple Inc (NASDAQ) => AAPL
        if(querySymbol.contains(" - "))
            querySymbol = querySymbol.substring(0, querySymbol.indexOf(" - "));
        Intent intent = new Intent(getBaseContext(), StockDetailsActivity.class);
        intent.putExtra("symbol", querySymbol);
        startActivity(intent);
    }

    private boolean isInputValidate(){
        if(null == symbol || symbol.length() == 0)
            return false;
        int length = symbol.replaceAll("\\s+", "").length();
        return (length > 0); //not empty and not just only contains space
    }
    /***************************** Button, Get Quote and Clear  *****************************/



    /***************************** AutoCompleteTextView, Using Volley*****************************/
    private void configureAutoComplete() {
        ac_stock_input = findViewById(R.id.ac_stock_input); //auto complete
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
//                Log.d(TAG, "Volley requester: " + requestType + ", Volley String: " + response);
                //process auto complete returned data
                if(requestType.equals(GET_AUTOCOMPLETE) && null != response){
                    stockNameList = ParseService.parseStockNames(response.toString(), autoCompleteLimit); //parse autocomplete
                    displayData(); //update view
                }else if(requestType.contains(GET_UPDATED_STOCK_DETAILS)){
                    if(null == response){
                        favoriteSize--;
                    }else{
                        String[] tmp = requestType.split(",");
                        int index = Integer.parseInt(tmp[1]);
                        FavoriteStock oldStockDetails = favoriteStockList.get(index);

                        //parse to get updated FavStock, use oldStockDetails to set change
                        FavoriteStock newStockDetails = ParseService.parseStockDetailsAsFavoriteStock(oldStockDetails, response.toString()); //

                        Log.i(TAG, "favoriteSize: " + favoriteSize + ", " + oldStockDetails);
                        Log.i(TAG, newStockDetails.toString());

                        if(newStockDetails == null)
                            favoriteSize--;
                        else{
                            favoriteStockList.set(index, newStockDetails);
                            finishNum++;
                            if(finishNum == favoriteSize){
                                pb_refreshing.setVisibility(ProgressBar.INVISIBLE);
                                //update storage and list view

//                                Collections.sort(refreshedFavoriteStockList, new FavoriteStockComparator(selectedSortField, selectedOrderRule));

                                //update list view
                                FavoriteListAdapter favoriteListAdapter = new FavoriteListAdapter(favoriteStockList);
                                lv_favorite.setAdapter(favoriteListAdapter);

                                //store updated list
                                StorageService storageService = StorageService.getInstance();
                                //over write favoriteStockList
                                storageService.setFavoriteStockList("favoriteStockList", favoriteStockList);
                            }
                        }
                    }
                }
            }
            @Override
            public void notifyError(String requestType,VolleyError error) {
                Log.d(TAG, "Volley requester:" + requestType + ", With Error: " + error.getMessage());
                if(requestType.equals(GET_UPDATED_STOCK_DETAILS)){
                    favoriteSize--;
                }
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


    private void displayData() {
        if (stockNameList != null) {
            acAdapter = new ArrayAdapter<>(
                    MainActivity.this, android.R.layout.simple_dropdown_item_1line, stockNameList);
            ac_stock_input.setAdapter(acAdapter);
            ac_stock_input.showDropDown(); //refresh
        }
    }
    /***************************** AutoCompleteTextView, Using Volley*****************************/



    TimerTask refreshTimerTask;
    Timer refreshTimer;

    /***************** Switch and ImageButton, Auto Refresh and Manual Refresh ****************/
    private void configureRefresh() {
        pb_refreshing = findViewById(R.id.pb_refreshing);

        //auto refresh
        switch_auto_refresh = findViewById(R.id.switch_auto_refresh);
        switch_auto_refresh.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    pb_refreshing.setVisibility(ProgressBar.VISIBLE);
                    Log.i(TAG, "Turn on Auto Refresh");
                    //turn on refreshTimer to refresh automatically
                    refreshTimerTask = new TimerTask(){
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    refreshFavoriteListOnce();
//                                    Toast.makeText(getApplicationContext(), "Start Auto Refresh", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    };
                    refreshTimer = new Timer();
                    refreshTimer.schedule(refreshTimerTask,0,5000);
                }else{
                    Log.i(TAG, "Turn off Auto Refresh");
                    //turn off refreshTimer
                    if(refreshTimer != null)
                        refreshTimer.cancel();
                    refreshTimer = null;
                }
            }
        });

        //manual refresh
        imgBtn_manual_refresh = findViewById(R.id.imgBtn_manual_refresh);
        imgBtn_manual_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pb_refreshing.setVisibility(ProgressBar.VISIBLE);
                refreshFavoriteListOnce();
            }
        });
    }

    private void refreshFavoriteListOnce() {
        //1. get favorite stock list as oldFavoriteStockList
        StorageService storageService = StorageService.getInstance(MainActivity.this); //get storage instance
        oldFavoriteStockList = storageService.getFavoriteStockList("favoriteStockList");
        if(oldFavoriteStockList == null || oldFavoriteStockList.size() == 0) { //if no list
            Toast.makeText(MainActivity.this, "No favorite stock to refresh", Toast.LENGTH_SHORT).show();
            return;
        }

        //2. set size and finish number, to know when to update list view
        favoriteSize = oldFavoriteStockList.size();
        finishNum = 0;

        //3. iterate and request new data
        volleyNetworkService = VolleyNetworkService.getInstance();
        for(int i = 0 ; i < favoriteStockList.size(); i++){
            FavoriteStock favoriteStock = favoriteStockList.get(i);
            String feed = "price?symbol=" + favoriteStock.getSymbol(); //symbol to query
            //pass its index to callback to update
            volleyNetworkService.getDataAsString(GET_UPDATED_STOCK_DETAILS + "," + i, feed);
        }

        //4. & 5. store to storage & update list view, done in call back function
    }
    /***************** Switch and ImageButton, Auto Refresh and Manual Refresh ****************/




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
        constructFavoriteStockListFromStorage();
        updateFavoriteList();
    }

    private void reSortFavoriteStockList(){
        Log.i(TAG, selectedSortField + ", " + selectedOrderRule);
        Collections.sort(favoriteStockList, new FavoriteStockComparator(selectedSortField, selectedOrderRule));
        FavoriteListAdapter favoriteListAdapter = new FavoriteListAdapter(favoriteStockList);
        favoriteListAdapter.notifyDataSetChanged();
        lv_favorite.setAdapter(favoriteListAdapter);

        //store updated list
        StorageService storageService = StorageService.getInstance();
        //over write favoriteStockList
        storageService.setFavoriteStockList("favoriteStockList", favoriteStockList);
    }


    /**
     * construct favoriteStockList from storage
     */
    private void constructFavoriteStockListFromStorage(){
        StorageService storageService = StorageService.getInstance(this); //get storage instance
        //fetch favoriteStockList
        favoriteStockList = storageService.getFavoriteStockList("favoriteStockList");
        if(favoriteStockList == null) //if no list
            favoriteStockList = new ArrayList<>();
    }


    /**
     * after user click add/delete stock to/from its favorite list,
     * the favorite list here(homepage) should also be updated, update it in onResume()
     * fetch list from SharedPreferences
     */
    private void updateFavoriteList(){
        FavoriteListAdapter favoriteListAdapter = new FavoriteListAdapter(favoriteStockList);
        lv_favorite.setAdapter(favoriteListAdapter);
        lv_favorite.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final FavoriteStock dataModel = favoriteStockList.get(position);
//                Toast.makeText(parent.getContext(), "view clicked: " + dataModel.getSymbol(), Toast.LENGTH_SHORT).show();

                //here, do not check input validation... since it must be valid
                String querySymbol = dataModel.getSymbol();
                Log.i(TAG, querySymbol + ", " + querySymbol.length());
                Intent intent = new Intent(getBaseContext(), StockDetailsActivity.class);
                intent.putExtra("symbol", querySymbol);
                startActivity(intent);
            }
        });
    }
    /***************************** ListView, for Favorite Stock List *****************************/

}
