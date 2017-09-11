package com.zd.wilddogdemo.ui;

import android.app.Dialog;
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
import com.zd.wilddogdemo.beans.Doctor;
import com.zd.wilddogdemo.beans.User;
import com.zd.wilddogdemo.cons.ConversationCons;
import com.zd.wilddogdemo.service.DialService;
import com.zd.wilddogdemo.storage.ObjectPreference;
import com.zd.wilddogdemo.utils.GlideApp;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class DialActivity extends AppCompatActivity implements ServiceConnection {
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
                    break;
                case ConversationCons.BUSY:
                    Toast.makeText(DialActivity.this, "ConversationCons.BUSY", Toast.LENGTH_SHORT).show();
                    break;
                case ConversationCons.TIMEOUT:
                    Toast.makeText(DialActivity.this, "ConversationCons.TIMEOUT", Toast.LENGTH_SHORT).show();
                    break;
                case ConversationCons.HANG_UP:
                    resetVideoViews();
                    onCall = false;
                    if (message.arg1 == ConversationCons.BALANCE_NOT_ENOUGH) {
                        new AlertDialog.Builder(DialActivity.this)
                                .setMessage("账户余额不足，请充值")
                                .setPositiveButton("知道了", null)
                                .show();
                    }
                default:
                    break;
            }
            return true;
        }
    }));
    private User mUser;
    private Doctor mDoctor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dial);
        ButterKnife.bind(this);
        bindVideoService();
        mUser = (User) getIntent().getSerializableExtra("user");
        mDoctor = (Doctor) getIntent().getSerializableExtra("doctor");
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

    private void initViews() {
        if (!TextUtils.isEmpty(mDoctor.getAd_url())) {
            GlideApp.with(this)
                    .load(mDoctor.getAd_url())
                    .placeholder(R.drawable.head)
                    .circleCrop()
                    .into(mDoctorHeadIv);
        }
        mDoctorNickName.setText(mDoctor.getNick_name());
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

    private void dial(final DialInfo info) {
        User user = ObjectPreference.getObject(this, User.class);
//        int amount = user.getAmount().intValue();
        int amount = 1;
        if (amount <= 0) {
            Toast.makeText(this, "您账号所剩余额不足1元，请先充值再进行拨号", Toast.LENGTH_LONG).show();
            return;
        }
        info.setMaxConversationTime(amount);
        Dialog dialog = new AlertDialog.Builder(this)
                .setMessage(String.format("您当前账户所剩余额%d元,预期可以进行%d分钟视频通话,是否确定进行拨号？", amount, amount))
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sendMessage(ConversationCons.RING_UP, info, null);
                    }
                })
                .create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }



    public class DialInfo {
        String mDoctorUid;
        User mUser;
        int maxConversationTime;

        public DialInfo(String doctorUid, User user) {
            mDoctorUid = doctorUid;
            mUser = user;
        }

        public String getDoctorUid() {
            return mDoctorUid;
        }

        public void setDoctorUid(String doctorUid) {
            mDoctorUid = doctorUid;
        }

        public User getUser() {
            return mUser;
        }

        public void setUser(User user) {
            mUser = user;
        }

        public int getMaxConversationTime() {
            return maxConversationTime;
        }

        public void setMaxConversationTime(int maxConversationTime) {
            this.maxConversationTime = maxConversationTime;
        }
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


    @OnClick({R.id.hung_up, R.id.ring_up, R.id.close})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.hung_up:
                resetVideoViews();
                closeConversation();
                break;
            case R.id.ring_up:
                dial(new DialInfo(mDoctor.getUser_id(), mUser));
                break;
            case R.id.close:
                sendMessage(ConversationCons.CLOSE, null, null);
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
