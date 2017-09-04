package com.zd.wilddogdemo;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.wilddog.video.WilddogVideoView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by dongjijin on 2017/9/1 0001.
 */

public class VideoConversationActivity extends AppCompatActivity implements ServiceConnection {

    private static final int RESULT_CODE = 0xee;
    @BindView(R.id.remote_view)
    WilddogVideoView mRemoteView;
    @BindView(R.id.local_view)
    WilddogVideoView mLocalView;

    private boolean isBinded;
    private Messenger mMessenger;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case VideoConversationCons.ACCEPTED:
                    break;
                case VideoConversationCons.REJECTED:
                    break;
                case VideoConversationCons.BUSY:
                    break;
                case VideoConversationCons.TIMEOUT:
                    break;
                case VideoConversationCons.CLOSED:
                    break;
            }
            return true;
        }
    });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_conversation);
        ButterKnife.bind(this);
        bindVideoService();
    }

    private void bindVideoService() {
        if (!isBinded) {
            Intent intent = new Intent(this, WilddogVideoService.class);
            bindService(intent, this, Service.BIND_AUTO_CREATE);
            isBinded = true;
        }
    }

    private void unbindVideoService() {
        if (isBinded) {
            unbindService(this);
            isBinded = false;
        }
    }

    @OnClick(R.id.hung_up)
    public void onViewClicked() {
        unbindVideoService();
        setResult(RESULT_CODE);
        finish();
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        mMessenger = new Messenger(iBinder);
        Message message = Message.obtain();
        message.what = VideoConversationCons.CONNECTED;
        message.replyTo = new Messenger(mHandler);
        try {
            mMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        boolean call = getIntent().getBooleanExtra("call", false);
//        如果是主动发起请求的
        if (call) {
            Message msg = Message.obtain();
            msg.what = VideoConversationCons.CALL;
            try {
                mMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {

        }

    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        mMessenger = null;
    }
}
