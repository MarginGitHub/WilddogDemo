package com.zd.wilddogdemo.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zd.wilddogdemo.R;
import com.zd.wilddogdemo.beans.Doctor;
import com.zd.wilddogdemo.net.NetServiceConfig;
import com.zd.wilddogdemo.utils.GlideApp;
import com.zd.wilddogdemo.utils.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dongjijin on 2017/8/30 0030.
 */

public class DoctorListAdapter extends RecyclerView.Adapter<DoctorListAdapter.UserListViewHolder> {
    private List<Doctor> mData;
    private ICallPeer mCallPeer;

    public DoctorListAdapter(ICallPeer callPeer) {
        mCallPeer = callPeer;
    }

    public void setData(List<Doctor> data) {
        mData = data;
    }


    @Override
    public UserListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_list, parent, false);
        return new UserListViewHolder(view, mCallPeer);
    }

    @Override
    public void onBindViewHolder(UserListViewHolder holder, int position) {
        Doctor doctor = mData.get(position);
        holder.setDoctor(doctor);
    }

    @Override
    public int getItemCount() {
        if (mData == null) {
            return 0;
        }
        return mData.size();
    }

    public boolean addItem(Doctor doctor) {
        if (doctor == null) {
            return false;
        }
        boolean ret = mData.add(doctor);
        notifyItemInserted(mData.size() - 1);
        return ret;
    }

    public boolean addItems(List<Doctor> doctors) {
        if (doctors == null || doctors.size() == 0) {
            return false;
        }
        int start = mData.size();
        boolean ret = mData.addAll(doctors);
        notifyItemRangeInserted(start, doctors.size());
        return ret;
    }

    public boolean updateItem(Doctor doctor) {
        if (doctor == null) {
            return false;
        }
        if (mData.size() != 0) {
            for (int i = 0; i < mData.size(); i++) {
                Doctor item = mData.get(i);
                if (item.getDoc_id() == doctor.getDoc_id()) {
                    item.update(doctor);
                    notifyItemChanged(i);
                }
            }
        }
        return true;
    }

    public boolean removeItem(String uid) {
        if (mData.size() != 0) {
            for (int i = 0; i < mData.size(); i++) {
                Doctor item = mData.get(i);
                if (item.getDoc_id().equals(uid)) {
                    mData.remove(i);
                    notifyItemRemoved(i);
                }
            }
        }
        return true;
    }

    class UserListViewHolder extends RecyclerView.ViewHolder {
        private TextView mNickNameTv;
        private TextView mPriceTv;
        private ImageView mAdIv;
        private Doctor mDoctor;
        private Context mContext;

        public UserListViewHolder(View itemView, final ICallPeer callPeer) {
            super(itemView);
            mContext = (Context)callPeer;
            mNickNameTv = (TextView) itemView.findViewById(R.id.nick_name);
            mPriceTv = (TextView)itemView.findViewById(R.id.price);
            mAdIv = (ImageView)itemView.findViewById(R.id.ad_iv);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callPeer.callPeer(mDoctor);
                }

            });
        }

        public void setDoctor(Doctor doctor) {
            mDoctor = doctor;
            GlideApp.with(mContext)
                    .load(NetServiceConfig.AD_BASE_URL + mDoctor.getAd_url())
                    .placeholder(R.drawable.banner)
                    .optionalCenterInside()
                    .into(mAdIv);
//            Util.setImageView(mContext, mAdIv, NetServiceConfig.AD_BASE_URL + mDoctor.getAd_url(), R.drawable.banner, false);
            mNickNameTv.setText(mDoctor.getNick_name());
            mPriceTv.setText(String.format("%s元/次", mDoctor.getVideo_price()));
        }

    }

    public interface ICallPeer {
        void callPeer(Doctor doctor);
    }
}
