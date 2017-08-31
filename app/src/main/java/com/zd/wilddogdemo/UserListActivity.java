package com.zd.wilddogdemo;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wilddog.client.ChildEventListener;
import com.wilddog.client.DataSnapshot;
import com.wilddog.client.SyncError;
import com.wilddog.client.SyncReference;
import com.wilddog.client.WilddogSync;
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

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UserListActivity extends AppCompatActivity implements WilddogVideo.Listener,
        Conversation.Listener, Conversation.StatsListener, ChildEventListener, ServiceConnection {

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
    private String mUid;


    private LocalStream mLocalStream;
    private RemoteStream mRemoteStream;
    private WilddogVideoService.ConversionBinder mBinder;
    private Conversation mVideoConversation;
    private boolean isInVideoConversation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        ButterKnife.bind(this);

        mUid = getIntent().getStringExtra("uid");
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
        unbindVideoService();
        super.onDestroy();
    }

    private void bindVideoService() {
        Intent intent = new Intent(this, WilddogVideoService.class);
        bindService(intent, this, Service.BIND_AUTO_CREATE);
    }

    private void unbindVideoService() {
        unbindService(this);
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
        mUid = getIntent().getStringExtra("uid");
        SyncReference syncReference = WilddogSync.getInstance().getReference("userlist");
        syncReference.addChildEventListener(this);
    }


    /**************************************************************************************/
    private void initLocalStream() {
        mLocalStream = WilddogVideo.getInstance().createLocalStream(
                new LocalStreamOptions.Builder()
                        .dimension(LocalStreamOptions.Dimension.DIMENSION_480P)
                        .build());
    }

    private void initVideoViews() {
        mRemoteView.setVisibility(View.VISIBLE);
        mRemoteView.setZOrderMediaOverlay(true);
        mLocalView.setVisibility(View.VISIBLE);
        mLocalView.setZOrderOnTop(true);
        mLocalView.setMirror(true);

//        将音视频流和View绑定
        mLocalStream.attach(mLocalView);
        mRemoteStream.attach(mRemoteView);
        mLocalStream.enableAudio(true);
        mRemoteStream.enableAudio(true);

//       接收到对方音视频流以后先把用户列表list给GONE掉，然后让VideoLayout显示出来
        mContainerRl.setVisibility(View.GONE);
        mVideoLayout.setVisibility(View.VISIBLE);

    }


    public void callPeer(String remote_uid) {
        if (mLocalStream == null) {
            initLocalStream();
        }
        mVideoConversation = WilddogVideo.getInstance().call(remote_uid, mLocalStream, "");
        mVideoConversation.setConversationListener(this);
        mVideoConversation.setStatsListener(this);

    }

    private void closeConversation() {
        if (isInVideoConversation) {
//            关闭会话
            mVideoConversation.close();
            mVideoConversation = null;
//            将流和VideoView解绑
            mRemoteStream.detach();
            mRemoteStream = null;
            mLocalStream.detach();
            mLocalStream = null;
//            设置View属性
            mLocalView.setMirror(false);
            mLocalView.setZOrderOnTop(false);
            mLocalView.setVisibility(View.GONE);
            mRemoteView.setZOrderMediaOverlay(false);
            mRemoteView.setVisibility(View.GONE);
            mVideoLayout.setVisibility(View.GONE);
            mContainerRl.setVisibility(View.VISIBLE);

            isInVideoConversation = false;
        }
    }

    @OnClick(R.id.hung_up)
    public void onViewClicked() {
        closeConversation();
    }

    /*************************************************************************************************************/
    //Conversation.Listener
    @Override
    public void onCallResponse(CallStatus callStatus) {
        switch (callStatus) {
            case ACCEPTED:
                break;
            case REJECTED:
                Toast.makeText(UserListActivity.this, "对方拒绝了你的视频通话请求", Toast.LENGTH_SHORT).show();
                break;
            case BUSY:
                Toast.makeText(UserListActivity.this, "对方正在通话中", Toast.LENGTH_SHORT).show();
                break;
            case TIMEOUT:
                Toast.makeText(UserListActivity.this, "呼叫超时，请稍后再呼叫", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    @Override
    public void onStreamReceived(RemoteStream remoteStream) {
        mRemoteStream = remoteStream;
        initVideoViews();
        isInVideoConversation = true;
    }

    @Override
    public void onClosed() {
        closeConversation();
    }


    @Override
    public void onError(WilddogVideoError wilddogVideoError) {

    }

    /************************************************************************************************************/
    //Conversation.StatsListener
    @Override
    public void onLocalStreamStatsReport(LocalStreamStatsReport localStreamStatsReport) {

    }

    @Override
    public void onRemoteStreamStatsReport(RemoteStreamStatsReport remoteStreamStatsReport) {

    }

    /**************************************************************************************************************/
    //WilddogVideo.Listener
    @Override
    public void onCalled(final Conversation conversation, String s) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage(String.format("用户%s邀请你进行视频通话", conversation.getRemoteUid()))
                .setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        conversation.reject();
                    }
                })
                .setPositiveButton("接受", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (mLocalStream == null) {
                            initLocalStream();
                        }
                        mVideoConversation = conversation;
                        mVideoConversation.accept(mLocalStream);
                        mVideoConversation.setConversationListener(UserListActivity.this);
                        mVideoConversation.setStatsListener(UserListActivity.this);
                    }
                })
                .create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    @Override
    public void onTokenError(WilddogVideoError wilddogVideoError) {

    }


    /************************************************************************************************************/
    //ChildEventListener
    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        if (dataSnapshot != null) {
            String uid = dataSnapshot.getKey();
            if (!mUid.equals(uid)) {
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
            if (!mUid.equals(uid)) {
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
        mBinder = (WilddogVideoService.ConversionBinder) binder;
        mBinder.setVideoListener(this);
        if (wakeupFromService()) {
//            mVideoConversation = mBinder.getVideoConversation();
//            mVideoConversation.setConversationListener(this);
//            mVideoConversation.setStatsListener(this);
            onCalled(mBinder.getVideoConversation(), "");
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        mBinder = null;
    }
}
