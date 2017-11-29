package edu.usc.liuyinhu.util;

import java.util.Comparator;

import edu.usc.liuyinhu.models.FavoriteStock;

/**
 * Created by hlyin on 28/11/2017.
 */

public class FavoriteStockComparator implements Comparator<FavoriteStock> {

    private static final String SORT_DEFAULT = "Default";
    private static final String SORT_SYMBOL = "Symbol";
    private static final String SORT_PRICE = "Price";
    private static final String SORT_CHANGE = "Change";
    private static final String ORDER_ASC = "Ascending";
    private static final String ORDER_DESC = "Descending";

    private String selectedSortField = SORT_DEFAULT; //TIME
    private String selectedOrderRule = ORDER_ASC;

    public FavoriteStockComparator(){}

    public FavoriteStockComparator(String selectedSortField, String selectedOrderRule){
        this.selectedSortField = selectedSortField;
        this.selectedOrderRule = selectedOrderRule;
    }

    @Override
    public int compare(FavoriteStock obj1, FavoriteStock obj2) {
        int ret = 0;
        switch (selectedSortField){
            case SORT_DEFAULT: {
                ret = obj1.getAddTime().compareTo(obj2.getAddTime()); break;
            }
            case SORT_SYMBOL: {
                ret = obj1.getSymbol().compareTo(obj2.getSymbol()); break;
            }
            case SORT_PRICE: {
                ret = obj1.getPrice().compareTo(obj2.getPrice()); break;
            }
            case SORT_CHANGE: {
                ret = obj1.getChange().compareTo(obj2.getChange()); break;
            }
        }
        return selectedOrderRule.equals(ORDER_ASC) ? ret : -1 * ret;
    }
}