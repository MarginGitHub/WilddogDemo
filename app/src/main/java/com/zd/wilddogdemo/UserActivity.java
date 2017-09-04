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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.wilddog.client.ChildEventListener;
import com.wilddog.client.DataSnapshot;
import com.wilddog.client.SyncError;
import com.wilddog.client.SyncReference;
import com.wilddog.client.WilddogSync;
import com.wilddog.video.WilddogVideoView;
import com.wilddog.wilddogauth.WilddogAuth;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UserActivity extends AppCompatActivity implements ChildEventListener, ServiceConnection {

    @BindView(R.id.container_rl)
    RecyclerView mContainerRl;
    @BindView(R.id.remote_view)
    WilddogVideoView mRemoteView;
    @BindView(R.id.local_view)
    WilddogVideoView mLocalView;
    @BindView(R.id.video_layout)
    FrameLayout mVideoLayout;

    private UserListAdapter mAdapter;
    private List<String> mUserListData;
    private Messenger mServerMessenger;
    private String mLocalUid;
    private boolean isBinded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        ButterKnife.bind(this);

        setupContainer();
        setupUserListDataListener();
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

    private boolean isDoctor() {
        return getIntent().getBooleanExtra("is_doctor", false);
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


    /******************************************************************************************************/
    private void setupContainer() {
        mUserListData = new ArrayList();
        mAdapter = new UserListAdapter(this);
        mContainerRl.setAdapter(mAdapter);
        mAdapter.setData(mUserListData);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mContainerRl.setLayoutManager(linearLayoutManager);
    }

    private void setupUserListDataListener() {
        mLocalUid = WilddogAuth.getInstance().getCurrentUser().getUid();
//        SyncReference syncReference = WilddogSync.getInstance()
//                .getReference(getResources().getString(R.string.video_conversation_room));
        SyncReference syncReference = WilddogSync.getInstance()
                .getReference("online");
        syncReference.addChildEventListener(this);
    }


    private void initVideoViews() {
        mRemoteView.setVisibility(View.VISIBLE);
        mRemoteView.setZOrderMediaOverlay(true);
        mLocalView.setVisibility(View.VISIBLE);
        mLocalView.setZOrderOnTop(true);
        mLocalView.setMirror(true);

//       接收到对方音视频流以后先把用户列表list给GONE掉，然后让VideoLayout显示出来
        mContainerRl.setVisibility(View.GONE);
        mVideoLayout.setVisibility(View.VISIBLE);

    }

    public void callPeer(String remoteUid) {
        Message msg = Message.obtain();
        msg.what = ConversationCons.RING_UP;
        msg.obj = remoteUid;
        try {
            mServerMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    private void closeConversation() {
//            设置View属性
        mLocalView.setMirror(false);
        mLocalView.setZOrderOnTop(false);
        mLocalView.setVisibility(View.GONE);
        mRemoteView.setZOrderMediaOverlay(false);
        mRemoteView.setVisibility(View.GONE);
        mVideoLayout.setVisibility(View.GONE);
        mContainerRl.setVisibility(View.VISIBLE);
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


    /************************************************************************************************************/
    //ChildEventListener
    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        if (dataSnapshot != null) {
            String uid = dataSnapshot.getKey();
            if (!mLocalUid.equals(uid)) {
                mUserListData.add(uid);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        if (dataSnapshot != null) {
            String uid = dataSnapshot.getKey();
            if (!mLocalUid.equals(uid)) {
                mUserListData.remove(uid);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onCancelled(SyncError syncError) {

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
                    Toast.makeText(UserActivity.this, "ConversationCons.ACCEPTED", Toast.LENGTH_SHORT).show();
                    break;
                case ConversationCons.STREAM_RECEIVED:
                    onCall = true;
                    WilddogVideoService.VideoStream videoStream = (WilddogVideoService.VideoStream) message.obj;
                    initVideoViews();
                    videoStream.mLocalStream.attach(mLocalView);
                    videoStream.mRemoteStream.attach(mRemoteView);
                    break;
                case ConversationCons.REJECTED:
                    Toast.makeText(UserActivity.this, "ConversationCons.REJECTED", Toast.LENGTH_SHORT).show();
                    break;
                case ConversationCons.BUSY:
                    Toast.makeText(UserActivity.this, "ConversationCons.BUSY", Toast.LENGTH_SHORT).show();
                    break;
                case ConversationCons.TIMEOUT:
                    Toast.makeText(UserActivity.this, "ConversationCons.TIMEOUT", Toast.LENGTH_SHORT).show();
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
