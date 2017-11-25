package edu.usc.liuyinhu.stocksearch;

import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import edu.usc.liuyinhu.interfaces.IAsyncResponse;

import static java.lang.Thread.sleep;

/**
 * Created by hlyin on 23/11/2017.
 */

public class AutoCompleteTask extends AsyncTask<String, Void, ArrayList<String>> {

//    String baseURL = "http://dev.markitondemand.com/MODApis/Api/v2/Lookup/json?input=";
    //Android emulator runs in a Virtual Machine therefore here 127.0.0.1 or localhost will be emulator's own loopback address.
    String baseURL = "http://10.0.2.2:3000/autocomplete?symbol="; //localhost

    public AutoCompleteTask(){}

    public IAsyncResponse delegate = null;


    @Override
    protected ArrayList<String> doInBackground(String... stockInput) {
        //http://dev.markitondemand.com/MODApis/Api/v2/Lookup/json?input=apple
        //http://localhost:3000/autocomplete?symbol=AAPL

        ArrayList<String> data = new ArrayList<>(); //store data from API
        String queryURL = baseURL + stockInput[0].trim(); //stockInput[0] is the user input
        String result;

        //type too fast will cause app crush, how to deal with it???
        try {
            sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        try {
            Log.d("AutoCompleteTask", queryURL);

            URL myUrl = new URL(queryURL);
            HttpURLConnection connection = (HttpURLConnection) myUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            InputStreamReader streamReader = new InputStreamReader(connection.getInputStream());
            BufferedReader reader = new BufferedReader(streamReader);
            StringBuilder stringBuilder = new StringBuilder();

            String inputLine;
            while((inputLine = reader.readLine()) != null){
                stringBuilder.append(inputLine);
            }

            reader.close();
            streamReader.close();
            result = stringBuilder.toString();
        }
        catch(IOException e){
            e.printStackTrace();
            result = null;
        }
        
        if(result != null){
            Log.d("AutoCompleteTask", result);
            data = JSONArray2List(result);
        }

        return data;
    }



    @Override
    protected void onPostExecute(ArrayList<String> result) {
        super.onPostExecute(result); //result is the returned data is doInBackground()
        delegate.processAutoCompleteFinish(result);
    }


    private ArrayList<String> JSONArray2List(String JSONArrayString){
        ArrayList<String> ret = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(JSONArrayString);
            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObj = jsonArray.getJSONObject(i);
                StringBuffer sb = new StringBuffer();
                sb.append(jsonObj.getString("Symbol"));
                sb.append(" - ");
                sb.append(jsonObj.getString("Name"));
                sb.append(" (");
                sb.append(jsonObj.getString("Exchange"));
                sb.append(")");
                ret.add(sb.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ret;
    }



//    private String fetchData(String queryUrl){
//        Log.d("AutoCompleteTask", queryUrl);
//        final List<String> list = new LinkedList<>();
//        final String[] data = new String[1];
//
//        final JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
//                (Request.Method.GET, queryUrl, null, new Response.Listener<JSONArray>() {
//                    @Override
//                    public void onResponse(JSONArray response) {
//                        Log.d("AutoCompleteTask", response.toString());
//                        try{
//
//                            for(int i = 0; i < response.length(); i++){
//                                JSONObject jsonObj = response.getJSONObject(i);
//
//                                StringBuffer sb = new StringBuffer();
//                                sb.append(jsonObj.getString("Symbol")); sb.append(" - ");
//                                sb.append(jsonObj.getString("Name")); sb.append(" (");
//                                sb.append(jsonObj.getString("Exchange")); sb.append(")");
//                                list.add(sb.toString());
//                                Log.d("AutoCompleteTask", sb.toString());
//                            }
//
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }, new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Log.d("AutoCompleteTask", error.toString());
//                    }
//                });
//        requestQueue.add(jsonArrayRequest);
//
//        return data[0];
//    }


}
