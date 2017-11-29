package edu.usc.liuyinhu.interfaces;

import java.util.List;

import edu.usc.liuyinhu.models.FavoriteStock;

/**
 * Created by hlyin on 28/11/2017.
 */

public interface IStorageService<T> {

    //get
    List<FavoriteStock> getFavoriteStockList(String getTag);

    //set
    void setFavoriteStockList(String setTag, List<T> listToStore);

}

