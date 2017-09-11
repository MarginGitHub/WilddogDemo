package com.zd.wilddogdemo.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.wilddog.client.WilddogSync;
import com.wilddog.video.WilddogVideo;
import com.wilddog.wilddogauth.WilddogAuth;
import com.yalantis.ucrop.UCrop;
import com.zd.wilddogdemo.R;
import com.zd.wilddogdemo.beans.User;
import com.zd.wilddogdemo.net.NetServiceConfig;
import com.zd.wilddogdemo.ui.LoginActivity;
import com.zd.wilddogdemo.ui.MainActivity;
import com.zd.wilddogdemo.utils.GlideApp;
import com.zd.wilddogdemo.utils.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


/**
 * Created by dongjijin on 2017/9/6 0006.
 */

public class AboutMeFragment extends Fragment {

    private static final int CAPTURE_REQUEST_CODE = 1;
    private static final int ALBUM_REQUEST_CODE = 2;
    @BindView(R.id.head_iv)
    public ImageView mHeadIv;
    @BindView(R.id.nick_name)
    TextView mNickName;
    @BindView(R.id.balance_account)
    TextView mBalanceAccount;
    Unbinder unbinder;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about_me, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        initViews();
    }

    private void initViews() {
        User user = ((MainActivity) getActivity()).mUser;
        mNickName.setText(user.getNick_name());
        //        设置用户头像
        String path = ((MainActivity) getActivity()).mUser.getHead_img_path();
        String imgUrl = ((MainActivity) getActivity()).mUser.getHead_img_url();
        if (!TextUtils.isEmpty(path)) {
            Util.setImageView(this, mHeadIv, path);
        } else if (!TextUtils.isEmpty(imgUrl)) {
            Util.setImageView(this, mHeadIv, NetServiceConfig.HEAD_IMAGE_BASE_URL + imgUrl);
        } else {
            Util.setImageView(this, mHeadIv, null);
        }
        mBalanceAccount.setText(String.format("余额: %f元", user.getAmount()));
    }


    @OnClick({R.id.conversation_history, R.id.logout_button, R.id.head_iv})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.head_iv:
                changeHeadView();
                break;
            case R.id.conversation_history:
                break;
            case R.id.logout_button:
                WilddogVideo.getInstance().stop();
                WilddogAuth.getInstance().signOut();
                WilddogSync.goOffline();
                startActivity(new Intent(getContext(), LoginActivity.class));
                getActivity().finish();
                break;

            case R.id.camera:
                openCamera();
                break;
            case R.id.album:
                openAlbum();
                break;
        }
    }


    private void changeHeadView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_change_head_img, null);
        final AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(view)
                .create();
        view.findViewById(R.id.camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onViewClicked(view);
                dialog.dismiss();
            }
        });
        view.findViewById(R.id.album).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onViewClicked(view);
                dialog.dismiss();
            }
        });
        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void openCamera() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        startActivityForResult(intent, CAPTURE_REQUEST_CODE);
    }

    private void openAlbum() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Intent intent = new Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, ALBUM_REQUEST_CODE);
        } else {

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        switch (requestCode) {
            case CAPTURE_REQUEST_CODE:
                Uri uri = data.getData();
                Bitmap bitmap;
                if (uri != null) {
                    bitmap = BitmapFactory.decodeFile(uri.getPath());
                } else {
                    bitmap = (Bitmap) data.getExtras().get("data");
                }
                Util.saveImageToGallery(getActivity(), bitmap);
                break;
            case ALBUM_REQUEST_CODE:
                Uri selectUri = data.getData();
                File imgFile = Util.getHeadImgFile();
                if (selectUri != null) {
                    UCrop.of(selectUri, Uri.fromFile(imgFile))
                            .useSourceImageAspectRatio()
                            .start(getActivity());
                }
                break;

        }
    }



}
