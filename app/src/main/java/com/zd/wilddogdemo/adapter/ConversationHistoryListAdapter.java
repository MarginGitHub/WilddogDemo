package com.zd.wilddogdemo.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zd.wilddogdemo.R;
import com.zd.wilddogdemo.beans.Doctor;
import com.zd.wilddogdemo.beans.VideoCallInfo;
import com.zd.wilddogdemo.net.NetServiceConfig;
import com.zd.wilddogdemo.utils.GlideApp;
import com.zd.wilddogdemo.utils.Util;

import java.text.SimpleDateFormat;
import java.util.List;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;

/**
 * Created by dongjijin on 2017/8/30 0030.
 */

public class ConversationHistoryListAdapter extends RecyclerView.Adapter<ConversationHistoryListAdapter.ConversationHistoryViewHolder> {
    private List<VideoCallInfo> mData;


    public void setData(List<VideoCallInfo> data) {
        mData = data;
    }


    @Override
    public ConversationHistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_call_history, parent, false);
        return new ConversationHistoryViewHolder(view, parent.getContext());
    }

    @Override
    public void onBindViewHolder(ConversationHistoryViewHolder holder, int position) {
        holder.updateData(mData.get(position));
    }

    @Override
    public int getItemCount() {
        if (mData == null) {
            return 0;
        }
        return mData.size();
    }

    class ConversationHistoryViewHolder extends RecyclerView.ViewHolder {
        private final ImageView mAvatar;
        private final TextView mNickName;
        private final TextView mConversationTime;
        private final Context mContext;
        private final TextView mConversationStartTime;
        private final SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        public ConversationHistoryViewHolder(View itemView, Context context) {
            super(itemView);
            mAvatar = (ImageView) itemView.findViewById(R.id.avatar_iv);
            mNickName = (TextView) itemView.findViewById(R.id.nick_name_tv);
            mConversationTime = (TextView) itemView.findViewById(R.id.conversation_time_tv);
            mConversationStartTime = (TextView) itemView.findViewById(R.id.conversation_start_tv);
            mContext = context;
        }

        public void updateData(VideoCallInfo info) {
            GlideApp.with(mContext)
                    .load(NetServiceConfig.HEAD_IMAGE_BASE_URL + info.getHead_img_url())
                    .placeholder(R.drawable.head)
                    .circleCrop()
                    .into(mAvatar);
            mNickName.setText(info.getNick_name());
            mConversationTime.setText(String.format("通话时长:\t%d秒", info.getDuration()));
            String date = dataFormat.format(info.getStart() * 1000);
            mConversationStartTime.setText(date);
        }
    }
}
