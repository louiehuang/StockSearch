package edu.usc.liuyinhu.interfaces;

import com.android.volley.VolleyError;

/**
 * Created by hlyin on 27/11/2017.
 */

public interface VolleyCallbackListener<T> {
    void notifySuccess(String requestType, T response);
    void notifyError(String requestType, VolleyError error);
}
