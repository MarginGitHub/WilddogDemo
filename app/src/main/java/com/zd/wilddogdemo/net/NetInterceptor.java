package com.zd.wilddogdemo.net;


import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by margin on 17-9-8.
 */

public class NetInterceptor implements Interceptor {
    private static final String TAG = "NetInterceptor";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);
        return response;
    }

}
