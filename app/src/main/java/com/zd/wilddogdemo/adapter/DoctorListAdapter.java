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
            mPriceTv.setText(String.format("%s元/分钟", mDoctor.getVideo_price()));
        }

    }

    public interface ICallPeer {
        void callPeer(Doctor doctor);
    }
}
