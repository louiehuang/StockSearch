package edu.usc.liuyinhu.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import edu.usc.liuyinhu.fragments.StockCurrentFragment;
import edu.usc.liuyinhu.fragments.StockHistoricalFragment;
import edu.usc.liuyinhu.fragments.StockNewsFragment;

/**
 * Created by hlyin on 26/11/2017.
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {
    private String symbol;

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        if(position == 0){ //current
            return StockCurrentFragment.newInstance(symbol); //indicator charts
        }else if(position == 1){ //historical
            return StockHistoricalFragment.newInstance(symbol); //historical chart
        }else{
            return StockNewsFragment.newInstance(symbol); //news
        }
    }

    @Override
    public int getCount() {
        return 3; // Show 3 total pages.
    }

    public String getSymbol() {
        return symbol;
    }
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

}
