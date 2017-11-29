package edu.usc.liuyinhu.models;

/**
 * Created by hlyin on 28/11/2017.
 */

public class FavoriteStock {

    private String symbol;
    private Double price;
    private Double change; //change = cur_price - prev_price, then calculate changePercent
    private Double changePercent;
    /**
     * time that the user added this stock to its favorite list
     * milliseconds, Date d = new Date(); Long addTime = d.getTime();
     */
    private Long addTime;


    public FavoriteStock(){}

    public FavoriteStock(String symbol, Double price, Double change, Double changePercent){
        this.symbol = symbol;
        this.price = price;
        this.change = change;
        this.changePercent = changePercent;
    }

    public FavoriteStock(String symbol, Double price, Double change, Double changePercent, Long addTime){
        this.symbol = symbol;
        this.price = price;
        this.change = change;
        this.changePercent = changePercent;
        this.addTime = addTime;
    }

    @Override
    public String toString() {
        return symbol + ", " + price + ", " + change + ", " + changePercent + ", " + addTime;
    }


    public String getSymbol() {
        return symbol;
    }
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    public Double getPrice() {
        return price;
    }
    public void setPrice(Double price) {
        this.price = price;
    }
    public Double getChange() {
        return change;
    }
    public void setChange(Double change) {
        this.change = change;
    }
    public Double getChangePercent() {
        return changePercent;
    }
    public void setChangePercent(Double changePercent) {
        this.changePercent = changePercent;
    }
    public Long getAddTime() {
        return addTime;
    }
    public void setAddTime(Long addTime) {
        this.addTime = addTime;
    }

}
