package edu.usc.liuyinhu.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import edu.usc.liuyinhu.interfaces.IStorageService;
import edu.usc.liuyinhu.models.FavoriteStock;

/**
 * Created by hlyin on 28/11/2017.
 */

public class StorageService implements IStorageService<FavoriteStock> {

    private static StorageService instance = null;
    Context context = null;
    SharedPreferences sharedPrefs;
    Gson gson;

    private StorageService(Context context)
    {
        this.context = context;
        this.sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.gson = new Gson();
    }

    public static synchronized StorageService getInstance(Context context)
    {
        if (null == instance)
            instance = new StorageService(context);
        return instance;
    }

    //this is so you don't need to pass context each time
    public static synchronized StorageService getInstance()
    {
        if (null == instance){
            throw new IllegalStateException(StorageService.class.getSimpleName() +
                    " is not initialized, call getInstance(...) first");
        }
        return instance;
    }


    /**
     * interface, get FavoriteStockList
     * @param getTag
     * @return
     */
    @Override
    public List<FavoriteStock> getFavoriteStockList(String getTag) {
        return getFavoriteStockListFromSharedPreferences(getTag);
    }

    /**
     * interface, set FavoriteStockList
     * @param setTag
     * @param listToStore
     */
    @Override
    public void setFavoriteStockList(String setTag, List<FavoriteStock> listToStore) {
        setFavoriteStockListToSharedPreferences(setTag, listToStore);
    }




    /**
     * Set FavoriteStockList to SharedPreferences
     * @param setTag "favoriteStockList"
     * @param favoriteStockList
     */
    private void setFavoriteStockListToSharedPreferences(String setTag, List<FavoriteStock> favoriteStockList){
        SharedPreferences.Editor editor = sharedPrefs.edit();
        String jsonString = gson.toJson(favoriteStockList);
        editor.putString(setTag, jsonString);
        editor.apply(); //handle in background
    }

    /**
     * Get FavoriteStockList from SharedPreferences
     * @param getTag
     * @return
     */
    private List<FavoriteStock> getFavoriteStockListFromSharedPreferences(String getTag) {
        String jsonString = sharedPrefs.getString(getTag, null);
        Type type = new TypeToken<ArrayList<FavoriteStock>>() { }.getType();
        return gson.fromJson(jsonString, type);
    }


}
