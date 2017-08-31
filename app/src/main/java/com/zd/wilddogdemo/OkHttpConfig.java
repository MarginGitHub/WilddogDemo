package com.zd.wilddogdemo;

import android.content.Context;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;

/**
 * Created by dongjijin on 2017/8/7 0007.
 */

public class OkHttpConfig {
    static private final String DEFAULT_CACHE_DIR = "OkHttpCache";
    static private final int DEFAULT_CACHE_SIZE = 8 * 1024 * 1024;
    private static OkHttpClient sInstance;
    private static Cache sCache;

    public static OkHttpClient client(Context appContext) {
        if (null == sInstance) {
            synchronized (OkHttpConfig.class) {
                if (null == sInstance) {
//                    mInstance = getDefaultBuilder().sslSocketFactory(SslContextFactory.getSslSocket(appContext)).build();
                    sInstance = getDefaultBuilder().build();
                }
            }
        }
        return sInstance;
    }

    private OkHttpConfig() {
    }

    static public void createCache(File appCacheDir){
        synchronized (OkHttpConfig.class){
            if (null == sCache){
                File cacheDir = new File(appCacheDir, DEFAULT_CACHE_DIR);
                sCache = new Cache(cacheDir, DEFAULT_CACHE_SIZE);
            }
        }
    }

    static public OkHttpClient.Builder getDefaultBuilder(){
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .cache(sCache);
        return builder;
    }
}
