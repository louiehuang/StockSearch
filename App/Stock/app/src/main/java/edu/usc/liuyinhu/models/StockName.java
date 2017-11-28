package edu.usc.liuyinhu.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by hlyin on 24/11/2017.
 */

public class StockName implements Parcelable {

    private String Symbol;
    private String Name;
    private String Exchange;


    protected StockName(Parcel in) {
        Symbol = in.readString();
        Name = in.readString();
        Exchange = in.readString();
    }

    public StockName(String symbol, String name, String exchange) {
        Symbol = symbol;
        Name = name;
        Exchange = exchange;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(Symbol);
        dest.writeString(Name);
        dest.writeString(Exchange);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<StockName> CREATOR = new Creator<StockName>() {
        @Override
        public StockName createFromParcel(Parcel in) {
            return new StockName(in);
        }

        @Override
        public StockName[] newArray(int size) {
            return new StockName[size];
        }
    };

    @Override
    public String toString() {
        return Symbol + " - " + Name + " (" + Exchange + ")";
    }


    public String getSymbol() {
        return Symbol;
    }
    public void setSymbol(String symbol) {
        Symbol = symbol;
    }
    public String getName() {
        return Name;
    }
    public void setName(String name) {
        Name = name;
    }
    public String getExchange() {
        return Exchange;
    }
    public void setExchange(String exchange) {
        Exchange = exchange;
    }
}
