package edu.usc.liuyinhu.interfaces;

import android.content.Context;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

/**
 * Created by hlyin on 29/11/2017.
 */

/**
 * Do not use this anymore, change to StockCurrentFragment, so to change chartImgURL without using a listener
 */
public class AbandonedIndicatorChartJSInterface {
    private Context mContext = null;
    private static AbandonedIndicatorChartJSInterface instance = null;
    private String chartImgURL = null;
    public String getChartImgURL() {
        return chartImgURL;
    }

    private AbandonedIndicatorChartJSInterface(Context context) {
        mContext = context;
    }


    public static synchronized AbandonedIndicatorChartJSInterface getInstance(Context context)
    {
        if (null == instance)
            instance = new AbandonedIndicatorChartJSInterface(context);
        return instance;
    }

    //this is so you don't need to pass context each time
    public static synchronized AbandonedIndicatorChartJSInterface getInstance()
    {
        if (null == instance){
            throw new IllegalStateException(AbandonedIndicatorChartJSInterface.class.getSimpleName() +
                    " is not initialized, call getInstance(...) first");
        }
        return instance;
    }

    /**
     * This method is also in JS, as interface, to fetch chartImgURL(variable in JS)
     */
    @JavascriptInterface
    public void getImageURL(String chartURL){
        Toast.makeText(mContext, chartURL, Toast.LENGTH_LONG).show();
        chartImgURL = chartURL;
    }
}
