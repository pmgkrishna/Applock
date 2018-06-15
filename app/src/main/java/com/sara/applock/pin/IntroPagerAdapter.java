










package com.sara.applock.pin;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.sara.applock.R;

import static com.sara.applock.pin.ImageFragment.newInstance;


/**
 * Created by pmgkrishna on 09-06-2017.
 */


public class IntroPagerAdapter extends FragmentPagerAdapter {
    int[] img={R.drawable.screen0,R.drawable.screen1,R.drawable.screen2};

    public IntroPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return newInstance(img[position],position,img.length);
    }
    @Override
    public int getCount() {
        return img.length;
    }
}


