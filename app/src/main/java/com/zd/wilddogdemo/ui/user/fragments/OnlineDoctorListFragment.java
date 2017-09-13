package com.zd.wilddogdemo.ui.user.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.listener.OnLoadmoreListener;
import com.zd.wilddogdemo.R;
import com.zd.wilddogdemo.adapter.DoctorListAdapter;
import com.zd.wilddogdemo.beans.Doctor;
import com.zd.wilddogdemo.beans.Result;
import com.zd.wilddogdemo.net.Net;
import com.zd.wilddogdemo.net.NetService;
import com.zd.wilddogdemo.net.NetServiceConfig;
import com.zd.wilddogdemo.net.NetServiceProvider;
import com.zd.wilddogdemo.ui.user.MainActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.annotations.NonNull;

/**
 * Created by dongjijin on 2017/9/6 0006.
 */

public class OnlineDoctorListFragment extends BaseFragment {

    @BindView(R.id.doctor_list_container)
    RecyclerView mDoctorListContainer;
    @BindView(R.id.doctor_refresh_layout)
    SmartRefreshLayout mDoctorRefreshLayout;
    Unbinder unbinder;
    private DoctorListAdapter mAdapter;

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
        View view = inflater.inflate(R.layout.fragment_online_doctor_list, container, false);
        unbinder = ButterKnife.bind(this, view);
        initViews();
        return view;
    }

    @Override
    public void onDestroyView() {
        ((MainActivity) getActivity()).mDoctorListData.setAdapter(null);
        Net.instance().removeRequest(OnlineDoctorListFragment.class.getSimpleName());
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    protected void initViews() {
        setupContainer();
    }


    private void setupRefreshLayout() {
        mDoctorRefreshLayout.setRefreshFooter(new ClassicsFooter(getContext()));
        mDoctorRefreshLayout.setEnableRefresh(false);
        mDoctorRefreshLayout.setEnableLoadmore(true);
        mDoctorRefreshLayout.setOnLoadmoreListener(new OnLoadmoreListener() {
            @Override
            public void onLoadmore(RefreshLayout refreshlayout) {
                String uid = ((MainActivity) getActivity()).mDoctorListData.getUid();
                if (!TextUtils.isEmpty(uid)) {
                    Net.instance().getDoctorInfo(mUser.getUser_id(), uid, mUser.getToken(), new Net.OnNext<Result<Doctor>>() {
                                @Override
                                public void onNext(@NonNull Result<Doctor> result) {
                                    if (result.getCode() == 100) {
                                        ((MainActivity) getActivity()).mDoctorListData.addDoctor(result.getData());
                                    }
                                    mDoctorRefreshLayout.finishLoadmore();
                                }
                            },
                            new Net.OnError() {
                                @Override
                                public void onError(@NonNull Throwable e) {
                                    mDoctorRefreshLayout.finishLoadmore();
                                }
                            }, OnlineDoctorListFragment.class.getSimpleName());
                } else {
                    mDoctorRefreshLayout.finishLoadmore();
                }
            }
        });
    }

    private void setupContainer() {
        setupRefreshLayout();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mDoctorListContainer.setLayoutManager(linearLayoutManager);
        mAdapter = new DoctorListAdapter((MainActivity)getActivity());
        mDoctorListContainer.setAdapter(mAdapter);
        ((MainActivity) getActivity()).mDoctorListData.setAdapter(mAdapter);
    }

}
