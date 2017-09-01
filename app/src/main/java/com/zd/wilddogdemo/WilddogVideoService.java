package com.zd.wilddogdemo;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.Nullable;

import com.wilddog.video.Conversation;
import com.wilddog.video.WilddogVideo;
import com.wilddog.video.WilddogVideoError;

/**
 * Created by dongjijin on 2017/8/30 0030.
 */

public class WilddogVideoService extends Service {

    private WilddogVideo.Listener mListener;
    private Conversation mVideoConversation;
    private String mUid;
    private KeepAliveBroadcastReceiver mReceiver;


    @Override
    public void onCreate() {
        super.onCreate();
        startKeepAliveBroadcastReceiver();
    }

    @Override
    public void onDestroy() {
        stopKeepAliveBroadcastReceiver();
        WilddogVideo.getInstance().stop();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            mUid = intent.getStringExtra("uid");
            initWilddogVideo(intent.getStringExtra("token"));
        }
        return START_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mListener = null;
        mVideoConversation = null;
        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new ConversionBinder();
    }


    private void initWilddogVideo(String token) {
        WilddogVideo.initialize(getApplicationContext(), getResources().getString(R.string.video_app_id), token);
        WilddogVideo.getInstance().setListener(new WilddogVideo.Listener() {
            @Override
            public void onCalled(Conversation conversation, String s) {
                if (mListener != null) {
                    mListener.onCalled(conversation, s);
                } else {
                    mVideoConversation = conversation;
                    Intent intent = new Intent(getApplicationContext(), UserListActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("uid", mUid);
                    intent.putExtra("wake_up", true);
                    startActivity(intent);
                }
            }

            @Override
            public void onTokenError(WilddogVideoError wilddogVideoError) {
                if (mListener != null) {
                    mListener.onTokenError(wilddogVideoError);
                } else {

                }
            }
        });
        WilddogVideo.getInstance().start();
    }

    private void startKeepAliveBroadcastReceiver() {
        mReceiver = new KeepAliveBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(mReceiver, filter);
    }

    private void stopKeepAliveBroadcastReceiver() {
        unregisterReceiver(mReceiver);
    }

    class ConversionBinder extends Binder {

        public void setVideoListener(WilddogVideo.Listener listener) {
            mListener = listener;
        }

        public Conversation getVideoConversation() {
            return mVideoConversation;
        }
    }

}
