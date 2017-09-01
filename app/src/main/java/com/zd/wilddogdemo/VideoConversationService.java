package com.zd.wilddogdemo;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import com.wilddog.video.Conversation;
import com.wilddog.video.LocalStream;
import com.wilddog.video.RemoteStream;
import com.wilddog.video.WilddogVideo;
import com.wilddog.video.WilddogVideoError;

/**
 * Created by dongjijin on 2017/8/30 0030.
 */

public class VideoConversationService extends Service {

    private KeepAliveBroadcastReceiver mReceiver;

    private Conversation mConversation;
    private LocalStream mLocalStream;
    private RemoteStream mRemoteStream;

    private boolean isBinded = false;
    private Messenger mClientMessenger;
    private Messenger mServiceMessenger = new Messenger(new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case VideoConversationCons.CALL:
                    break;
                case VideoConversationCons.CALLED:
                    break;
                case VideoConversationCons.CONNECTED:
                    mClientMessenger = message.replyTo;
                    break;
            }
            return true;
        }
    }));

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
            initWilddogVideo(intent.getStringExtra("token"));
        }
        return START_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        isBinded = false;
        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        isBinded = true;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        isBinded = true;
        return mServiceMessenger.getBinder();
    }


    private void initWilddogVideo(String token) {
        WilddogVideo.initialize(getApplicationContext(), getResources().getString(R.string.video_app_id), token);
        WilddogVideo.getInstance().setListener(new WilddogVideo.Listener() {
            @Override
            public void onCalled(Conversation conversation, String s) {
                if (isBinded) {
//                    如果绑定了 说明在Main页面
                    Message message = Message.obtain();
                    message.what = VideoConversationCons.CALLED;
                    try {
                        mClientMessenger.send(message);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                } else {
//                    如果没有绑定 则说明应用退出了

                }
            }

            @Override
            public void onTokenError(WilddogVideoError wilddogVideoError) {
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


}
