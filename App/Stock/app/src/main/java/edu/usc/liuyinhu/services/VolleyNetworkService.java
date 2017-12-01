package edu.usc.liuyinhu.services;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import edu.usc.liuyinhu.interfaces.VolleyCallbackListener;

/**
 * Created by hlyin on 27/11/2017.
 * reference1: https://stackoverflow.com/questions/28172496/android-volley-how-to-isolate-requests-in-another-class
 * reference2: https://stackoverflow.com/questions/35628142/how-to-make-separate-class-for-volley-library-and-call-all-method-of-volley-from
 */

public class VolleyNetworkService {
    private static final String TAG = "VolleyNetworkService";

    //local test => http://10.0.2.2:3000/   such as http://10.0.2.2:3000/autocomplete?symbol=AAPL
    //server use => http://liuyinstock.us-east-2.elasticbeanstalk.com/
    //also remeber to change the base_url in currentChart.html and historicalChart.html
    private static final String BASE_URL = "http://liuyinstock.us-east-2.elasticbeanstalk.com/";

    private static final Integer TIMEOUT_LIMIT = 15000;

    private static VolleyNetworkService instance = null;
    VolleyCallbackListener callbackListener = null; //call back
    RequestQueue requestQueue; //for Volley to call stock API

    public VolleyNetworkService(VolleyCallbackListener resultCallback, Context context)
    {
        callbackListener = resultCallback;
        requestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }

    public static synchronized VolleyNetworkService getInstance(VolleyCallbackListener resultCallback, Context context)
    {
        if (null == instance)
            instance = new VolleyNetworkService(resultCallback, context);
        return instance;
    }

    //this is so you don't need to pass context each time
    public static synchronized VolleyNetworkService getInstance()
    {
        if (null == instance){
            throw new IllegalStateException(VolleyNetworkService.class.getSimpleName() +
                    " is not initialized, call getInstance(...) first");
        }
        return instance;
    }


    /**
     * send StringRequest, get response as String
     * @param requestType
     * @param feed
     */
    public void getDataAsString(final String requestType, String feed){
        String queryURL = BASE_URL + feed;
        Log.d(TAG + ": ", requestType + ", url: " + queryURL);
        try {
            StringRequest stringRequest = new StringRequest(Request.Method.GET, queryURL,
                    new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if(null != callbackListener && null != response) {
                        Log.d(TAG + ": ", requestType + ", Response: " + response.toString());
                        callbackListener.notifySuccess(requestType, response);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if(null != callbackListener && null != error.networkResponse) {
                        Log.d(TAG + ": ", "Error Response code: " + error.networkResponse.statusCode);
                        callbackListener.notifyError(requestType, error);
                    }
                }
            });

            //set time out to 15s since price api response very slow
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                    TIMEOUT_LIMIT,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            requestQueue.add(stringRequest);
        }catch(Exception e){

        }
    }

}
