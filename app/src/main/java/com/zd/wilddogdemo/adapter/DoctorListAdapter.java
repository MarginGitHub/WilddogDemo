package com.zd.wilddogdemo.adapter;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.zd.wilddogdemo.R;
import com.zd.wilddogdemo.beans.Doctor;
import com.zd.wilddogdemo.ui.UserActivity;

import java.util.List;

/**
 * Created by dongjijin on 2017/8/30 0030.
 */

public class DoctorListAdapter extends RecyclerView.Adapter<DoctorListAdapter.UserListViewHolder> {
    private List<Doctor> mData;
    private UserActivity mContext;

    public DoctorListAdapter(UserActivity context) {
        mContext = context;
    }

    public void setData(List<Doctor> data) {
        mData = data;
        notifyDataSetChanged();
    }

    @Override
    public UserListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_list, parent, false);
        return new UserListViewHolder(view, mContext);
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
        private final ImageView mHeadIv;
        private final TextView mNameTv;
        private final TextView mIdTv;
        private final ImageView mSexIv;
        private final TextView mFollowCountTv;
        private final TextView mVideoCountTv;
        private final TextView mPriceTv;
        private Doctor mDoctor;

        public UserListViewHolder(View itemView, final UserActivity context) {
            super(itemView);
            mHeadIv = (ImageView) itemView.findViewById(R.id.head_iv);
            mNameTv = (TextView) itemView.findViewById(R.id.name_tv);
            mIdTv = (TextView) itemView.findViewById(R.id.id_tv);
            mSexIv = (ImageView) itemView.findViewById(R.id.sex_iv);
            mFollowCountTv = (TextView) itemView.findViewById(R.id.follow_count);
            mVideoCountTv = (TextView) itemView.findViewById(R.id.video_count);
            mPriceTv = (TextView) itemView.findViewById(R.id.price);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    context.callPeer(mDoctor.getUser_id());
                }

            });
        }

        public void setDoctor(Doctor doctor) {
            mDoctor = doctor;
            Glide.with(mContext).load(mDoctor.getAd_url()).into(mHeadIv);
            mNameTv.setText(mDoctor.getNick_name());
            mIdTv.setText(mDoctor.getUser_id());
            if (mDoctor.getSex().equals("1")) {

            } else {

            }
            mFollowCountTv.setText(mDoctor.getFollow_count());
            mVideoCountTv.setText(mDoctor.getVideo_count());
            mPriceTv.setText(mDoctor.getVideo_price());
        }

    }
}
