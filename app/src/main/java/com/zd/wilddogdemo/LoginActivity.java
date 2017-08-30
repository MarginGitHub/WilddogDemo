package com.zd.wilddogdemo;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.wilddog.client.SyncReference;
import com.wilddog.client.WilddogSync;
import com.wilddog.video.WilddogVideo;
import com.wilddog.wilddogauth.WilddogAuth;
import com.wilddog.wilddogauth.core.Task;
import com.wilddog.wilddogauth.core.listener.OnCompleteListener;
import com.wilddog.wilddogauth.core.result.AuthResult;

import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "Demo";
    private WilddogAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
    }


    @OnClick(R.id.sign_in_button)
    public void onViewClicked() {
        mAuth = WilddogAuth.getInstance();
        mAuth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(Task<AuthResult> authResult) {
                if (authResult.isSuccessful()) {
                    String uid = authResult.getResult().getWilddogUser().getUid();
                    syncUserData(uid);
                    initWilddogVideo();
                    Intent intent = new Intent(LoginActivity.this, UserListActivity.class);
                    intent.putExtra("uid", uid);
                    startActivity(intent);
                } else {
                    Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "onComplete: " + "登录失败");
                }
            }
        });
    }

    private void syncUserData(String uid) {
        SyncReference reference = WilddogSync.getInstance().getReference("users");
        HashMap<String, Object> map = new HashMap();
        map.put(uid, true);
        reference.updateChildren(map);
        reference.child(uid).onDisconnect().removeValue();
    }

    private void initWilddogVideo() {
        String token = WilddogAuth.getInstance().getCurrentUser().getToken(false).getResult().getToken();
        WilddogVideo.initialize(getApplicationContext(), getResources().getString(R.string.video_app_id), token);
    }
}

