package com.zd.wilddogdemo.service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.wilddog.video.CallStatus;
import com.wilddog.video.Conversation;
import com.wilddog.video.LocalStream;
import com.wilddog.video.LocalStreamOptions;
import com.wilddog.video.RemoteStream;
import com.wilddog.video.WilddogVideo;
import com.wilddog.video.WilddogVideoError;
import com.zd.wilddogdemo.R;
import com.zd.wilddogdemo.beans.User;
import com.zd.wilddogdemo.broadreceiver.KeepAliveBroadcastReceiver;
import com.zd.wilddogdemo.cons.ConversationCons;
import com.zd.wilddogdemo.ui.DoctorActivity;
import com.zd.wilddogdemo.ui.UserActivity;


/**
 * Created by dongjijin on 2017/8/30 0030.
 */

public class WilddogVideoService extends Service implements Conversation.Listener {

    private static final String TAG = "WilddogVideoService";
    private Conversation mVideoConversation;
    private KeepAliveBroadcastReceiver mReceiver;
    private WilddogVideo mWilddogVideo;
    private LocalStream mLocalStream;
    private RemoteStream mRemoteStream;

    private Messenger mServerMessenger = new Messenger(new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
//                服务绑定连接上
                case ConversationCons.CONNECTED:
                    mClientMessenger = message.replyTo;
                    break;
//                呼叫
                case ConversationCons.RING_UP:
                    callPeer((String)message.obj);
                    break;
//                挂断
                case ConversationCons.HANG_UP:
                    closeConversation();
                    break;
//                接受
                case ConversationCons.ACCEPTED:
                    if (mLocalStream == null) {
                        createLocalStream();
                    }
                    mVideoConversation.accept(mLocalStream);
                    break;
//                拒绝
                case ConversationCons.REJECTED:
                    mVideoConversation.reject();
                    break;
                default:
                    break;
            }
            return true;
        }
    }));
    private Messenger mClientMessenger;
    private boolean isBinded = false;
    private boolean onCall = false;
    private User mUser;


    @Override
    public void onCreate() {
        super.onCreate();
        startKeepAliveBroadcastReceiver();
    }

    @Override
    public void onDestroy() {
        stopKeepAliveBroadcastReceiver();
        mWilddogVideo.stop();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            mUser = (User) intent.getSerializableExtra("user");
            initWilddogVideo(mUser.getToken());
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
        return mServerMessenger.getBinder();
    }


    private void initWilddogVideo(String token) {
        String videoAppID = getResources().getString(R.string.video_app_id);
        WilddogVideo.initialize(getApplicationContext(), videoAppID, token);
        mWilddogVideo = WilddogVideo.getInstance();
        mWilddogVideo.setListener(new WilddogVideo.Listener() {
            @Override
            public void onCalled(Conversation conversation, String s) {

                mVideoConversation = conversation;
                mVideoConversation.setConversationListener(WilddogVideoService.this);
                if (isBinded) {
                    Message msg = Message.obtain();
                    msg.what = ConversationCons.RING_UP;
                    msg.obj = s;
                    try {
                        mClientMessenger.send(msg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                } else {
                    Intent intent;
                    if (mUser.isDoctor()) {
                        intent = new Intent(getApplicationContext(), DoctorActivity.class);
                    } else {
                        intent = new Intent(getApplicationContext(), UserActivity.class);
                    }
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("user", mUser);
                    startActivity(intent);
                }
            }

            @Override
            public void onTokenError(WilddogVideoError wilddogVideoError) {
                String message = wilddogVideoError.getMessage();
                Log.d(TAG, "onTokenError: " + message);
            }
        });
        mWilddogVideo.start();
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

    private void createLocalStream() {
        LocalStreamOptions options = new LocalStreamOptions.Builder()
                .dimension(LocalStreamOptions.Dimension.DIMENSION_480P)
                .build();
        mLocalStream = mWilddogVideo.createLocalStream(options);
    }

    private void callPeer(String remoteUid) {
        createLocalStream();
        mVideoConversation = mWilddogVideo.call(remoteUid, mLocalStream, mUser.getUid());
        mVideoConversation.setConversationListener(this);
    }

    private void closeConversation() {
        if (onCall) {
            mVideoConversation.close();
            mLocalStream.detach();
            mRemoteStream.detach();
            mVideoConversation = null;
            mLocalStream = null;
            mRemoteStream = null;
            onCall = false;
        }
    }


    @Override
    public void onCallResponse(CallStatus callStatus) {
        Message msg = Message.obtain();
        switch (callStatus) {
            case ACCEPTED:
                msg.what = ConversationCons.ACCEPTED;
                break;
            case REJECTED:
                msg.what = ConversationCons.REJECTED;
                break;
            case BUSY:
                msg.what = ConversationCons.BUSY;
                break;
            case TIMEOUT:
                msg.what = ConversationCons.TIMEOUT;
                break;
            default:
                break;
        }
        try {
            mClientMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStreamReceived(RemoteStream remoteStream) {
        mRemoteStream = remoteStream;
        Message msg = Message.obtain();
        msg.what = ConversationCons.STREAM_RECEIVED;
        msg.obj = new VideoStream(mLocalStream, mRemoteStream);
        try {
            mClientMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        onCall = true;
    }

    @Override
    public void onClosed() {
        closeConversation();
        Message msg = Message.obtain();
        msg.what = ConversationCons.HANG_UP;
        try {
            mClientMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(WilddogVideoError wilddogVideoError) {
        String message = wilddogVideoError.getMessage();
        Log.d(TAG, "onError: " + message);
    }

    public class VideoStream {
        public LocalStream mLocalStream;
        public RemoteStream mRemoteStream;

        public VideoStream(LocalStream localStream, RemoteStream remoteStream) {
            mLocalStream = localStream;
            mRemoteStream = remoteStream;
        }
    }

}
