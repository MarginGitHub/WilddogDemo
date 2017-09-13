package com.zd.wilddogdemo.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.MenuItem;

import com.wilddog.client.ChildEventListener;
import com.wilddog.client.DataSnapshot;
import com.wilddog.client.SyncError;
import com.wilddog.client.SyncReference;
import com.wilddog.client.WilddogSync;
import com.wilddog.video.WilddogVideo;
import com.wilddog.wilddogauth.WilddogAuth;
import com.zd.wilddogdemo.R;
import com.zd.wilddogdemo.adapter.DoctorListAdapter;
import com.zd.wilddogdemo.adapter.DoctorListData;
import com.zd.wilddogdemo.adapter.VideoConversationFragmentPagerAdapter;
import com.zd.wilddogdemo.beans.Doctor;
import com.zd.wilddogdemo.beans.Result;
import com.zd.wilddogdemo.net.Net;
import com.zd.wilddogdemo.ui.BaseActivity;
import com.zd.wilddogdemo.ui.View.NoSlidingViewPaper;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends BaseActivity implements DoctorListAdapter.ICallPeer, ChildEventListener {

    @BindView(R.id.navigation)
    BottomNavigationView mNavigation;
    @BindView(R.id.content)
    NoSlidingViewPaper mContent;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.online_doctors:
                    mContent.setCurrentItem(0, true);
                    return true;
                case R.id.follow_doctors:
                    mContent.setCurrentItem(1, true);
                    return true;
                case R.id.about_me:
                    mContent.setCurrentItem(2, true);
                    return true;
            }
            return false;
        }

    };


    public DoctorListData mDoctorListData = new DoctorListData();
    private VideoConversationFragmentPagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initViews();
        setOnlineDoctorListListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Net.instance().removeRequest(MainActivity.class.getSimpleName());
        logout();
    }

    @Override
    protected void onActivityResult(final int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        Fragment fragment = getSupportFragmentManager().findFragmentByTag(mPagerAdapter.getFragmentTag(R.id.content, 2));
//        if (fragment != null) {
//            fragment.onActivityResult(requestCode, resultCode, data);
//        }
    }

    private void logout() {
        WilddogVideo.getInstance().stop();
        WilddogAuth.getInstance().signOut();
        WilddogSync.goOffline();
    }

    protected void initViews() {
        mNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        mPagerAdapter = new VideoConversationFragmentPagerAdapter(getSupportFragmentManager());
        mContent.setAdapter(mPagerAdapter);
    }

    private void setOnlineDoctorListListener() {
        SyncReference syncReference = WilddogSync.getInstance()
                .getReference(getResources().getString(R.string.doctors_room));
        syncReference.addChildEventListener(this);
    }

    public void callPeer(final Doctor doctor) {
        Intent intent = new Intent(this, DoctorInfoActivity.class);
        intent.putExtra("doctor", doctor);
        startActivity(intent);
    }

    /**********************************************************************/
    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        if (dataSnapshot != null) {
            String uid = dataSnapshot.getKey();
            if (mDoctorListData.getDoctorCount() < 10) {
                Net.instance().getDoctorInfo(mUser.getUser_id(), uid, mUser.getToken(), new Net.OnNext<Result<Doctor>>() {
                            @Override
                            public void onNext(@io.reactivex.annotations.NonNull Result<Doctor> result) {
                                if (result.getCode() == 100) {
                                    Doctor doctor = result.getData();
                                    mDoctorListData.addDoctor(doctor);
                                }
                            }
                        },
                        new Net.OnError() {
                            @Override
                            public void onError(@io.reactivex.annotations.NonNull Throwable e) {

                            }
                        }, MainActivity.class.getSimpleName());
            } else {
                mDoctorListData.addUid(uid);
            }
        }
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        Boolean noUpdate = (Boolean) dataSnapshot.getValue();
        if (noUpdate) {
            return;
        }
        if (dataSnapshot != null) {
            String uid = dataSnapshot.getKey();
            if (!mDoctorListData.contains(uid)) {
                Net.instance().getDoctorInfo(mUser.getUser_id(), uid, mUser.getToken(), new Net.OnNext<Result<Doctor>>() {
                            @Override
                            public void onNext(@io.reactivex.annotations.NonNull Result<Doctor> result) {
                                if (result.getCode() == 100) {
                                    mDoctorListData.updateDoctor(result.getData());
                                }
                            }
                        },
                        new Net.OnError() {
                            @Override
                            public void onError(@io.reactivex.annotations.NonNull Throwable e) {

                            }
                        }, MainActivity.class.getSimpleName());
            }
        }
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {

        if (dataSnapshot != null) {
            String uid = dataSnapshot.getKey();
            mDoctorListData.removeDoctor(uid);

        }
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        Log.d("onChildMoved", "onChildMoved: " + s);
    }

    @Override
    public void onCancelled(SyncError syncError) {

    }


}
