package com.zd.wilddogdemo;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.wilddog.client.ChildEventListener;
import com.wilddog.client.DataSnapshot;
import com.wilddog.client.SyncError;
import com.wilddog.client.SyncReference;
import com.wilddog.client.ValueEventListener;
import com.wilddog.client.WilddogSync;
import com.wilddog.wilddogauth.WilddogAuth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserListActivity extends AppCompatActivity {

    @BindView(R.id.container_rl)
    RecyclerView mContainerRl;
    private UserListAdapter mAdapter;
    private List<String> mUserListData;
    private String mUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        ButterKnife.bind(this);

        setupContainer();

        setupUserListListener();

        setupCallStatesListener();

    }

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
        SyncReference syncReference = WilddogSync.getInstance().getReference("users");
        syncReference.addChildEventListener(new ChildEventListener() {
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
        });
    }

    private void setupCallStatesListener() {
        SyncReference states = WilddogSync.getInstance().getReference("state");
        states.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String uid = dataSnapshot.getKey();
                String currentUid = WilddogAuth.getInstance().getCurrentUser().getUid();
                if (currentUid.equals(uid)) {
                    Intent intent = new Intent(UserListActivity.this, MainActivity.class);
                    startActivity(intent);
                    resetCallState(currentUid);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(SyncError syncError) {

            }

            private void resetCallState(String uid) {
                SyncReference state = WilddogSync.getInstance().getReference("state");
                state.child(uid).removeValue();
                state.child(uid).onDisconnect().removeValue();
            }
        });
    }

    static class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserListViewHolder> {
        private List<String> mData;
        private Activity mContext;

        public UserListAdapter(Activity context) {
            mContext = context;
        }

        public void setData(List<String> data) {
            mData = data;
            notifyDataSetChanged();
        }

        @Override
        public UserListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_list, parent, false);
            return new UserListViewHolder(view, mContext);
        }

        @Override
        public void onBindViewHolder(UserListViewHolder holder, int position) {
            String uid = mData.get(position);
            holder.setRemoteId(uid);
        }

        @Override
        public int getItemCount() {
            if (mData == null) {
                return 0;
            }
            return mData.size();
        }

        static class UserListViewHolder extends RecyclerView.ViewHolder {
            public TextView mRemoteIdTv;
            public Button mCallBtn;
            private String mRemoteId;

            public UserListViewHolder(View itemView, final Activity context) {
                super(itemView);
                mRemoteIdTv = (TextView) itemView.findViewById(R.id.remote_id);
                mCallBtn = (Button) itemView.findViewById(R.id.call_btn);
                mCallBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setCallState(getRemoteId());
                        Intent intent = new Intent(context, MainActivity.class);
                        intent.putExtra("remote_id", getRemoteId());
                        context.startActivity(intent);
                    }

                    private void setCallState(String remoteID) {
                        Map<String, Object> state = new HashMap();
                        state.put(remoteID, true);
                        WilddogSync.getInstance().getReference("state").updateChildren(state);
                    }

                });
            }

            public void setRemoteId(String remoteId) {
                mRemoteId = remoteId;
                mRemoteIdTv.setText(mRemoteId);
            }

            private String getRemoteId() {
                return mRemoteId;
            }
        }
    }
}
