package com.zd.wilddogdemo.net;

import android.content.Context;
import android.util.Log;

import com.wilddog.wilddogauth.WilddogAuth;
import com.wilddog.wilddogauth.core.Task;
import com.wilddog.wilddogauth.core.listener.OnCompleteListener;
import com.wilddog.wilddogauth.core.result.AuthResult;
import com.wilddog.wilddogcore.WilddogApp;
import com.zd.wilddogdemo.beans.Doctor;
import com.zd.wilddogdemo.beans.Result;
import com.zd.wilddogdemo.beans.User;
import com.zd.wilddogdemo.utils.Util;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;


/**
 * Created by dongjijin on 2017/9/8 0008.
 */

public class Net {
    private static final String TAG = Net.class.getSimpleName();
    private static Net instance;
    private HashMap<String, List<Subscription>> mSubscriptionHashMap;
    private NetService mNetService;

    private Net(Context context) {
        mNetService = (NetService) NetServiceProvider.instance(context)
                .provider(NetService.class, NetServiceConfig.SERVER_BASE_URL);
        mSubscriptionHashMap = new HashMap<>();
    }

    public static void init(Context context) {
        instance = new Net(context);
    }

    public static Net instance() {
        return instance;
    }

    private void addSubscription(String requestID, Subscription s) {
        List<Subscription> subscriptionList = mSubscriptionHashMap.get(requestID);
        if (subscriptionList != null) {
            subscriptionList.add(s);
        } else {
            subscriptionList = new ArrayList<>();
            subscriptionList.add(s);
        }
        mSubscriptionHashMap.put(requestID, subscriptionList);
    }

    public void register(String mobile, String password, final String ref, final OnNext<Result<User>> next,
                           final OnError error) {
        String ts = String.valueOf(System.currentTimeMillis() / 1000);
        String apiKey = "test";
        HashMap<String, String> params = new HashMap<>();
        params.put("ts", ts);
        params.put("apiKey", apiKey);
        params.put("mobile", mobile);
        params.put("password", password);
        params.put("ref", ref);
        String sign = Util.sign(params);
        mNetService.register(ts, apiKey, sign, mobile, password, ref)
                .take(1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Result<User>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                    }

                    @Override
                    public void onNext(@NonNull Result<User> userResult) {
                        next.onNext(userResult);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        error.onError(e);
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: ");
                    }
                });
    }

    public void login(String mobile, String password, final OnNext<User> next,
                      final OnError err) {
        String ts = String.valueOf(System.currentTimeMillis() / 1000);
        String apiKey = "test";
        int flag = 1;

        HashMap<String, String> params = new HashMap<>();
        params.put("ts", ts);
        params.put("apiKey", apiKey);
        params.put("mobile", mobile);
        params.put("password", password);
        params.put("flag", String.valueOf(flag));
        String sign = Util.sign(params);
        mNetService.login(ts, apiKey, sign, mobile, password, flag)
                .flatMap(new Function<Result<User>, ObservableSource<User>>() {
                    @Override
                    public ObservableSource<User> apply(@NonNull final Result<User> userResult) throws Exception {
                        if (userResult.isSuccessful()) {
                            final User user = userResult.getData();
                            return new Observable<User>() {
                                @Override
                                protected void subscribeActual(final Observer<? super User> observer) {
                                    WilddogAuth.getInstance(WilddogApp.getInstance())
                                            .signInWithCustomToken(user.getWilddog_token())
                                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                @Override
                                                public void onComplete(Task<AuthResult> authResultTask) {
                                                    if (authResultTask.isSuccessful()) {
                                                        String token = authResultTask.getResult()
                                                                .getWilddogUser()
                                                                .getToken(false)
                                                                .getResult()
                                                                .getToken();
                                                        user.setWilddogVideoToken(token);
                                                        observer.onNext(user);
                                                        observer.onComplete();
                                                    } else {
                                                        observer.onError(new Throwable("登录失败", authResultTask.getException()));
                                                    }
                                                }
                                            });
                                }
                            };
                        }
                        throw new Exception(userResult.getMsg());
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<User>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull User user) {
                        next.onNext(user);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        err.onError(e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });

//                .subscribe(new Observer<Result<User>>() {
//                    @Override
//                    public void onSubscribe(@NonNull Disposable d) {
//
//                    }
//
//                    @Override
//                    public void onNext(@NonNull Result<User> userResult) {
//                        next.onNext(userResult);
//                    }
//
//                    @Override
//                    public void onError(@NonNull Throwable e) {
//                        err.onError(e);
//                    }
//
//                    @Override
//                    public void onComplete() {
//
//                    }
//                });
    }

    public void wilddogLogin(final User user, final OnNext<Boolean> next, final OnError err) {
        Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(@NonNull final ObservableEmitter<Boolean> e) throws Exception {
                WilddogAuth.getInstance(WilddogApp.getInstance())
                        .signInWithCustomToken(user.getWilddog_token())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(Task<AuthResult> authResultTask) {
                                if (authResultTask.isSuccessful()) {
                                    e.onNext(true);
                                    e.onComplete();
                                } else {
                                    e.onError(new Throwable("登录失败", authResultTask.getException()));
                                }
                            }
                        });
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull Boolean success) {
                        next.onNext(success);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        err.onError(e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void getDoctorInfo(String userId, String docId, String token, final OnNext<Result<Doctor>> next, final OnError err) {
        String ts = String.valueOf(System.currentTimeMillis() / 1000);
        String apiKey = "test";

        HashMap<String, String> params = new HashMap<>();
        params.put("ts", ts);
        params.put("apiKey", apiKey);
        params.put("token", token);
        params.put("userId", userId);
        params.put("docId", docId);
        String sign = Util.sign(params);
        mNetService.getDoctorInfo(ts, apiKey, sign, userId, docId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Result<Doctor>>() {
                    public static final String TAG = "getDoctorInfo";

                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull Result<Doctor> doctorResult) {
                        next.onNext(doctorResult);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        err.onError(e);
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: ");
                    }
                });
    }

    public void getUserInfo(String token, String userId, final OnNext<Result<User>> next, final OnError err) {
        String ts = String.valueOf(System.currentTimeMillis() / 1000);
        String apiKey = "test";
        HashMap<String, String> params = new HashMap<>();
        params.put("ts", ts);
        params.put("apiKey", apiKey);
        params.put("token", token);
        params.put("userId", userId);
        String sign = Util.sign(params);
        mNetService.getUserInfo(ts, apiKey, sign, userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Result<User>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull Result<User> userResult) {
                        next.onNext(userResult);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        err.onError(e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void uploadUserHeadImage(String token, String userId, String headUrl, final OnNext<Result<String>> next, final OnError err) {
        String ts = String.valueOf(System.currentTimeMillis() / 1000);
        String apiKey = "test";
        HashMap<String, String> params = new HashMap<>();
        params.put("ts", ts);
        params.put("apiKey", apiKey);
        params.put("token", token);
        params.put("userId", userId);
        String sign = Util.sign(params);
        File file = new File(headUrl);

        RequestBody tsBody = RequestBody.create(MediaType.parse("multipart/form-data"),ts);
        RequestBody apiKeyBody = RequestBody.create(MediaType.parse("multipart/form-data"),apiKey);
        RequestBody signBody = RequestBody.create(MediaType.parse("multipart/form-data"),sign);
        RequestBody userIdBody = RequestBody.create(MediaType.parse("multipart/form-data"),userId);
        RequestBody fileBody = RequestBody.create(MediaType.parse("image/png"),file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("upfile", file.getName(), fileBody);
        mNetService.uploadUserHeadImage(tsBody, apiKeyBody, signBody, userIdBody, part)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Result<String>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull Result<String> stringResult) {
                        next.onNext(stringResult);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        err.onError(e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void uploadDoctorHeadImage(String token, String userId, String headUrl, final OnNext<Result<String>> next, final OnError err) {
        String ts = String.valueOf(System.currentTimeMillis() / 1000);
        String apiKey = "test";
        HashMap<String, String> params = new HashMap<>();
        params.put("ts", ts);
        params.put("apiKey", apiKey);
        params.put("token", token);
        params.put("userId", userId);
        String sign = Util.sign(params);
        File file = new File(headUrl);

        RequestBody tsBody = RequestBody.create(MediaType.parse("multipart/form-data"),ts);
        RequestBody apiKeyBody = RequestBody.create(MediaType.parse("multipart/form-data"),apiKey);
        RequestBody signBody = RequestBody.create(MediaType.parse("multipart/form-data"),sign);
        RequestBody userIdBody = RequestBody.create(MediaType.parse("multipart/form-data"),userId);
        RequestBody fileBody = RequestBody.create(MediaType.parse("image/png"),file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("upfile", file.getName(), fileBody);
        mNetService.uploadDoctorHeadImage(tsBody, apiKeyBody, signBody, userIdBody, part)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Result<String>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull Result<String> stringResult) {
                        next.onNext(stringResult);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        err.onError(e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void uploadVideoConversationRecord(final String token, final String userId, String docId, long start, long duration,
                                              final OnNext<Result<User>> next, final OnError err) {
        final String ts = String.valueOf(System.currentTimeMillis() / 1000);
        final String apiKey = "test";
        HashMap<String, String> params = new HashMap<>();
        params.put("ts", ts);
        params.put("apiKey", apiKey);
        params.put("token", token);
        params.put("userId", userId);
        params.put("docId", docId);
        params.put("start", String.valueOf(start));
        params.put("duration", String.valueOf(duration));
        String sign = Util.sign(params);
        mNetService.uploadVideoConversationRecord(ts, apiKey, sign, userId, docId, start, duration)
                .flatMap(new Function<Result<Object>, ObservableSource<Result<User>>>() {
                    @Override
                    public ObservableSource<Result<User>> apply(@NonNull Result<Object> objectResult) throws Exception {
                        if (objectResult.getCode() == 100) {
                            HashMap<String, String> params = new HashMap<>();
                            params.put("ts", ts);
                            params.put("apiKey", apiKey);
                            params.put("token", token);
                            params.put("userId", userId);
                            String sign = Util.sign(params);
                            return mNetService.getUserInfo(ts, apiKey, sign, userId).subscribeOn(Schedulers.io());
                        } else {
                            try {
                                throw new Throwable("Error Code: " + objectResult.getCode() + ", Message: " + objectResult.getMsg());
                            } catch (Throwable throwable) {
                                throwable.printStackTrace();
                            }
                            return null;
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Observer<Result<User>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull Result<User> objectResult) {
                        next.onNext(objectResult);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        err.onError(e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


    public interface OnNext<T> {
        void onNext(@NonNull T result);
    }

    public interface OnError {
        void onError(@NonNull Throwable e);
    }

}
