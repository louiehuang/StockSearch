package edu.usc.liuyinhu.adapters;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import edu.usc.liuyinhu.fragments.PlaceholderFragment;
import edu.usc.liuyinhu.fragments.StockCurrentFragment;

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
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).

        if(position == 0){
            Log.i("Adapter", "symbol: " + symbol);
            return StockCurrentFragment.newInstance(-10, symbol);
        }
        return PlaceholderFragment.newInstance(position + 1);
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
