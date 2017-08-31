package com.zd.wilddogdemo;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by dongjijin on 2017/8/10 0010.
 */

public interface NetService {

    @GET("auth/login")
    Observable<Result<LoginInfo>> login(
            @Query("ts") String ts, @Query("apiKey") String apiKey, @Query("sign") String sign,
            @Query("mobile") String mobile, @Query("password") String password, @Query("flag") int flag);

}
