package com.balakrishnan.poorna.call_record;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class ViewPagerAdapter extends FragmentPagerAdapter {
    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        /*DemoFragment demoFragment=new DemoFragment();
        i=i+1;
        Bundle bundle=new Bundle();
        bundle.putString("message","Fragment :"+i);
        demoFragment.setArguments(bundle);
        return demoFragment;*/
        Fragment fragment=null;
        switch (i){
            case 0:
                fragment=new DemoFragment();
                return fragment;
            case 1:
                fragment=new Fragment_Contact();
                return fragment;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        String res="";
        switch (position){
            case 0:
                res= "Call";
                break;
            case 1:
                res= "Contacts";
               break;
        }
       // position=position+1;
       // return "Fragment "+position;
        return  res;
    }
}
