package com.zd.wilddogdemo.adapter;

import com.zd.wilddogdemo.beans.Doctor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by dongjijin on 2017/9/12 0012.
 */

public class DoctorListData {
    private List<Doctor> mDoctors;
    private List<String> mDoctorUids;
    private DoctorListAdapter mAdapter;

    public DoctorListData() {
        mDoctors = Collections.synchronizedList(new ArrayList<Doctor>());
        mDoctorUids = Collections.synchronizedList(new ArrayList<String>());
    }

    public DoctorListAdapter getAdapter() {
        return mAdapter;
    }

    public void setAdapter(DoctorListAdapter adapter) {
        mAdapter = adapter;
        if (mAdapter != null) {
            mAdapter.setData(mDoctors);
        }
    }

    public synchronized int getDoctorCount() {
        return mDoctors.size();
    }

    public synchronized int getDoctorUidCount() {
        return mDoctorUids.size();
    }

    public synchronized void addDoctor(Doctor doctor) {
        if (doctor == null) {
            return;
        }
        mDoctors.add(doctor);
        if (mAdapter != null) {
            int pos = mDoctors.indexOf(doctor);
            mAdapter.notifyItemInserted(pos);
        }
    }

    public synchronized void removeDoctor(String uid) {
        if (mDoctorUids.contains(uid)) {
            removeUid(uid);
        } else {
            int pos = -1;
            for (int i = 0; i < mDoctors.size(); i++) {
                if (mDoctors.get(i).getUser_id().equals(uid)) {
                    pos = i;
                    break;
                }
            }
            if (pos != -1) {
                mDoctors.remove(pos);
                if (mAdapter != null) {
                    mAdapter.notifyItemRemoved(pos);
                }
            }
        }

    }

    public synchronized void updateDoctor(Doctor doctor) {
        if (doctor == null) {
            return;
        }
        for (int i = 0; i < mDoctors.size(); i++) {
            if (mDoctors.get(i).getUser_id().equals(doctor.getUser_id())) {
                mDoctors.get(i).update(doctor);
                if (mAdapter != null) {
                    mAdapter.notifyItemChanged(i);
                }
            }
        }
    }

    public synchronized void addUid(String uid) {
        mDoctorUids.add(uid);
    }

    public synchronized void removeUid(String uid) {
        mDoctorUids.remove(uid);
    }

    public synchronized String getUid() {
        if (mDoctorUids.size() != 0) {
            return mDoctorUids.remove(0);
        }
        return null;
    }

    public synchronized boolean contains(String uid) {
        return mDoctorUids.contains(uid);
    }
}
