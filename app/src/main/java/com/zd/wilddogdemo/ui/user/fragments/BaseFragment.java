package com.zd.wilddogdemo.ui.user.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zd.wilddogdemo.beans.User;
import com.zd.wilddogdemo.ui.LoginActivity;
import com.zd.wilddogdemo.utils.Util;

/**
 * Created by dongjijin on 2017/9/12 0012.
 */

public  abstract class BaseFragment extends Fragment {
    protected User mUser;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mUser = Util.getUser(getContext());
        if (mUser == null) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            Util.clearActivityStack();
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mUser == null) {
            mUser = Util.getUser(getContext());
            if (mUser == null) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                Util.clearActivityStack();
            }
        }
    }

    protected abstract void initViews();

}
