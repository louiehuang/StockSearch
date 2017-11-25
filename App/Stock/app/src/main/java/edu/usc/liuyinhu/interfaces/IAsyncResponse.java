package edu.usc.liuyinhu.interfaces;

import java.util.ArrayList;

/**
 * Created by hlyin on 24/11/2017.
 */

public interface IAsyncResponse {
//    void processFinish(String output);

    void processAutoCompleteFinish(ArrayList<String> output);

}
