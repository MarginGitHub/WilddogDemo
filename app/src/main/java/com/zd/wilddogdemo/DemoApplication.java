package com.zd.wilddogdemo;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.wilddog.wilddogauth.WilddogAuth;
import com.wilddog.wilddogcore.WilddogApp;
import com.wilddog.wilddogcore.WilddogOptions;
import com.zd.wilddogdemo.net.Net;
import com.zd.wilddogdemo.storage.memory.ObjectProvider;
import com.zd.wilddogdemo.utils.Util;

/**
 * Created by dongjijin on 2017/8/28 0028.
 */

public class DemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Net.init(getApplicationContext());
        initWilddogApp();
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle bundle) {
                Util.pushActivity(activity);
            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                Util.removeActivity(activity);
            }
        });
    }

    private void initWilddogApp() {
        WilddogOptions.Builder builder = new WilddogOptions.Builder().setSyncUrl(getResources().getString(R.string.sync_url));
        WilddogOptions options = builder.build();
        WilddogApp.initializeApp(this, options);
    }


}
