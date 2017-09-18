package com.zd.wilddogdemo.adapter;


import com.zd.wilddogdemo.beans.VideoCallInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * Created by dongjijin on 2017/8/30 0030.
 */

public class ConversationHistoryList {
    private List<VideoCallInfo> mData;
    private ConversationHistoryListAdapter mAdapter;
    private int mStart = 0;
    public static final int COUNT = 10;

    public ConversationHistoryList(ConversationHistoryListAdapter adapter) {
        mData = new ArrayList<>();
        mAdapter = adapter;
        if (mAdapter != null) {
            mAdapter.setData(mData);
        }
    }

    public void setAdapter(ConversationHistoryListAdapter adapter) {
        mAdapter = adapter;
        if (mAdapter != null) {
            mAdapter.setData(mData);
        }
    }

    public ConversationHistoryListAdapter getAdapter() {
        return mAdapter;
    }


    public int getStart() {
        return mStart;
    }

    public void addCallInfo(VideoCallInfo info) {
        if (info == null) {
            return;
        }
        mData.add(info);
        if (mAdapter != null) {
            mAdapter.notifyItemInserted(mData.size() - 1);
        }
        mStart += 1;
    }

    public void append(List<VideoCallInfo> info) {
        if (info == null || info.size() == 0) {
            return;
        }
        int size = info.size();
        int start = mData.size();
        mData.addAll(info);
        mStart += size;
        if (mAdapter != null) {
            mAdapter.notifyItemRangeInserted(start, size);
        }
    }

    public void update(List<VideoCallInfo> infos) {
        mData.clear();
        mData.addAll(infos);
        int size = infos.size();
        mStart = size;
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }
}
