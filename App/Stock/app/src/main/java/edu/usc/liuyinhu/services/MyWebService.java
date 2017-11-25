package edu.usc.liuyinhu.services;

import edu.usc.liuyinhu.models.StockName;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MyWebService {

//    //    String BASE_URL = "http://10.0.2.2:3000/";
////    String FEED = "autocomplete";

    //http://dev.markitondemand.com/MODApis/Api/v2/Lookup/json?input=AAPL
    String BASE_URL = "http://dev.markitondemand.com/";
    String FEED = "MODApis/Api/v2/Lookup/json";
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    @GET(FEED)
    Call<StockName[]> stockNameItems(@Query("input") String symbol);


//    @GET(FEED)
//    Call<StockName[]> stockNameItems(@Query("symbol") String symbol);



}
