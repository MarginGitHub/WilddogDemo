package com.zd.wilddogdemo;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.wilddog.video.CallStatus;
import com.wilddog.video.Conversation;
import com.wilddog.video.LocalStream;
import com.wilddog.video.LocalStreamOptions;
import com.wilddog.video.RemoteStream;
import com.wilddog.video.WilddogVideo;
import com.wilddog.video.WilddogVideoError;
import com.wilddog.video.WilddogVideoView;
import com.wilddog.video.core.stats.LocalStreamStatsReport;
import com.wilddog.video.core.stats.RemoteStreamStatsReport;
import com.wilddog.wilddogauth.WilddogAuth;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Demo";
    @BindView(R.id.remote_view)
    WilddogVideoView mRemoteView;
    @BindView(R.id.local_view)
    WilddogVideoView mLocalView;

    private WilddogVideo mVideo;
    private Conversation mConversation;
    private LocalStream mLocalStream;
    private boolean mIsInConversation;
    private RemoteStream mRemoteStream;
    private Conversation.Listener mConversationListener = new Conversation.Listener() {
        @Override
        public void onCallResponse(CallStatus callStatus) {
            switch (callStatus) {
                case ACCEPTED:
                    mIsInConversation = true;
                    break;
                case REJECTED:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mIsInConversation = false;
                            Toast.makeText(MainActivity.this, "对方拒绝了你的视频通话请求", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                    break;
                case BUSY:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mIsInConversation = false;
                            Toast.makeText(MainActivity.this, "对方正在通话中", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                    break;
                case TIMEOUT:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mIsInConversation = false;
                            Toast.makeText(MainActivity.this, "呼叫超时，请稍后再呼叫", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onStreamReceived(RemoteStream remoteStream) {
            mRemoteStream = remoteStream;
            mRemoteStream.enableAudio(true);
            remoteStream.attach(mRemoteView);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "视频通话开始", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onClosed() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mIsInConversation = false;
                    Toast.makeText(MainActivity.this, "视频通话已结束", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }

        @Override
        public void onError(WilddogVideoError wilddogVideoError) {

        }
    };

    private Conversation.StatsListener mStatsListener = new Conversation.StatsListener() {
        @Override
        public void onLocalStreamStatsReport(LocalStreamStatsReport localStreamStatsReport) {

        }

        @Override
        public void onRemoteStreamStatsReport(RemoteStreamStatsReport remoteStreamStatsReport) {

        }
    };
    private WilddogVideo.Listener mVideoListener = new WilddogVideo.Listener() {
        @Override
        public void onCalled(final Conversation conversation, String s) {
           doCalled(conversation);
        }

        @Override
        public void onTokenError(WilddogVideoError wilddogVideoError) {
            Log.e(TAG, "onTokenError: code=" + wilddogVideoError.getErrCode() + ", message=" + wilddogVideoError.getMessage());
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initVideoViews();
        init();

        String remote_id = getIntent().getStringExtra("remote_id");
        Log.d(TAG, "remote_id: " + (TextUtils.isEmpty(remote_id)? "null" : remote_id));
        if (!TextUtils.isEmpty(remote_id)) {
            Toast.makeText(this, remote_id, Toast.LENGTH_SHORT).show();
            inviteToConversation(remote_id);
        }
    }

    private void initVideoViews() {
        mRemoteView.setZOrderMediaOverlay(true);
        mLocalView.setZOrderOnTop(true);
        mLocalView.setMirror(true);
    }

    private void init() {
        mVideo = WilddogVideo.getInstance();
        setupLocalStream();
        setupVideoListener();
    }

    private void setupLocalStream() {
        LocalStreamOptions options = new LocalStreamOptions.Builder()
                .dimension(LocalStreamOptions.Dimension.DIMENSION_480P)
                .build();
        mLocalStream = mVideo.createLocalStream(options);
        mLocalStream.enableAudio(true);
        mLocalStream.attach(mLocalView);
    }


    private void setupVideoListener() {
        Toast.makeText(this, "开始初始化Video监听器", Toast.LENGTH_SHORT).show();
        Log.d("onCalled", "开始初始化Video监听器");
        if (mVideoListener == null) {
            Log.d("onCalled", "mVideoListener = null");
            return;
        }
        mVideo.setListener(mVideoListener);
    }


    private void inviteToConversation(String remoteUid) {
        //创建连接参数对象
        mConversation = mVideo.call(remoteUid, mLocalStream, "");
        mConversation.setConversationListener(mConversationListener);
        mConversation.setStatsListener(mStatsListener);

    }

    @Override
    protected void onDestroy() {
        closeConversation();
        super.onDestroy();
    }


    @OnClick({ R.id.hung_up})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.hung_up:
                finish();
                break;
        }
    }

    private void closeConversation() {
        mVideo.setListener(null);
//        mLocalStream.detach();
        mLocalStream = null;
        if (mIsInConversation && mConversation != null) {
            mConversation.setStatsListener(null);
            mConversation.setConversationListener(null);
            mConversation.close();
            mConversation = null;
//            mRemoteStream.detach();
            mRemoteStream = null;
        }

    }

    private void doCalled(final Conversation conversation) {
        String remoteUid = conversation.getRemoteUid();
        Log.d("onCalled", "onCalled: " + remoteUid);
        Log.d("onCalled", "Thread: " + Thread.currentThread().getName());
        Toast.makeText(MainActivity.this, remoteUid + "发起了一次视频通话请求", Toast.LENGTH_SHORT).show();
        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                .setMessage(String.format("用户%s邀请你进行视频通话", remoteUid))
                .setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        conversation.reject();
                        finish();
                    }
                })
                .setPositiveButton("接受", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mConversation = conversation;
                        conversation.accept(mLocalStream);
                        mConversation.setConversationListener(mConversationListener);
                        mConversation.setStatsListener(mStatsListener);
                        mIsInConversation = true;
                    }
                })
                .create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
}
