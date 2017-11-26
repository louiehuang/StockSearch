package edu.usc.liuyinhu.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;

import edu.usc.liuyinhu.models.StockName;
import retrofit2.Call;

public class MyService extends IntentService {

    public static final String TAG = "MyService";
    public static final String MY_SERVICE_MESSAGE = "myServiceMessage";
    public static final String MY_SERVICE_PAYLOAD = "myServicePayload";

    public MyService() {
        super("MyService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        AutoCompleteService webService = AutoCompleteService.retrofit.create(AutoCompleteService.class);
        Call<StockName[]> call = webService.stockNameItems("AA");
        StockName[] stockNameItems;

        try {
            stockNameItems = call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, "onHandleIntent: " + e.getMessage());
            return;
        }
        Log.i(TAG, "onHandleIntent: " + stockNameItems[0]);

        //Return the data to MainActivity
        Intent messageIntent = new Intent(MY_SERVICE_MESSAGE);
        messageIntent.putExtra(MY_SERVICE_PAYLOAD, stockNameItems);
        LocalBroadcastManager manager =
                LocalBroadcastManager.getInstance(getApplicationContext());
        manager.sendBroadcast(messageIntent);
    }

}
