package com.zd.wilddogdemo.ui;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.Toast;

import com.wilddog.client.SyncReference;
import com.wilddog.client.WilddogSync;
import com.wilddog.wilddogauth.WilddogAuth;
import com.wilddog.wilddogauth.core.Task;
import com.wilddog.wilddogauth.core.listener.OnCompleteListener;
import com.wilddog.wilddogauth.core.result.AuthResult;
import com.wilddog.wilddogauth.model.WilddogUser;
import com.wilddog.wilddogcore.WilddogApp;
import com.zd.wilddogdemo.DemoApplication;
import com.zd.wilddogdemo.R;
import com.zd.wilddogdemo.beans.Doctor;
import com.zd.wilddogdemo.beans.LoginInfo;
import com.zd.wilddogdemo.beans.Result;
import com.zd.wilddogdemo.beans.User;
import com.zd.wilddogdemo.net.NetService;
import com.zd.wilddogdemo.net.NetServiceConfig;
import com.zd.wilddogdemo.net.NetServiceProvider;
import com.zd.wilddogdemo.service.WilddogVideoService;
import com.zd.wilddogdemo.storage.ObjectPreference;
import com.zd.wilddogdemo.utils.Util;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.phone)
    AutoCompleteTextView mPhone;
    @BindView(R.id.doctor_cb)
    CheckBox mDoctorCb;
    private User mUser;
    private NetService mService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        mUser = ObjectPreference.getObject(this, User.class);
        if ((mUser != null) && (!TextUtils.isEmpty(mUser.getPhone()))) {
            mPhone.setText(mUser.getPhone());
        }
    }


    @OnClick(R.id.sign_in_button)
    public void onViewClicked() {
//        final boolean isDoctor = mDoctorCb.isChecked();
//        WilddogAuth.getInstance().signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//            @Override
//            public void onComplete(Task<AuthResult> var1) {
//                if (var1.isSuccessful()) {
//                    WilddogUser wilddogUser = var1.getResult().getWilddogUser();
//                    User u = new User();
//                    u.setPhone("");
//                    u.setDoctor(isDoctor);
//                    u.setUid(wilddogUser.getUid());
//                    u.setToken(wilddogUser.getToken(false).getResult().getToken());
//                    u.setLast_login_time(System.currentTimeMillis() / 100);
//                    syncUserData(u);
//                    startVideoService(u);
//
//                    Intent intent;
//                    if (u.isDoctor()) {
//                        intent = new Intent(LoginActivity.this, DoctorActivity.class);
//                    } else {
//                        intent = new Intent(LoginActivity.this, UserActivity.class);
//                    }
//                    intent.putExtra("user", u);
//                    startActivity(intent);
//                }
//            }
//        });

        final boolean isDoctor = mDoctorCb.isChecked();
        final String phone = mPhone.getText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "电话号码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mUser == null || mUser.isOverdue() || mUser.isDoctor() != isDoctor || !mUser.getPhone().equals(phone)) {
            mService = (NetService) NetServiceProvider.instance(this)
                    .provider(NetService.class, NetServiceConfig.SERVER_BASE_URL);

            String ts = String.valueOf(System.currentTimeMillis() / 1000);
            String apiKey = "test";
            String mobile = phone;
            String password = "12345678";
            int flag = 1;

            HashMap<String, String> params = new HashMap<>();
            params.put("ts", ts);
            params.put("apiKey", apiKey);
            params.put("mobile", phone);
            params.put("password", password);
            params.put("flag", String.valueOf(flag));
            String sign = Util.sign(params);
            mService.login(ts, apiKey, sign, mobile, password, flag)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Result<LoginInfo>>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {

                        }

                        @Override
                        public void onNext(@NonNull Result<LoginInfo> loginInfoResult) {
                            switch (loginInfoResult.getCode()) {
                                case 100:
                                    LoginInfo loginInfo = loginInfoResult.getData();
                                    Log.d("login", loginInfo.toString());
                                    User u = new User();
                                    u.setUid(loginInfo.getUser_id());
                                    u.setWilddog_login_token(loginInfo.getWilddog_token());
                                    u.setLast_login_time(System.currentTimeMillis() / 1000);
                                    u.setDoctor(isDoctor);
                                    u.setPhone(phone);
                                    wilddogLogin(u);

                                    break;
                                case 101:
                                    Log.d("login", loginInfoResult.getMsg());
                                    break;
                                default:
                                    Log.d("login", loginInfoResult.toString());
                                    break;
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        } else {
            wilddogLogin(mUser);
        }


    }

    private void wilddogLogin(final User user) {
        WilddogAuth.getInstance(WilddogApp.getInstance())
                .signInWithCustomToken(user.getWilddog_login_token())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(Task<AuthResult> authResultTask) {
                if (authResultTask.isSuccessful()) {
                    String token = authResultTask.getResult().getWilddogUser().getToken(false).getResult().getToken();
                    user.setToken(token);
                    syncUserData(user);
                    startVideoService(user);
                    Intent intent;
                    if (user.isDoctor()) {
                        intent = new Intent(LoginActivity.this, DoctorActivity.class);
                    } else {
                        intent = new Intent(LoginActivity.this, UserActivity.class);
                    }
                    intent.putExtra("user", user);
                    startActivity(intent);
                    ObjectPreference.saveObject(LoginActivity.this, user);
                }
            }
        });
    }

    private void syncUserData(final User user) {
//        if (user.isDoctor()) {
//            String ts = String.valueOf(System.currentTimeMillis() / 1000);
//            String apiKey = "test";
//
//            HashMap<String, String> params = new HashMap<>();
//            params.put("ts", ts);
//            params.put("apiKey", apiKey);
//            params.put("userId", user.getUid());
//            params.put("docId", user.getUid());
//            String sign = Util.sign(params);
//            mService.getDoctorInfo(ts, apiKey, sign, user.getUid(), user.getUid())
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(new Observer<Result<Doctor>>() {
//                        @Override
//                        public void onSubscribe(@NonNull Disposable d) {
//
//                        }
//
//                        @Override
//                        public void onNext(@NonNull Result<Doctor> doctorResult) {
//                            if (doctorResult.getCode() == 100) {
//                                Doctor doctor = doctorResult.getData();
//                                SyncReference reference = WilddogSync.getInstance()
//                                                .getReference(getResources()
//                                                .getString(R.string.doctors_room));
//                                HashMap<String, Object> map = new HashMap();
//                                map.put(user.getUid(), doctor);
//                                reference.updateChildren(map);
//                                reference.child(user.getUid()).onDisconnect().removeValue();
//                            }
//                        }
//
//                        @Override
//                        public void onError(@NonNull Throwable e) {
//                            Log.d("getDoctorInfo", "onError: " + e.toString());
//                        }
//
//                        @Override
//                        public void onComplete() {
//                            Log.d("getDoctorInfo", "onComplete: ");
//                        }
//                    });
//        } else {
//            SyncReference reference = WilddogSync.getInstance()
//                    .getReference(getResources()
//                            .getString(R.string.users_room));
//            HashMap<String, Object> map = new HashMap();
//            map.put(user.getUid(), true);
//            reference.updateChildren(map);
//            reference.child(user.getUid()).onDisconnect().removeValue();
//        }

        SyncReference reference;
        if (user.isDoctor()) {
            reference = WilddogSync.getInstance()
                    .getReference(getResources()
                            .getString(R.string.doctors_room));
        } else {
            reference = WilddogSync.getInstance()
                    .getReference(getResources()
                            .getString(R.string.users_room));
        }
        HashMap<String, Object> map = new HashMap();
        map.put(user.getUid(), true);
        reference.updateChildren(map);
        reference.child(user.getUid()).onDisconnect().removeValue();

    }


    private void startVideoService(User user) {
        Intent intent = new Intent(this, WilddogVideoService.class);
        intent.putExtra("user", user);
        startService(intent);
    }
}

