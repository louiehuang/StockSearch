package edu.usc.liuyinhu.services;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by hlyin on 26/11/2017.
 */

public interface StockPriceService {

//    String BASE_URL = "http://dev.markitondemand.com/";
//    String FEED = "MODApis/Api/v2/Lookup/json";
//
//    Retrofit retrofit = new Retrofit.Builder()
//            .baseUrl(BASE_URL)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build();
//    @GET(FEED)
//    Call<ResponseBody> stockPrice(@Query("input") String symbol);


    //http://localhost:3000/price?symbol=AAPL
    String BASE_URL = "http://10.0.2.2:3000/";
    String FEED = "price";

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    //get rawResponse
    @GET(FEED)
    Call<ResponseBody> stockPrice(@Query("symbol") String symbol); //or use ResponseBody and the parse

}