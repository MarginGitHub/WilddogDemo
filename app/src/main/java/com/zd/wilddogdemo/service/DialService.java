package com.zd.wilddogdemo.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.wilddog.video.CallStatus;
import com.wilddog.video.Conversation;
import com.wilddog.video.LocalStream;
import com.wilddog.video.LocalStreamOptions;
import com.wilddog.video.RemoteStream;
import com.wilddog.video.WilddogVideo;
import com.wilddog.video.WilddogVideoError;
import com.zd.wilddogdemo.R;
import com.zd.wilddogdemo.beans.Result;
import com.zd.wilddogdemo.beans.User;
import com.zd.wilddogdemo.broadreceiver.KeepAliveBroadcastReceiver;
import com.zd.wilddogdemo.cons.ConversationCons;
import com.zd.wilddogdemo.net.Net;
import com.zd.wilddogdemo.storage.ObjectPreference;
import com.zd.wilddogdemo.ui.DialActivity;

import java.util.HashMap;

import io.reactivex.annotations.NonNull;

import static android.R.attr.data;


/**
 * Created by dongjijin on 2017/8/30 0030.
 */

public class DialService extends Service implements Conversation.Listener {

    private static final String TAG = "DialService";
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
                    callPeer((DialActivity.DialInfo) message.obj);
                    break;
//                挂断
                case ConversationCons.HANG_UP:
                    closeConversation();
                    uploadVideoConversationRecord();
                    break;
                case ConversationCons.CLOSE:
                    if (mVideoConversation != null) {
                        mVideoConversation.close();
                        mVideoConversation = null;
                    }
                    break;
                default:
                    break;
            }
            return true;
        }
    }));
    private Messenger mClientMessenger;
    private boolean onCall = false;
    private User mUser;
    private long mStartTime;
    private String mDoctorUid;


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        mWilddogVideo.stop();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            mUser = (User) intent.getSerializableExtra("user");
            initWilddogVideo(mUser.getWilddogVideoToken());
        }
        return START_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mServerMessenger.getBinder();
    }


    private void initWilddogVideo(String token) {
        String videoAppID = getResources().getString(R.string.video_app_id);
        WilddogVideo.initialize(getApplicationContext(), videoAppID, token);
        mWilddogVideo = WilddogVideo.getInstance();
        mWilddogVideo.start();
    }


    private void createLocalStream() {
        LocalStreamOptions options = new LocalStreamOptions.Builder()
                .dimension(LocalStreamOptions.Dimension.DIMENSION_480P)
                .build();
        mLocalStream = mWilddogVideo.createLocalStream(options);
    }

    private void callPeer(DialActivity.DialInfo info) {
        createLocalStream();
        mDoctorUid = info.getDoctorUid();
        HashMap<String, String> data = new HashMap<>();
        data.put("nickname", info.getUser().getNick_name());
        String imgUrl = info.getUser().getHead_img_url();
        data.put("faceurl", "http://www.feizl.com/upload2007/2014_06/140625163961254.jpg");
        String user = new Gson().toJson(data);
        mVideoConversation = mWilddogVideo.call(info.getDoctorUid(), mLocalStream, user);
        mVideoConversation.setConversationListener(this);
    }

    private void closeConversation() {
        if (onCall) {
            mVideoConversation.close();
//            mLocalStream.detach();
//            mRemoteStream.detach();
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
        mStartTime = System.currentTimeMillis() / 1000;
        onCall = true;
    }

    @Override
    public void onClosed() {
        closeConversation();
        Message msg = Message.obtain();
        msg.what = ConversationCons.HANG_UP;
        try {
            mClientMessenger.send(msg);
            uploadVideoConversationRecord();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(WilddogVideoError wilddogVideoError) {
        String message = wilddogVideoError.getMessage();
        Log.d(TAG, "onError: " + message);
    }

    private void uploadVideoConversationRecord() {
        long currentTime = System.currentTimeMillis() / 1000;
        Net.instance().uploadVideoConversationRecord(
                mUser.getToken(), mUser.getUser_id(), mDoctorUid, mStartTime, currentTime - mStartTime,
                new Net.OnNext<Result<User>>() {
                    @Override
                    public void onNext(@NonNull Result<User> result) {
                        if (result.getCode() == 100) {
                            ObjectPreference.saveObject(getApplicationContext(), result.getData());
                        }
                    }
                },
                new Net.OnError() {
                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.d(TAG, "onError: " + e.toString());
                    }
                });
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