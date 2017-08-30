package com.zd.wilddogdemo;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.wilddog.video.Conversation;
import com.wilddog.video.WilddogVideo;
import com.wilddog.video.WilddogVideoError;
import com.wilddog.wilddogauth.WilddogAuth;

/**
 * Created by dongjijin on 2017/8/30 0030.
 */

public class ConversionService extends Service {

    private WilddogVideo mWilddogVideo;
    private WilddogVideo.Listener mListener;

    @Override
    public void onCreate() {
        super.onCreate();
        initWilddogVideo();
        mWilddogVideo.setListener(new WilddogVideo.Listener() {
            @Override
            public void onCalled(Conversation conversation, String s) {
                if (mListener != null) {
                    mListener.onCalled(conversation, s);
                } else {
                    Intent intent = new Intent(getApplicationContext(), UserListActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mListener = null;
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

    class ConversionBinder extends Binder {

        public void setVideoListener(WilddogVideo.Listener listener) {
            mListener = listener;
        }
    }

//

    private void initWilddogVideo() {
        String token = WilddogAuth.getInstance().getCurrentUser().getToken(false).getResult().getToken();
        WilddogVideo.initialize(getApplicationContext(), getResources().getString(R.string.video_app_id), token);
        mWilddogVideo = WilddogVideo.getInstance();
    }
}
