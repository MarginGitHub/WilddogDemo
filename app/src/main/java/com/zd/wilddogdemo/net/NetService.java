package com.zd.wilddogdemo.net;

import com.zd.wilddogdemo.beans.Doctor;
import com.zd.wilddogdemo.beans.Result;
import com.zd.wilddogdemo.beans.User;

import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 * Created by dongjijin on 2017/8/10 0010.
 */

public interface NetService {

    @GET("auth/login")
    Observable<Result<User>> login(
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

    @GET("user/getinfo")
    Observable<Result<User>> getUserInfo(
            @Query("ts") String ts, @Query("apiKey") String apiKey, @Query("sign") String sign,
            @Query("userId") String userId
    );

    @GET("auth/register")
    Observable<Result<User>> register(
            @Query("ts") String ts, @Query("apiKey") String apiKey, @Query("sign") String sign,
            @Query("mobile") String mobile, @Query("password") String password, @Query("ref") String ref);

    @Multipart
    @POST("auth/uploadHead")
    Observable<Result<String>> uploadUserHeadImage(
            @Part("ts") RequestBody ts, @Part("apiKey") RequestBody apiKey, @Part("sign") RequestBody sign,
            @Part("userId") RequestBody userId, @Part MultipartBody.Part upfile);


    @Multipart
    @POST("doctor/uploadAD")
    Observable<Result<String>> uploadDoctorHeadImage(
            @Part("ts") RequestBody ts, @Part("apiKey") RequestBody apiKey, @Part("sign") RequestBody sign,
            @Part("userId") RequestBody userId, @Part MultipartBody.Part upfile);

    @GET("user/addVideoCall")
    Observable<Result<Object>> uploadVideoConversationRecord(
            @Query("ts") String ts, @Query("apiKey") String apiKey, @Query("sign") String sig,
            @Query("userId") String userId, @Query("docId") String docId, @Query("start") long start,
            @Query("duration") long duration );

}
