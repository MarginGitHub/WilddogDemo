package com.zd.wilddogdemo;

import android.app.Application;

import com.wilddog.wilddogauth.WilddogAuth;
import com.wilddog.wilddogcore.WilddogApp;
import com.wilddog.wilddogcore.WilddogOptions;
import com.zd.wilddogdemo.net.Net;

/**
 * Created by dongjijin on 2017/8/28 0028.
 */

public class DemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Net.init(getApplicationContext());
        initWilddogApp();
    }

    private void initWilddogApp() {
        WilddogOptions.Builder builder = new WilddogOptions.Builder().setSyncUrl(getResources().getString(R.string.sync_url));
        WilddogOptions options = builder.build();
        WilddogApp.initializeApp(this, options);
    }
}
