package com.zd.wilddogdemo;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.wilddog.video.WilddogVideoView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by dongjijin on 2017/9/1 0001.
 */

public class VideoConversationActivity extends AppCompatActivity implements ServiceConnection {

    @BindView(R.id.remote_view)
    WilddogVideoView mRemoteView;
    @BindView(R.id.local_view)
    WilddogVideoView mLocalView;

    private boolean isBinded;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_conversation);
        ButterKnife.bind(this);
        bindVideoService();
    }

    private void bindVideoService() {
        Intent intent = new Intent(this, WilddogVideoService.class);
        bindService(intent, this, Service.BIND_AUTO_CREATE);
        isBinded = true;
    }

    private void unbindVideoService() {
        unbindService(this);
        isBinded = false;
    }

    @OnClick(R.id.hung_up)
    public void onViewClicked() {
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }
}
