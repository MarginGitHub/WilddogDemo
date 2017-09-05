package com.zd.wilddogdemo.net;

import com.zd.wilddogdemo.beans.Doctor;
import com.zd.wilddogdemo.beans.LoginInfo;
import com.zd.wilddogdemo.beans.Result;

import java.util.List;

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

    @GET("doctor/getList")
    Observable<Result<List<Doctor>>> getDoctorList(
            @Query("ts") String ts, @Query("apiKey") String apiKey, @Query("sign") String sign,
            @Query("userId") String userId, @Query("start") int start, @Query("count") int count);

    @GET("doctor/getInfo")
    Observable<Result<Doctor>> getDoctorInfo(
            @Query("ts") String ts, @Query("apiKey") String apiKey, @Query("sign") String sign,
            @Query("userId") String userId, @Query("docId") String docId);

}
