package com.zd.wilddogdemo;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UserListActivity extends AppCompatActivity implements WilddogVideo.Listener,
        Conversation.Listener, Conversation.StatsListener, ChildEventListener{

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


    private WilddogVideo mWilddogVideo;
    private Conversation mVideoConversation;
    private LocalStream mLocalStream;
    private RemoteStream mRemoteStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        ButterKnife.bind(this);

        setupContainer();

        setupUserListListener();

//        initVideoConversation();
        mWilddogVideo = WilddogVideo.getInstance();
        mWilddogVideo.setListener(this);

    }

    @Override
    protected void onDestroy() {
        WilddogSync.getInstance().getReference("user")
                .child(WilddogAuth.getInstance().getCurrentUser().getUid()).removeValue();
        mWilddogVideo.setListener(null);
        super.onDestroy();
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

    private void setupUserListListener() {
        mUid = getIntent().getStringExtra("uid");
        SyncReference syncReference = WilddogSync.getInstance().getReference("user");
        syncReference.addChildEventListener(this);
    }


    /**************************************************************************************/
    private void initVideoConversation() {

        mLocalStream = mWilddogVideo.createLocalStream(
                new LocalStreamOptions.Builder()
                        .dimension(LocalStreamOptions.Dimension.DIMENSION_480P)
                        .build());
        mRemoteView.setZOrderMediaOverlay(true);
        mLocalView.setVisibility(View.VISIBLE);
        mLocalView.setZOrderOnTop(true);
        mLocalView.setMirror(true);
    }


    public void call(String remote_uid) {
        initVideoConversation();
        mVideoConversation = mWilddogVideo.call(remote_uid, mLocalStream, "");
        mVideoConversation.setConversationListener(this);
        mVideoConversation.setStatsListener(this);

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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(UserListActivity.this, "对方拒绝了你的视频通话请求", Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            case BUSY:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(UserListActivity.this, "对方正在通话中", Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            case TIMEOUT:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(UserListActivity.this, "呼叫超时，请稍后再呼叫", Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            default:
                break;
        }
    }

    @Override
    public void onStreamReceived(RemoteStream remoteStream) {
        mContainerRl.setVisibility(View.GONE);
        mVideoLayout.setVisibility(View.VISIBLE);
        mLocalStream.attach(mLocalView);
        mRemoteStream = remoteStream;
        mRemoteStream.attach(mRemoteView);
    }

    @Override
    public void onClosed() {
        closeConversation();
    }

    private void closeConversation() {
        if (mVideoConversation != null) {
            mVideoConversation.close();
        }
        mVideoConversation = null;
        if (mRemoteStream != null) {
            mRemoteStream.detach();
        }
        mLocalStream.detach();
        mRemoteStream = null;
        mLocalStream = null;
        mLocalView.setVisibility(View.GONE);
        mContainerRl.setVisibility(View.VISIBLE);
        mVideoLayout.setVisibility(View.GONE);
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
                        initVideoConversation();
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
}
