package edu.usc.liuyinhu.stocksearch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.usc.liuyinhu.R;
import edu.usc.liuyinhu.interfaces.IAsyncResponse;
import edu.usc.liuyinhu.models.StockName;
import edu.usc.liuyinhu.services.MyService;
import edu.usc.liuyinhu.services.AutoCompleteService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static java.lang.Thread.sleep;


public class MainActivity extends AppCompatActivity implements IAsyncResponse {
    private static final String TAG = "MainActivity";
    //    private static String[] stocks = new String[] {"AAP", "AAPL", "AL"};
    AutoCompleteTextView ac_stock_input;

    List<StockName> stockNameList;
    ArrayAdapter<StockName> acAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //test switch button
        configureSwitchButton();

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
                if(s.toString().trim().length() >= 1) {
//                    AutoCompleteTask acTask = new AutoCompleteTask();
//                    acTask.delegate = MainActivity.this;
//                    acTask.execute(s.toString());

                    requestData(s.toString().trim());
                }
            }
        });

        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(mBroadcastReceiver,
                        new IntentFilter(MyService.MY_SERVICE_MESSAGE));
    }





    @Override
    public void processAutoCompleteFinish(ArrayList<String> output){
        //Here receive the result fired from async class of onPostExecute(result) method.
        ArrayList<String> stocksList = output;
        ArrayAdapter<String> acAdapter = new ArrayAdapter<>(
                MainActivity.this, android.R.layout.simple_dropdown_item_1line, stocksList); //simple_dropdown_item_1line, or use select_dialog_item
        ac_stock_input.setAdapter(acAdapter);
        ac_stock_input.showDropDown(); //refresh
    }


    private void configureSwitchButton() {

        //query btn
        Button btn_getQuote = findViewById(R.id.btn_getQuote);
        btn_getQuote.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                //https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=AAPL&outputsize=full&apikey=KAGMK7YPZKV0EYYA

                //get data

                //pass data to StockDetailsActivity

                Intent intent = new Intent(getBaseContext(), StockDetailsActivity.class);
                intent.putExtra("symbol", "AAPL");
                startActivity(intent);
            }
        });


        Button switchBtn = findViewById(R.id.switch_btn);
        switchBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, StockDetailsActivity.class));
            }
        });

        //test
        Button testBtn = findViewById(R.id.test_btn);
        testBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
//                startService(new Intent(MainActivity.this, MyService.class));
                requestData();
            }
        });
    }


    private void requestData(String symbol) {
        Log.i(TAG, "request");

        //type too fast will cause app crush, how to deal with it???
        try {
            sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        AutoCompleteService webService =
                AutoCompleteService.retrofit.create(AutoCompleteService.class);
        Call<StockName[]> call = webService.stockNameItems(symbol);
        call.enqueue(new Callback<StockName[]>() {
            @Override
            public void onResponse(Call<StockName[]> call, Response<StockName[]> response) {
                StockName[] stockNameItems = response.body();
                Toast.makeText(MainActivity.this,
                        "Received " + stockNameItems.length + " items from service",
                        Toast.LENGTH_SHORT).show();
                stockNameList = Arrays.asList(stockNameItems);
                displayData();
                Log.i("MainActivity", stockNameItems[0].toString());
            }
            @Override
            public void onFailure(Call<StockName[]> call, Throwable t) {
            }
        });
    }

    private void displayData() {
        if (stockNameList != null) {
            acAdapter = new ArrayAdapter<>(
                    MainActivity.this, android.R.layout.simple_dropdown_item_1line, stockNameList);
            ac_stock_input.setAdapter(acAdapter);
            ac_stock_input.showDropDown(); //refresh
        }
    }


    private void requestData() {
        Log.i(TAG, "request");
        Intent intent = new Intent(MainActivity.this, MyService.class);
        startService(intent);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            StockName[] stockNameItems = (StockName[]) intent
                    .getParcelableArrayExtra(MyService.MY_SERVICE_PAYLOAD);
            Toast.makeText(MainActivity.this,
                    "Received " + stockNameItems.length + " items from service",
                    Toast.LENGTH_SHORT).show();

            Log.i(TAG, stockNameItems[0].toString());

//            stockNameList = Arrays.asList(stockNameItems);
//            displayData();
        }
    };


}
