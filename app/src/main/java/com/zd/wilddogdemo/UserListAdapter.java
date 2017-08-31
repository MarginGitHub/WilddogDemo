package com.zd.wilddogdemo;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

/**
 * Created by dongjijin on 2017/8/30 0030.
 */

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserListViewHolder> {
    private List<String> mData;
    private UserListActivity mContext;

    public UserListAdapter(UserListActivity context) {
        mContext = context;
    }

    public void setData(List<String> data) {
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
        String uid = mData.get(position);
        holder.setRemoteId(uid);
    }

    @Override
    public int getItemCount() {
        if (mData == null) {
            return 0;
        }
        return mData.size();
    }

    class UserListViewHolder extends RecyclerView.ViewHolder {
        public TextView mRemoteIdTv;
        public Button mCallBtn;
        private String mRemoteId;

        public UserListViewHolder(View itemView, final UserListActivity context) {
            super(itemView);
            mRemoteIdTv = (TextView) itemView.findViewById(R.id.remote_id);
            mCallBtn = (Button) itemView.findViewById(R.id.call_btn);
            mCallBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    context.callPeer(getRemoteId());
                }

            });
        }

        public void setRemoteId(String remoteId) {
            mRemoteId = remoteId;
            mRemoteIdTv.setText(mRemoteId);
        }

        private String getRemoteId() {
            return mRemoteId;
        }
    }
}
