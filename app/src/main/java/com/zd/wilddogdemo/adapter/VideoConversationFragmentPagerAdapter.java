package com.zd.wilddogdemo.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.zd.wilddogdemo.ui.user.fragments.AboutMeFragment;
import com.zd.wilddogdemo.ui.user.fragments.FollowedDoctorsFragment;
import com.zd.wilddogdemo.ui.user.fragments.OnlineDoctorListFragment;

import java.util.ArrayList;
import java.util.List;

import static android.R.attr.id;

/**
 * Created by dongjijin on 2017/9/6 0006.
 */

public class VideoConversationFragmentPagerAdapter extends FragmentPagerAdapter {
    private List<Fragment> mFragments;
    public VideoConversationFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
        mFragments = new ArrayList<>(3);
        mFragments.add(new OnlineDoctorListFragment());
        mFragments.add(new FollowedDoctorsFragment());
        mFragments.add(new AboutMeFragment());
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return 3;
    }

    public OnlineDoctorListFragment getOnlineDoctorListFragment() {
        return (OnlineDoctorListFragment) mFragments.get(0);
    }

    public String getFragmentTag(int viewId, int postion) {
        return "android:switcher:" + viewId + ":" + postion;
    }
}
