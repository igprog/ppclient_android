package com.example.programprehraneklijent;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new RecordFragment();
        } else if (position == 1) {
            return new RecordsFragment();
        } else return new MenusFragment();
    }

    @Override
    public int getCount() {
        return 3;
    }

}
