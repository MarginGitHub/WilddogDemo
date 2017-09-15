package com.zd.wilddogdemo.ui.user;

import android.app.Service;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.wilddog.video.WilddogVideoView;
import com.zd.wilddogdemo.R;
import com.zd.wilddogdemo.beans.DialInfo;
import com.zd.wilddogdemo.cons.ConversationCons;
import com.zd.wilddogdemo.service.DialService;
import com.zd.wilddogdemo.ui.BaseActivity;
import com.zd.wilddogdemo.utils.GlideApp;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class DialActivity extends BaseActivity implements ServiceConnection {
    @BindView(R.id.remote_view)
    WilddogVideoView mRemoteView;
    @BindView(R.id.local_view)
    WilddogVideoView mLocalView;
    @BindView(R.id.video_layout)
    FrameLayout mVideoLayout;
    @BindView(R.id.doctor_head_iv)
    ImageView mDoctorHeadIv;
    @BindView(R.id.doctor_nick_name)
    TextView mDoctorNickName;
    @BindView(R.id.dial_layout)
    FrameLayout mDialLayout;

    private boolean onCall;
    private Messenger mServerMessenger;
    private Messenger mClientMessenger = new Messenger(new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case ConversationCons.ACCEPTED:
                    Toast.makeText(DialActivity.this, "ConversationCons.ACCEPTED", Toast.LENGTH_SHORT).show();
                    break;
                case ConversationCons.STREAM_RECEIVED:
                    onCall = true;
                    DialService.VideoStream videoStream = (DialService.VideoStream) message.obj;
                    setVideoViews();
                    videoStream.mLocalStream.attach(mLocalView);
                    videoStream.mRemoteStream.attach(mRemoteView);
                    break;
                case ConversationCons.REJECTED:
                    Toast.makeText(DialActivity.this, "ConversationCons.REJECTED", Toast.LENGTH_SHORT).show();
                    setResult(0);
                    finish();
                    break;
                case ConversationCons.BUSY:
                    Toast.makeText(DialActivity.this, "ConversationCons.BUSY", Toast.LENGTH_SHORT).show();
                    setResult(0);
                    finish();
                    break;
                case ConversationCons.TIMEOUT:
                    Toast.makeText(DialActivity.this, "ConversationCons.TIMEOUT", Toast.LENGTH_SHORT).show();
                    setResult(0);
                    finish();
                    break;
                case ConversationCons.HANG_UP:
                    resetVideoViews();
                    onCall = false;
                    if (message.arg1 == ConversationCons.BALANCE_NOT_ENOUGH) {
                        new AlertDialog.Builder(DialActivity.this)
                                .setMessage("账户余额不足，请充值")
                                .setPositiveButton("知道了", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        setResult(RESULT_OK);
                                        finish();
                                    }
                                })
                                .show();
                    } else {
                        setResult(RESULT_OK);
                        finish();
                    }
                default:
                    break;
            }
            return true;
        }
    }));
    private DialInfo mDialInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dial);
        ButterKnife.bind(this);
        mDialInfo = (DialInfo)getIntent().getSerializableExtra("dial_info");
        bindVideoService();
        initViews();
    }


    @Override
    protected void onDestroy() {
        if (onCall) {
            closeConversation();
        }
        unbindVideoService();
        super.onDestroy();
    }

    private void bindVideoService() {
        Intent intent = new Intent(this, DialService.class);
        bindService(intent, this, Service.BIND_AUTO_CREATE);
    }

    private void unbindVideoService() {
        unbindService(this);
    }

    protected void initViews() {
        String avatarUrl = mDialInfo.getDoctor().getAd_url();
        if (!TextUtils.isEmpty(avatarUrl)) {
            GlideApp.with(this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.head)
                    .circleCrop()
                    .into(mDoctorHeadIv);
        }
        mDoctorNickName.setText(mDialInfo.getDoctor().getNick_name());
    }

    private void setVideoViews() {
        mDialLayout.setVisibility(View.GONE);
        mVideoLayout.setVisibility(View.VISIBLE);
        mRemoteView.setVisibility(View.VISIBLE);
        mRemoteView.setZOrderMediaOverlay(true);
        mLocalView.setVisibility(View.VISIBLE);
        mLocalView.setZOrderOnTop(true);
        mLocalView.setMirror(true);
    }

    private void resetVideoViews() {
        mDialLayout.setVisibility(View.VISIBLE);
        mVideoLayout.setVisibility(View.GONE);
        mRemoteView.setVisibility(View.GONE);
        mRemoteView.setZOrderMediaOverlay(false);
        mLocalView.setVisibility(View.GONE);
        mLocalView.setZOrderOnTop(false);
        mLocalView.setMirror(false);
    }


    private void closeConversation() {
        sendMessage(ConversationCons.HANG_UP, null, null);
        onCall = false;
    }



    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        mServerMessenger = new Messenger(iBinder);
        sendMessage(ConversationCons.CONNECTED, null, mClientMessenger);
        sendMessage(ConversationCons.RING_UP, mDialInfo, null);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        mServerMessenger = null;
    }


    @OnClick({R.id.hung_up, R.id.close})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.hung_up:
                resetVideoViews();
                closeConversation();
                setResult(RESULT_OK);
                finish();
                break;
            case R.id.close:
                sendMessage(ConversationCons.CLOSE, null, null);
                setResult(0);
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
