package edu.usc.liuyinhu.interfaces;

/**
 * Created by hlyin on 29/11/2017.
 */

public interface ParamConfigurations {

    String ARG_STOCK_SYMBOL = "symbol";

    /*************************** Params for Volley ************************/
    String GET_AUTOCOMPLETE = "GET_AUTOCOMPLETE";
    String GET_UPDATED_STOCK_DETAILS = "GET_UPDATED_STOCK_DETAILS";
    String GET_STOCK_DETAILS = "GET_STOCK_DETAILS";
    String GET_NEWS = "GET_NEWS";
    /*************************** Params for Volley ************************/



    /*************************** Params for Sort and Order ************************/
    String SORT_DEFAULT = "Default";
    String SORT_SYMBOL = "Symbol";
    String SORT_PRICE = "Price";
    String SORT_CHANGE = "Change";
    String ORDER_ASC = "Ascending";
    String ORDER_DESC = "Descending";
    /*************************** Params for Sort and Order ************************/


    /*************************** Params for Limit ************************/
    Integer AUTOCOMPLETE_ITEM_LIMIT = 5; //news
    Integer NEWS_ITEM_LIMIT = 5; //news
    /*************************** Params for Limit ************************/

}
