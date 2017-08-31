package com.zd.wilddogdemo;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.wilddog.client.SyncReference;
import com.wilddog.client.WilddogSync;
import com.wilddog.video.WilddogVideo;
import com.wilddog.wilddogauth.WilddogAuth;
import com.wilddog.wilddogauth.core.Task;
import com.wilddog.wilddogauth.core.listener.OnCompleteListener;
import com.wilddog.wilddogauth.core.result.AuthResult;
import com.wilddog.wilddogauth.model.WilddogUser;
import com.wilddog.wilddogcore.WilddogApp;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
    }


    @OnClick(R.id.sign_in_button)
    public void onViewClicked() {

        String phone = mPhone.getText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "电话号码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        NetService service = (NetService) NetServiceProvider.instance(this)
                .provider(NetService.class, NetServiceConfig.SERVER_BASE_URL);
        String ts = String.valueOf(System.currentTimeMillis() / 1000);
        String apiKey = "test";
        String mobile = phone;
        String password = "12345678";
        int flag = 1;
        String sign = Util.sign(ts, apiKey, mobile, password, flag);
        service.login(ts, apiKey, sign, mobile, password, flag)
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
                                String token = loginInfo.getWilddog_token();

                                wilddogLogin(token);

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
    }

    private void wilddogLogin(String token) {
        WilddogAuth.getInstance(WilddogApp.getInstance()).signInWithCustomToken(token).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(Task<AuthResult> authResultTask) {
                if (authResultTask.isSuccessful()) {
                    WilddogUser wilddogUser = authResultTask.getResult().getWilddogUser();
                    String uid = wilddogUser.getUid();
                    String wilddogToken = wilddogUser.getToken(false).getResult().getToken();
                    syncUserData(uid);
                    initWilddogVideo(wilddogToken);
                    startVideoService(uid);

                    Intent intent = new Intent(LoginActivity.this, UserListActivity.class);
                    intent.putExtra("uid", uid);
                    startActivity(intent);
                }
            }
        });
    }

    private void syncUserData(String uid) {
        SyncReference reference = WilddogSync.getInstance().getReference("userlist");
        HashMap<String, Object> map = new HashMap();
        map.put(uid, true);
        reference.updateChildren(map);
        reference.child(uid).onDisconnect().removeValue();
    }

    private void initWilddogVideo(String token) {
        WilddogVideo.initialize(getApplicationContext(), getResources().getString(R.string.video_app_id), token);
        WilddogVideo.getInstance().start();
    }

    private void startVideoService(String uid) {
        Intent intent = new Intent(this, WilddogVideoService.class);
        intent.putExtra("uid", uid);
        startService(intent);
    }
}

