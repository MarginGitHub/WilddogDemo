package com.zd.wilddogdemo.ui;


import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.wilddog.video.WilddogVideoView;
import com.zd.wilddogdemo.R;
import com.zd.wilddogdemo.beans.User;
import com.zd.wilddogdemo.cons.ConversationCons;
import com.zd.wilddogdemo.net.NetServiceConfig;
import com.zd.wilddogdemo.service.DialService;
import com.zd.wilddogdemo.service.VideoReceiverService;
import com.zd.wilddogdemo.utils.GlideApp;


import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.R.attr.handle;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class VideoReceiverActivity extends AppCompatActivity implements ServiceConnection {
    @BindView(R.id.remote_view)
    WilddogVideoView mRemoteView;
    @BindView(R.id.local_view)
    WilddogVideoView mLocalView;
    @BindView(R.id.video_layout)
    FrameLayout mVideoLayout;
    @BindView(R.id.user_head_iv)
    ImageView mUserHeadIv;
    @BindView(R.id.user_nick_name)
    TextView mUserNickName;
    @BindView(R.id.accept_layout)
    FrameLayout mAcceptLayout;

    private boolean onCalled = true;
    private boolean onCall;
    private Messenger mServerMessenger;
    private Messenger mClientMessenger = new Messenger(new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case ConversationCons.STREAM_RECEIVED:
                    onCall = true;
                    VideoReceiverService.VideoStream videoStream = (VideoReceiverService.VideoStream) message.obj;
                    setVideoViews();
                    videoStream.mLocalStream.attach(mLocalView);
                    videoStream.mRemoteStream.attach(mRemoteView);
                    break;
                case ConversationCons.HANG_UP:
                    closeConversation();
                    finish();
                default:
                    break;
            }
            return true;
        }
    }));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_receiver);
        ButterKnife.bind(this);
        bindVideoService();
        initViews();
    }

    @Override
    protected void onDestroy() {
        exist();
        super.onDestroy();
        if (onCall) {
            closeConversation();
        } else if (onCalled) {
            sendMessage(ConversationCons.REJECT, null, null);
        }
        unbindVideoService();
    }

    private void exist() {
        String className = VideoReceiverActivity.class.getName();
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            List<ActivityManager.AppTask> appTasks = am.getAppTasks();
            for (ActivityManager.AppTask task: appTasks) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    String base = task.getTaskInfo().baseActivity.getClassName();
                    String top = task.getTaskInfo().topActivity.getClassName();
                    if (top.equals(className) &&
                            base.equals(className)) {
                        Intent intent = new Intent(this, DoctorActivity.class);
                        intent.putExtra("user", getIntent().getSerializableExtra("doctor"));
                        startActivity(intent);
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> runningTasks = am.getRunningTasks(100);
            for (ActivityManager.RunningTaskInfo info :
                   runningTasks ) {
                String top = info.topActivity.getClassName();
                String base = info.baseActivity.getClassName();
                if (top.equals(className) &&
                        base.equals(className)) {
                    Intent intent = new Intent(this, DoctorActivity.class);
                    intent.putExtra("user", getIntent().getSerializableExtra("doctor"));
                    startActivity(intent);
                }
            }
        }
    }


    private void initViews() {
        User user = (User) getIntent().getSerializableExtra("user");
        if (user != null) {
            if (!TextUtils.isEmpty(user.getHead_img_url())) {
                GlideApp.with(this)
                        .load(user.getHead_img_url())
                        .placeholder(R.drawable.head)
                        .circleCrop()
                        .into(mUserHeadIv);
            }
            mUserNickName.setText(user.getNick_name());
        }
    }

    private void bindVideoService() {
        Intent intent = new Intent(this, VideoReceiverService.class);
        bindService(intent, this, Service.BIND_AUTO_CREATE);
    }

    private void unbindVideoService() {
        unbindService(this);
    }

    private void setVideoViews() {
        mAcceptLayout.setVisibility(View.GONE);
        mVideoLayout.setVisibility(View.VISIBLE);
        mRemoteView.setZOrderMediaOverlay(true);
        mLocalView.setZOrderOnTop(true);
        mLocalView.setMirror(true);
    }

    private void closeConversation() {
        sendMessage(ConversationCons.HANG_UP, null, null);
        onCall = false;
    }


    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        mServerMessenger = new Messenger(iBinder);
        sendMessage(ConversationCons.CONNECTED, null, mClientMessenger);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        mServerMessenger = null;
    }


    @OnClick({R.id.hung_up, R.id.accept_iv, R.id.reject_iv})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.hung_up:
                closeConversation();
                finish();
                break;
            case R.id.accept_iv:
                onCalled = false;
                sendMessage(ConversationCons.ACCEPT, null, null);
                break;
            case R.id.reject_iv:
                onCalled = false;
                sendMessage(ConversationCons.REJECT, null, null);
                finish();
                break;
        }
    }

    private void sendMessage(int what, Object obj, Messenger messenger) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.obj = obj;
        msg.replyTo = messenger;
        try {
            mServerMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
