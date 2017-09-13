package com.zd.wilddogdemo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.zd.wilddogdemo.beans.User;
import com.zd.wilddogdemo.utils.Util;

/**
 * Created by dongjijin on 2017/9/12 0012.
 */

public abstract class BaseActivity extends AppCompatActivity {
    protected User mUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUser = Util.getUser(getApplicationContext());
        if (mUser == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            Util.clearActivityStack();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mUser == null) {
            mUser = Util.getUser(getApplicationContext());
            if (mUser == null) {
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                Util.clearActivityStack();
            }
        }
    }

    protected abstract void initViews();
}
