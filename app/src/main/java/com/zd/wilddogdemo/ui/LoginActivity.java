package com.zd.wilddogdemo.ui;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.wilddog.client.SyncReference;
import com.wilddog.client.WilddogSync;
import com.wilddog.wilddogauth.WilddogAuth;
import com.wilddog.wilddogauth.core.Task;
import com.wilddog.wilddogauth.core.listener.OnCompleteListener;
import com.wilddog.wilddogauth.core.result.AuthResult;
import com.wilddog.wilddogcore.WilddogApp;
import com.zd.wilddogdemo.R;
import com.zd.wilddogdemo.beans.Login;
import com.zd.wilddogdemo.beans.Result;
import com.zd.wilddogdemo.beans.User;
import com.zd.wilddogdemo.net.Net;
import com.zd.wilddogdemo.service.AliveService;
import com.zd.wilddogdemo.service.DialService;
import com.zd.wilddogdemo.service.VideoReceiverService;
import com.zd.wilddogdemo.storage.ObjectPreference;
import com.zd.wilddogdemo.utils.Util;

import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.annotations.NonNull;
import pub.devrel.easypermissions.EasyPermissions;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private static final int RC_AUDIO_CAMERA = 100;
    private static final int REQUEST_CODE = 1;
    @BindView(R.id.phone)
    AutoCompleteTextView mPhone;
    @BindView(R.id.doctor_cb)
    CheckBox mDoctorCb;
    @BindView(R.id.password)
    EditText mPassword;
    @BindView(R.id.register)
    TextView mRegister;
    private User mUser;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        initViews();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            initPermissions();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RegisterActivity.RESULT_CODE) {
            initViews();
        }
    }

    private void initViews() {
        mUser = ObjectPreference.getObject(this, User.class);
        mRegister.setEnabled(true);
        mDoctorCb.setFocusable(true);
        if ((mUser != null) && (!TextUtils.isEmpty(mUser.getMobile()))) {
            mPhone.setText(mUser.getMobile());
            mPassword.setText(mUser.getPwd());
        }
        mDoctorCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    mRegister.setVisibility(View.INVISIBLE);
                } else {
                    mRegister.setVisibility(View.VISIBLE);
                }
            }
        });
    }


    private void login() {
        final boolean isDoctor = mDoctorCb.isChecked();
        final String mobile = mPhone.getText().toString().trim();
        final String pwd = mPassword.getText().toString().trim();
        if (!Util.isPhoneValid(mobile)) {
            Toast.makeText(this, "电话号码格式不正确", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Util.isPasswordValid(pwd)) {
            Toast.makeText(this, "密码长度最少为6位且不能有特殊字符", Toast.LENGTH_SHORT).show();
            return;
        }

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("正在登录中，请稍等");
        mProgressDialog.show();

        if (mUser == null || mUser.isOverdue() || mUser.isDoctor() != isDoctor
                || !mUser.getMobile().equals(mobile) || !mUser.getPwd().equals(pwd)) {
            Net.instance().login(mobile, pwd,
                    new Net.OnNext<Result<User>>() {
                        @Override
                        public void onNext(@NonNull Result<User> result) {
                            if (result.getCode() == 100) {
                                User user = result.getData();
                                user.setDoctor(isDoctor);
                                user.setPwd(pwd);
                                wilddogLogin(user);
                            }
                        }
                    },
                    new Net.OnError() {
                        @Override
                        public void onError(@NonNull Throwable e) {
                            Log.d("login", "onError: " + e.toString());
                            mProgressDialog.dismiss();
                        }
                    });
        } else {
            wilddogLogin(mUser);
        }
    }

    private void wilddogLogin(final User user) {
        Net.instance().wilddogLogin(user, new Net.OnNext<Task<AuthResult>>() {
                    @Override
                    public void onNext(@NonNull Task<AuthResult> result) {
                        if (result.isSuccessful()) {
                            String token = result.getResult().getWilddogUser().getToken(false).getResult().getToken();
                            user.setWilddogVideoToken(token);
                            syncUserData(user);
                            startVideoService(user);
                            Intent intent;
                            if (user.isDoctor()) {
                                intent = new Intent(LoginActivity.this, DoctorActivity.class);
                                intent.putExtra("user", user);
                            } else {
                                intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.putExtra("user", user);
                            }
                            startActivity(intent);
                            finish();
                        }
                        mProgressDialog.dismiss();
                    }
                },
                new Net.OnError() {
                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.d("wilddogLogin", "onError: " + e.toString());
                        mProgressDialog.dismiss();
                    }
                });
    }

    private void syncUserData(final User user) {
//        保存用户信息到本地
        ObjectPreference.saveObject(LoginActivity.this, user);
//        野狗方面数据的存储
        WilddogSync.goOnline();
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
        map.put(user.getUser_id(), true);
        reference.updateChildren(map);
        reference.child(user.getUser_id()).onDisconnect().removeValue();

    }


    private void startVideoService(User user) {
//        开启保活Service
//        startService(new Intent(this, AliveService.class));
//        判断是医生还是用户，以此来开启相应的Service
        Intent intent;
        if (user.isDoctor()) {
            intent = new Intent(this, VideoReceiverService.class);
        } else {
            intent = new Intent(this, DialService.class);
        }
        intent.putExtra("user", user);
        startService(intent);
    }

    @OnClick({R.id.sign_in_button, R.id.register})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.sign_in_button:
                login();
                break;
            case R.id.register:
                startActivityForResult(new Intent(this, RegisterActivity.class), REQUEST_CODE);
                break;
        }
    }

    private void initPermissions() {
        String[] perms = {Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA,
                Manifest.permission.MODIFY_AUDIO_SETTINGS, Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_CONTACTS, Manifest.permission.GET_ACCOUNTS,
                Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, "需要音视频存储相关权限", RC_AUDIO_CAMERA, perms);
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        finish();
    }
}

