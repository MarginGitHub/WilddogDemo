package com.zd.wilddogdemo.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.wilddog.client.ChildEventListener;
import com.wilddog.client.DataSnapshot;
import com.wilddog.client.SyncError;
import com.wilddog.client.SyncReference;
import com.wilddog.client.WilddogSync;
import com.wilddog.video.WilddogVideo;
import com.wilddog.wilddogauth.WilddogAuth;
import com.yalantis.ucrop.UCrop;
import com.zd.wilddogdemo.R;
import com.zd.wilddogdemo.adapter.DoctorListAdapter;
import com.zd.wilddogdemo.adapter.VideoConversationFragmentPagerAdapter;
import com.zd.wilddogdemo.beans.Doctor;
import com.zd.wilddogdemo.beans.DoctorNotShownList;
import com.zd.wilddogdemo.beans.Result;
import com.zd.wilddogdemo.beans.User;
import com.zd.wilddogdemo.net.Net;
import com.zd.wilddogdemo.net.NetService;
import com.zd.wilddogdemo.net.NetServiceConfig;
import com.zd.wilddogdemo.net.NetServiceProvider;
import com.zd.wilddogdemo.storage.ObjectPreference;
import com.zd.wilddogdemo.ui.View.NoSlidingViewPaper;
import com.zd.wilddogdemo.ui.fragment.AboutMeFragment;
import com.zd.wilddogdemo.ui.fragment.OnlineDoctorListFragment;
import com.zd.wilddogdemo.utils.Util;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;



public class MainActivity extends AppCompatActivity implements DoctorListAdapter.ICallPeer, ChildEventListener {

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
    public User mUser;


    public List<Doctor> mOnlineDoctorList = new ArrayList<>();
    private DoctorNotShownList mOnlineDoctorUidList = new DoctorNotShownList();
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
        initUser();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        logout();
    }

    @Override
    protected void onActivityResult(final int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        if (requestCode == UCrop.REQUEST_CROP) {
            if (resultCode == RESULT_OK) {
                final Uri output = UCrop.getOutput(data);
                Net.instance().uploadUserHeadImage(mUser.getToken(), mUser.getUser_id(), output.getPath(), new Net.OnNext<Result<String>>() {
                            @Override
                            public void onNext(@io.reactivex.annotations.NonNull Result<String> result) {
                                if (result.getCode() == 100) {
                                    AboutMeFragment aboutMeFragment = (AboutMeFragment) mPagerAdapter.getItem(2);
                                    Util.setImageView(MainActivity.this, aboutMeFragment.mHeadIv, output.getPath());
                                    mUser.setHead_img_path(output.getPath());
                                    mUser.setHead_img_url(result.getData());
                                    ObjectPreference.saveObject(getApplicationContext(), mUser);
                                }
                            }
                        },
                        new Net.OnError() {
                            @Override
                            public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                                Log.d("uploadHead", "onError: " + e.toString());
                            }
                        });
            } else if (resultCode == UCrop.RESULT_ERROR) {

            }
        }
    }

    private void logout() {
        WilddogVideo.getInstance().stop();
        WilddogAuth.getInstance().signOut();
        WilddogSync.goOffline();
    }

    private void initUser() {
        mUser = ObjectPreference.getObject(getApplicationContext(), User.class);
    }

    private void initViews() {
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
        Intent intent = new Intent(this, DialActivity.class);
        intent.putExtra("user", mUser);
        intent.putExtra("doctor", doctor);
        startActivity(intent);
    }

    /**********************************************************************/
    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        if (dataSnapshot != null) {
            String uid = dataSnapshot.getKey();
            final OnlineDoctorListFragment fragment = mPagerAdapter.getOnlineDoctorListFragment();
            if (fragment.isViewDestroied()) {
                if (mOnlineDoctorList.size() < 5) {
                    Net.instance().getDoctorInfo(mUser.getUser_id(), uid, mUser.getToken(), new Net.OnNext<Result<Doctor>>() {
                                @Override
                                public void onNext(@io.reactivex.annotations.NonNull Result<Doctor> result) {
                                    if (result.getCode() == 100) {
                                        mOnlineDoctorList.add(result.getData());
                                    }
                                }
                            },
                            new Net.OnError() {
                                @Override
                                public void onError(@io.reactivex.annotations.NonNull Throwable e) {

                                }
                            });
                } else {
                    mOnlineDoctorUidList.add(uid);
                }
            } else if (fragment.isViewFull()) {
                mOnlineDoctorUidList.add(uid);
            } else {
                Net.instance().getDoctorInfo(mUser.getUser_id(), uid, mUser.getToken(), new Net.OnNext<Result<Doctor>>() {
                            @Override
                            public void onNext(@io.reactivex.annotations.NonNull Result<Doctor> result) {
                                if (result.getCode() == 100) {
                                    fragment.getDoctorListAdapter().addItem(result.getData());
                                }
                            }
                        },
                        new Net.OnError() {
                            @Override
                            public void onError(@io.reactivex.annotations.NonNull Throwable e) {

                            }
                        });
            }
        }
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        if (dataSnapshot != null) {
            String uid = dataSnapshot.getKey();
            if (!mOnlineDoctorUidList.contains(uid)) {
                Net.instance().getDoctorInfo(mUser.getUser_id(), uid, mUser.getToken(), new Net.OnNext<Result<Doctor>>() {
                            @Override
                            public void onNext(@io.reactivex.annotations.NonNull Result<Doctor> result) {
                                if (result.getCode() != 100) {
                                    return;
                                }
                                Doctor doctor = result.getData();
                                OnlineDoctorListFragment fragment = mPagerAdapter.getOnlineDoctorListFragment();
                                if (fragment.isViewDestroied()) {
                                    int pos = -1;
                                    for (int i = 0; i < mOnlineDoctorList.size(); i++) {
                                        if (mOnlineDoctorList.get(i).getUser_id().equals(doctor.getUser_id())) {
                                            pos = i;
                                            break;
                                        }
                                    }
                                    if (pos != -1 && pos < mOnlineDoctorList.size()) {
                                        mOnlineDoctorList.remove(pos);
                                    }
                                    mOnlineDoctorList.add(doctor);
                                } else {
                                    fragment.getDoctorListAdapter().updateItem(doctor);
                                }
                            }
                        },
                        new Net.OnError() {
                            @Override
                            public void onError(@io.reactivex.annotations.NonNull Throwable e) {

                            }
                        });
            }

        }
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {

        if (dataSnapshot != null) {
            String uid = dataSnapshot.getKey();
            if (!mOnlineDoctorUidList.contains(uid)) {
                OnlineDoctorListFragment fragment = mPagerAdapter.getOnlineDoctorListFragment();
                if (fragment.isViewDestroied()) {
                    int pos = -1;
                    for (int i = 0; i < mOnlineDoctorList.size(); i++) {
                        if (mOnlineDoctorList.get(i).getUser_id().equals(uid)) {
                            pos = i;
                            break;
                        }
                    }
                    if (pos != -1 && pos < mOnlineDoctorList.size()) {
                        mOnlineDoctorList.remove(pos);
                    }
                } else {
                    fragment.getDoctorListAdapter().removeItem(uid);
                }
            } else {
                mOnlineDoctorUidList.remove(uid);
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
