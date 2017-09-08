package com.zd.wilddogdemo.beans;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dongjijin on 2017/9/6 0006.
 */

public class DoctorNotShownList {
    private List<String> mUidList;

    public DoctorNotShownList() {
        mUidList = new ArrayList<>();
    }

    public synchronized void add(String uid) {
        mUidList.add(uid);
    }

    public synchronized void remove(String uid) {
        mUidList.remove(uid);
    }

    public synchronized boolean contains(String uid) {
        return mUidList.contains(uid);
    }

    public synchronized List<String> getUids(int count) {
        List<String> ret = new ArrayList<>();
        int size = mUidList.size() >= count ? count : mUidList.size();
        for (int i = 0; i < size; i++) {
            ret.add(mUidList.get(0));
            mUidList.remove(0);
        }
        return ret;
    }

}
