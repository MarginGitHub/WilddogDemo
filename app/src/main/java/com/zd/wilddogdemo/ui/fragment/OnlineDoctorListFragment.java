package com.zd.wilddogdemo.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.zd.wilddogdemo.adapter.DoctorListAdapter;
import com.zd.wilddogdemo.net.NetService;
import com.zd.wilddogdemo.net.NetServiceConfig;
import com.zd.wilddogdemo.net.NetServiceProvider;
import com.zd.wilddogdemo.ui.MainActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by dongjijin on 2017/9/6 0006.
 */

public class OnlineDoctorListFragment extends Fragment {

    @BindView(R.id.doctor_list_container)
    RecyclerView mDoctorListContainer;
    @BindView(R.id.doctor_refresh_layout)
    SmartRefreshLayout mDoctorRefreshLayout;
    Unbinder unbinder;
    private NetService mNetService;
    private DoctorListAdapter mAdapter;
    private boolean isViewDestroied;

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
        mNetService = (NetService) NetServiceProvider.instance(getActivity())
                .provider(NetService.class, NetServiceConfig.SERVER_BASE_URL);
        isViewDestroied = false;
        setupContainer();
        return view;
    }

    @Override
    public void onDestroyView() {
        isViewDestroied = true;
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public boolean isViewDestroied() {
        return isViewDestroied;
    }

    public boolean isViewFull() {
        int position = mAdapter.getItemCount() - 1;
        int visibleItemPosition = ((LinearLayoutManager) mDoctorListContainer.getLayoutManager())
                .findLastVisibleItemPosition();
        return visibleItemPosition < position;
    }

    public DoctorListAdapter getDoctorListAdapter() {
        return  mAdapter;
    }

    private void setupRefreshLayout() {
        mDoctorRefreshLayout.setRefreshFooter(new ClassicsFooter(getContext()));
        mDoctorRefreshLayout.setEnableRefresh(false);
        mDoctorRefreshLayout.setEnableLoadmore(true);
        mDoctorRefreshLayout.setOnLoadmoreListener(new OnLoadmoreListener() {
            @Override
            public void onLoadmore(RefreshLayout refreshlayout) {
                mDoctorRefreshLayout.finishLoadmore();
            }
        });
    }

    private void setupContainer() {
        setupRefreshLayout();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mDoctorListContainer.setLayoutManager(linearLayoutManager);
        mAdapter = new DoctorListAdapter((MainActivity)getActivity());
        mAdapter.setData(((MainActivity)getActivity()).mOnlineDoctorList);
        mDoctorListContainer.setAdapter(mAdapter);
    }

}
