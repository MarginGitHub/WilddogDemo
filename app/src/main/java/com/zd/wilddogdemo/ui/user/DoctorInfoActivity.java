package com.zd.wilddogdemo.ui.user;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.zd.wilddogdemo.R;
import com.zd.wilddogdemo.beans.DialInfo;
import com.zd.wilddogdemo.beans.Doctor;
import com.zd.wilddogdemo.net.NetServiceConfig;
import com.zd.wilddogdemo.ui.BaseActivity;
import com.zd.wilddogdemo.utils.GlideApp;
import com.zd.wilddogdemo.utils.Util;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DoctorInfoActivity extends BaseActivity {

    @BindView(R.id.head_iv)
    ImageView mHeadIv;
    @BindView(R.id.nick_name)
    TextView mNickName;
    @BindView(R.id.video_price)
    TextView mVideoPrice;
    @BindView(R.id.video_count)
    TextView mVideoCount;
    @BindView(R.id.follow_count)
    TextView mFollowCount;
    private Doctor mDoctor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_info);
        ButterKnife.bind(this);
        mDoctor = (Doctor) getIntent().getSerializableExtra("doctor");
        initViews();
    }

    protected void initViews() {
        Util.setAvatarView(getApplicationContext(), mHeadIv, NetServiceConfig.HEAD_IMAGE_BASE_URL);
        mNickName.setText(mDoctor.getNick_name());
        mVideoPrice.setText(mDoctor.getVideo_price());
        mVideoCount.setText(mDoctor.getVideo_count());
        mFollowCount.setText(mDoctor.getFollow_count());
    }

    @OnClick(R.id.dial)
    public void onViewClicked() {
        int amount = mUser.getAmount().intValue();
        if (amount <= 0) {
            Toast.makeText(this, "您账号所剩余额不足1元，请先充值再进行拨号", Toast.LENGTH_LONG).show();
            return;
        }

        final DialInfo info = new DialInfo(mDoctor);
        info.setMaxConversationTime(amount);
        Dialog dialog = new AlertDialog.Builder(this)
                .setMessage(String.format("您当前账户所剩余额%d元,预期可以进行%d分钟视频通话,是否进行拨号？", amount, amount))
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(DoctorInfoActivity.this, DialActivity.class);
                        intent.putExtra("dial_info", info);
                        startActivity(intent);
                    }
                })
                .create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }


}
