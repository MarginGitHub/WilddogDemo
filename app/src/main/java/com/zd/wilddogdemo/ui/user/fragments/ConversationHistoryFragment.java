package com.zd.wilddogdemo.ui.user.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.listener.OnLoadmoreListener;
import com.zd.wilddogdemo.R;
import com.zd.wilddogdemo.adapter.ConversationHistoryList;
import com.zd.wilddogdemo.adapter.ConversationHistoryListAdapter;
import com.zd.wilddogdemo.beans.Result;
import com.zd.wilddogdemo.beans.VideoCallInfo;
import com.zd.wilddogdemo.net.Net;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.annotations.NonNull;

/**
 * Created by dongjijin on 2017/9/6 0006.
 */

public class ConversationHistoryFragment extends BaseFragment {

    @BindView(R.id.conversation_history_container)
    RecyclerView mConversationHistoryContainer;
    Unbinder unbinder;
    @BindView(R.id.refresh_layout)
    SmartRefreshLayout mRefreshLayout;
    private ConversationHistoryList mHistoryList;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_conversation_history, container, false);
        unbinder = ButterKnife.bind(this, view);
        initViews();
        return view;
    }

    @Override
    protected void initViews() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mConversationHistoryContainer.setLayoutManager(linearLayoutManager);
        ConversationHistoryListAdapter adapter = new ConversationHistoryListAdapter();
        if (mHistoryList == null) {
            mHistoryList = new ConversationHistoryList(adapter);
        } else {
            mHistoryList.setAdapter(adapter);
        }
        mConversationHistoryContainer.setAdapter(adapter);

        mRefreshLayout.setRefreshFooter(new ClassicsFooter(getContext()));
        mRefreshLayout.setEnableRefresh(false);
        mRefreshLayout.setEnableLoadmore(true);
        mRefreshLayout.setOnLoadmoreListener(new OnLoadmoreListener() {
            @Override
            public void onLoadmore(RefreshLayout refreshlayout) {
                Net.instance().getVideoCallInfoList(mUser.getToken(), mUser.getUser_id(),
                        mHistoryList.getStart(), ConversationHistoryList.COUNT, new Net.OnNext<Result<List<VideoCallInfo>>>() {
                            @Override
                            public void onNext(@NonNull Result<List<VideoCallInfo>> result) {
                                if (result.getCode() == 100) {
                                    List<VideoCallInfo> infos = result.getData();
                                    mHistoryList.addCallInfos(infos);
                                }
                                mRefreshLayout.finishLoadmore();
                            }
                        }, new Net.OnError() {
                            @Override
                            public void onError(@NonNull Throwable e) {
                                mRefreshLayout.finishLoadmore();
                            }
                        }, ConversationHistoryFragment.class.getSimpleName());
            }
        });
        mRefreshLayout.autoLoadmore();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        Net.instance().removeRequest(ConversationHistoryFragment.class.getSimpleName());
        mHistoryList.setAdapter(null);
        super.onDestroyView();
        unbinder.unbind();
    }
}
