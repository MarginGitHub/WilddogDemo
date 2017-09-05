package com.zd.wilddogdemo.ui;

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
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.wilddog.client.ChildEventListener;
import com.wilddog.client.DataSnapshot;
import com.wilddog.client.GenericTypeIndicator;
import com.wilddog.client.SyncError;
import com.wilddog.client.SyncReference;
import com.wilddog.client.WilddogSync;
import com.wilddog.video.WilddogVideoView;
import com.wilddog.wilddogauth.WilddogAuth;
import com.zd.wilddogdemo.R;
import com.zd.wilddogdemo.adapter.DoctorListAdapter;
import com.zd.wilddogdemo.beans.Doctor;
import com.zd.wilddogdemo.beans.Result;
import com.zd.wilddogdemo.beans.User;
import com.zd.wilddogdemo.cons.ConversationCons;
import com.zd.wilddogdemo.net.NetService;
import com.zd.wilddogdemo.net.NetServiceConfig;
import com.zd.wilddogdemo.net.NetServiceProvider;
import com.zd.wilddogdemo.service.WilddogVideoService;
import com.zd.wilddogdemo.utils.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class UserActivity extends AppCompatActivity implements ChildEventListener, ServiceConnection {

    @BindView(R.id.container_rl)
    RecyclerView mContainerRl;
    @BindView(R.id.remote_view)
    WilddogVideoView mRemoteView;
    @BindView(R.id.local_view)
    WilddogVideoView mLocalView;
    @BindView(R.id.video_layout)
    FrameLayout mVideoLayout;
//    @BindView(R.id.refreshlayout)
//    SmoothRefreshLayout mRefreshlayout;

    private DoctorListAdapter mAdapter;
    private List<Doctor> mDoctorListData;
    private List<Doctor> mOnlineDoctorListData;
    private Messenger mServerMessenger;
    private String mLocalUid;
    private boolean isBinded;
    private NetService mNetService;
    private User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        ButterKnife.bind(this);

        mUser = (User) getIntent().getSerializableExtra("user");

        getDoctorList();
        setupContainer();
//        setupUserListDataListener();
        setupRefreshLayout();
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

    private void setupRefreshLayout() {
    }

    private void getDoctorList() {
        mNetService = (NetService) NetServiceProvider.instance(this)
                .provider(NetService.class, NetServiceConfig.SERVER_BASE_URL);

        String ts = String.valueOf(System.currentTimeMillis() / 1000);
        String apiKey = "test";
        String userId = mUser.getUid();
        int start = 0;
        int count = 100;

        HashMap<String, String> params = new HashMap<>();
        params.put("ts", ts);
        params.put("apiKey", apiKey);
        params.put("userId", userId);
        params.put("start", String.valueOf(start));
        params.put("count", String.valueOf(count));
        String sign = Util.sign(params);
        mNetService.getDoctorList(ts, apiKey, sign, userId, start, count)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Result<List<Doctor>>>() {
                    public static final String TAG = "getDoctorList";

                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull Result<List<Doctor>> listResult) {
                        if (listResult.getCode() == 100) {
                            mDoctorListData = listResult.getData();
                            setupUserListDataListener();
                        }
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        Log.e(TAG, "onError: " + e.toString());
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: ");
                    }
                });
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
        mOnlineDoctorListData = new ArrayList<>();
        mAdapter = new DoctorListAdapter(this);
        mContainerRl.setAdapter(mAdapter);
        mAdapter.setData(mOnlineDoctorListData);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mContainerRl.setLayoutManager(linearLayoutManager);
    }

    private void setupUserListDataListener() {
        mLocalUid = WilddogAuth.getInstance().getCurrentUser().getUid();
        SyncReference syncReference = WilddogSync.getInstance()
                .getReference(getResources().getString(R.string.doctors_room));
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

    public void callPeer(final String remoteUid) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage(String.format("确认呼叫%s", remoteUid))
                .setNegativeButton("取消", null)
                .setPositiveButton("呼叫", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Message msg = Message.obtain();
                        msg.what = ConversationCons.RING_UP;
                        msg.obj = remoteUid;
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
            for (Doctor doctor: mDoctorListData) {
                if (doctor.getUser_id().equals(uid)) {
                    mOnlineDoctorListData.add(doctor);
                    break;
                }
            }
//            Doctor doctor = mDoctorListData.get(0);
//            doctor.setUser_id(uid);
//            mOnlineDoctorListData.add(doctor);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        if (dataSnapshot != null) {
            String uid = dataSnapshot.getKey();
            for (Doctor doctor: mDoctorListData) {
                if (doctor.getUser_id().equals(uid)) {
                    mOnlineDoctorListData.remove(doctor);
                    break;
                }
            }
            mAdapter.notifyDataSetChanged();
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
