package com.zd.wilddogdemo;

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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wilddog.video.WilddogVideoView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DoctorActivity extends AppCompatActivity implements ServiceConnection {
    @BindView(R.id.head_iv)
    ImageView mHeadIv;
    @BindView(R.id.doctor_info)
    TextView mDoctorInfo;
    @BindView(R.id.doctor_container)
    LinearLayout mDoctorContainer;
    @BindView(R.id.remote_view)
    WilddogVideoView mRemoteView;
    @BindView(R.id.local_view)
    WilddogVideoView mLocalView;
    @BindView(R.id.video_layout)
    FrameLayout mVideoLayout;
    private Messenger mServerMessenger;
    private boolean isBinded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor);
        ButterKnife.bind(this);

        bindVideoService();

    }

    @Override
    protected void onDestroy() {
//        WilddogSync.getInstance().getReference("userlist")
//                .child(WilddogAuth.getInstance().getCurrentUser().getUid()).removeValue();
//        WilddogVideo.getInstance().stop();
//        WilddogAuth.getInstance().signOut();
        if (onCall) {
            closeConversation();
        }
        unbindVideoService();
        super.onDestroy();
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

    private boolean wakeupFromService() {
        return getIntent().getBooleanExtra("wake_up", false);
    }


    private void initVideoViews() {
        mRemoteView.setVisibility(View.VISIBLE);
        mRemoteView.setZOrderMediaOverlay(true);
        mLocalView.setVisibility(View.VISIBLE);
        mLocalView.setZOrderOnTop(true);
        mLocalView.setMirror(true);

//       接收到对方音视频流以后先把用户列表list给GONE掉，然后让VideoLayout显示出来
        mDoctorContainer.setVisibility(View.GONE);
        mVideoLayout.setVisibility(View.VISIBLE);

    }



    private void closeConversation() {
//            设置View属性
        mLocalView.setMirror(false);
        mLocalView.setZOrderOnTop(false);
        mLocalView.setVisibility(View.GONE);
        mRemoteView.setZOrderMediaOverlay(false);
        mRemoteView.setVisibility(View.GONE);
        mVideoLayout.setVisibility(View.GONE);
        mDoctorContainer.setVisibility(View.VISIBLE);
        onCall = false;
    }

    @OnClick(R.id.hung_up)
    public void onViewClicked() {
        Message msg = Message.obtain();
        msg.what = ConversationCons.HANG_UP;
        try {
            mServerMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        closeConversation();
    }


    /*****************************************************************************************************/
    //ServiceConnection
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder binder) {
        mServerMessenger = new Messenger(binder);
        Message msg = Message.obtain();
        msg.what = ConversationCons.CONNECTED;
        msg.replyTo = mClientMessenger;
        try {
            mServerMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (wakeupFromService()) {
            String remote_uid = getIntent().getStringExtra("remote_uid");
            if (!TextUtils.isEmpty(remote_uid)) {
                showDialog(remote_uid);
            }
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        mServerMessenger = null;
    }

    private boolean onCall;
    private Messenger mClientMessenger = new Messenger(new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case ConversationCons.RING_UP:
                    String remoteUid = (String) message.obj;
                    showDialog(remoteUid);
                    break;
                case ConversationCons.ACCEPTED:
                    Toast.makeText(DoctorActivity.this, "ConversationCons.ACCEPTED", Toast.LENGTH_SHORT).show();
                    break;
                case ConversationCons.STREAM_RECEIVED:
                    onCall = true;
                    WilddogVideoService.VideoStream videoStream = (WilddogVideoService.VideoStream) message.obj;
                    initVideoViews();
                    videoStream.mLocalStream.attach(mLocalView);
                    videoStream.mRemoteStream.attach(mRemoteView);
                    break;
                case ConversationCons.REJECTED:
                    Toast.makeText(DoctorActivity.this, "ConversationCons.REJECTED", Toast.LENGTH_SHORT).show();
                    break;
                case ConversationCons.BUSY:
                    Toast.makeText(DoctorActivity.this, "ConversationCons.BUSY", Toast.LENGTH_SHORT).show();
                    break;
                case ConversationCons.TIMEOUT:
                    Toast.makeText(DoctorActivity.this, "ConversationCons.TIMEOUT", Toast.LENGTH_SHORT).show();
                    break;
                case ConversationCons.HANG_UP:
                    closeConversation();
                default:
                    break;
            }
            return true;
        }
    }));

    private void showDialog(String remoteUid) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage(String.format("%s邀请你进行视频通话", remoteUid))
                .setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Message msg = Message.obtain();
                        msg.what = ConversationCons.REJECTED;
                        try {
                            mServerMessenger.send(msg);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setPositiveButton("接受", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Message msg = Message.obtain();
                        msg.what = ConversationCons.ACCEPTED;
                        try {
                            mServerMessenger.send(msg);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
}
