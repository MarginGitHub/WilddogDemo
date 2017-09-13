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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.wilddog.client.SyncReference;
import com.wilddog.client.WilddogSync;
import com.zd.wilddogdemo.R;
import com.zd.wilddogdemo.beans.Login;
import com.zd.wilddogdemo.beans.User;
import com.zd.wilddogdemo.net.Net;
import com.zd.wilddogdemo.net.NetServiceConfig;
import com.zd.wilddogdemo.service.DialService;
import com.zd.wilddogdemo.storage.ObjectPreference;
import com.zd.wilddogdemo.ui.user.MainActivity;
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
    @BindView(R.id.password)
    EditText mPassword;
    @BindView(R.id.register)
    TextView mRegister;
    @BindView(R.id.avatar_iv)
    ImageView mAvatarIv;
    @BindView(R.id.auto_login)
    CheckBox mAutoLogin;
    @BindView(R.id.nick_name_tv)
    TextView mNickNameTv;

    private Login mLogin;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        mLogin = ObjectPreference.getObject(this, Login.class);
        initViews();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            initPermissions();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RegisterActivity.RESULT_CODE && data != null) {
            mLogin = (Login) data.getSerializableExtra("login");
            initViews();
            ObjectPreference.saveObject(this, mLogin);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Net.instance().removeRequest(LoginActivity.class.getSimpleName());
    }

    private void initViews() {
        mRegister.setEnabled(true);
        if ((mLogin != null) && (!TextUtils.isEmpty(mLogin.getMobile()))) {
            if (mLogin.isAutoLogin()) {
                login(mLogin.getMobile(), mLogin.getPassword(), true);
            }
            Util.setImageView(this, mAvatarIv, NetServiceConfig.HEAD_IMAGE_BASE_URL + mLogin.getAvatarUrl());
            mNickNameTv.setText(mLogin.getNickName());
            mPhone.setText(mLogin.getMobile());
            mPassword.setText(mLogin.getPassword());
            mAutoLogin.setChecked(mLogin.isAutoLogin());
        }
    }


    private void loginWithCheck() {
        boolean autoLogin = mAutoLogin.isChecked();
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

        login(mobile, pwd, autoLogin);

    }

    private void login(final String mobile, final String pwd, final boolean autoLogin) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("正在登录中，请稍等");
        dialog.show();
        Net.instance().login(mobile, pwd,
                new Net.OnNext<User>() {
                    @Override
                    public void onNext(@NonNull User user) {
//                        首先保存登录信息
                        if (mLogin == null || !mLogin.getMobile().equals(mobile) ||
                                !mLogin.getPassword().equals(pwd) || mLogin.isAutoLogin() != autoLogin) {
                            Login login = new Login(user.getNick_name(), mobile, pwd, user.getHead_img_url(), autoLogin);
                            ObjectPreference.saveObject(LoginActivity.this, login);
                        }
//                        保存用户信息
                        Util.saveUser(getApplicationContext(), user);
                        syncUserData(user);
                        startVideoService();
                        Intent intent;
                        intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                        dialog.dismiss();
                    }
                },
                new Net.OnError() {
                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.d("login", "onError: " + e.toString());
                        Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }
                }, LoginActivity.class.getSimpleName());
    }

    private void syncUserData(final User user) {
//        野狗方面数据的存储
        WilddogSync.goOnline();
        SyncReference reference;
        reference = WilddogSync.getInstance()
                .getReference(getResources()
                        .getString(R.string.users_room));
        HashMap<String, Object> map = new HashMap();
        map.put(user.getUser_id(), true);
        reference.updateChildren(map);
        reference.child(user.getUser_id()).onDisconnect().removeValue();

    }


    private void startVideoService() {
//        开启保活Service
//        startService(new Intent(this, AliveService.class));
//        判断是医生还是用户，以此来开启相应的Service
        Intent intent = new Intent(this, DialService.class);
        startService(intent);
    }

    @OnClick({R.id.sign_in_button, R.id.register})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.sign_in_button:
                loginWithCheck();
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

