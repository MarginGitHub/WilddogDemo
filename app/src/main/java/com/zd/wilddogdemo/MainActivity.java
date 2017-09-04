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

import com.wilddog.client.ChildEventListener;
import com.wilddog.client.DataSnapshot;
import com.wilddog.client.SyncError;
import com.wilddog.client.SyncReference;
import com.wilddog.client.WilddogSync;
import com.wilddog.wilddogauth.WilddogAuth;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity implements ServiceConnection, ChildEventListener {


    private static final int REQUEST_CODE = 0xff;
    @BindView(R.id.container_rl)
    RecyclerView mContainerRl;

    private MainAdapter mAdapter;
    private List<String> mUserListData;
    private boolean isBinded = false;
    private Messenger mMessenger;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            if (message.what == VideoConversationCons.CALLED) {
//                如果服务接收到Peer呼叫请求，那么在绑定情况下，先解绑服务，然后打开会话Activity
                if (isBinded) {
                    unbindVideoService();
                    Intent intent = new Intent(MainActivity.this, VideoConversationActivity.class);
                    intent.putExtra("call", true);
                    startActivityForResult(intent, REQUEST_CODE);
                }
            }
            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setupContainer();
        setupUserListDataListener();
        bindVideoService();

    }

    @Override
    protected void onDestroy() {
        unbindVideoService();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        主要处理从视频通话界面返回以后进行Video Conversation Service的绑定工作
        if (requestCode == REQUEST_CODE) {
            if (!isBinded) {
                bindVideoService();
            }
        }
    }

    private void bindVideoService() {
        Intent intent = new Intent(this, WilddogVideoService.class);
        bindService(intent, this, Service.BIND_AUTO_CREATE);
        isBinded = true;
    }

    private void unbindVideoService() {
        unbindService(this);
        isBinded = false;
    }

    private void setupContainer() {
        mUserListData = new ArrayList();
        mAdapter = new MainAdapter(this);
        mContainerRl.setAdapter(mAdapter);
        mAdapter.setData(mUserListData);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mContainerRl.setLayoutManager(linearLayoutManager);
    }

    private void setupUserListDataListener() {
        SyncReference syncReference = WilddogSync.getInstance().getReference(
                getResources()
                        .getString(R.string.video_conversation_room));
        syncReference.addChildEventListener(this);
    }


    public void callPeer(String remote_uid) {
        Message msg = new Message();
        msg.what = VideoConversationCons.CALL;
        msg.obj = remote_uid;
        try {
            mMessenger.send(msg);
            unbindVideoService();
            Intent intent = new Intent(this, VideoConversationActivity.class);
            startActivity(intent);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /************************************************************************************************************/
    //ChildEventListener
    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        if (dataSnapshot != null) {
            String currentUID = WilddogAuth.getInstance().getCurrentUser().getUid();
            String uid = dataSnapshot.getKey();
            if (!currentUID.equals(uid)) {
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
            String currentUID = WilddogAuth.getInstance().getCurrentUser().getUid();
            String uid = dataSnapshot.getKey();
            if (!currentUID.equals(uid)) {
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
        mMessenger = new Messenger(binder);
        Message message = Message.obtain();
        message.replyTo = new Messenger(mHandler);
        message.what = VideoConversationCons.CONNECTED;
        try {
            mMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        mMessenger = null;
    }
}
