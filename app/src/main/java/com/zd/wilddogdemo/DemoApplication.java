package com.zd.wilddogdemo;

import android.app.Application;

import com.wilddog.wilddogcore.WilddogApp;
import com.wilddog.wilddogcore.WilddogOptions;

/**
 * Created by dongjijin on 2017/8/28 0028.
 */

public class DemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initWilddogApp();
    }

    private void initWilddogApp() {
        WilddogOptions.Builder builder = new WilddogOptions.Builder().setSyncUrl(getResources().getString(R.string.sync_url));
        WilddogOptions options = builder.build();
        WilddogApp.initializeApp(this, options);
    }
}
