package com.zd.wilddogdemo.ui;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.wilddog.client.WilddogSync;
import com.wilddog.video.WilddogVideo;
import com.wilddog.wilddogauth.WilddogAuth;
import com.yalantis.ucrop.UCrop;
import com.zd.wilddogdemo.R;
import com.zd.wilddogdemo.beans.Result;
import com.zd.wilddogdemo.beans.User;
import com.zd.wilddogdemo.net.Net;
import com.zd.wilddogdemo.net.NetServiceConfig;
import com.zd.wilddogdemo.storage.ObjectPreference;
import com.zd.wilddogdemo.utils.GlideApp;
import com.zd.wilddogdemo.utils.GlideRequests;
import com.zd.wilddogdemo.utils.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.annotations.NonNull;


public class DoctorActivity extends AppCompatActivity {

    private static final int CAPTURE_REQUEST_CODE = 100;
    private static final int ALBUM_REQUEST_CODE = 200;
    @BindView(R.id.head_iv)
    ImageView mHeadIv;
    @BindView(R.id.nick_name)
    TextView mNickName;
    @BindView(R.id.balance_account)
    TextView mBalanceAccount;

    private User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor);
        ButterKnife.bind(this);
        initViews();
    }

    private void initViews() {
        mUser = ObjectPreference.getObject(getApplicationContext(), User.class);

//        设置用户头像
        String path = mUser.getHead_img_path();
        String imgUrl = mUser.getHead_img_url();
        if (path != null) {
            Util.setImageView(this, mHeadIv, path);
        } else if (!TextUtils.isEmpty(imgUrl)) {
            Util.setImageView(this, mHeadIv, imgUrl);
        } else {
            Util.setImageView(this, mHeadIv, null);
        }

        mNickName.setText(mUser.getNick_name());
        mBalanceAccount.setText(String.format("余额: %f元", mUser.getAmount()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @OnClick({R.id.logout, R.id.head_iv, R.id.conversation_history})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.head_iv:
                changeHeadView();
                break;
            case R.id.conversation_history:
                Toast.makeText(this, "该功能暂未开放", Toast.LENGTH_SHORT).show();
                break;
            case R.id.logout:
                WilddogVideo.getInstance().stop();
                WilddogAuth.getInstance().signOut();
                WilddogSync.goOffline();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                break;
        }
    }


    private void changeHeadView() {
        View view = LayoutInflater.from(this).inflate(R.layout.view_change_head_img, null);
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .create();
        view.findViewById(R.id.camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCamera();
                dialog.dismiss();
            }
        });
        view.findViewById(R.id.album).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAlbum();
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
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, ALBUM_REQUEST_CODE);
        } else {

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CAPTURE_REQUEST_CODE:
                Uri uri = data.getData();
                Bitmap bitmap;
                if (uri != null) {
                    bitmap = BitmapFactory.decodeFile(uri.getPath());
                } else {
                    bitmap = (Bitmap) data.getExtras().get("data");
                }
                saveImageToGallery(bitmap);
                break;
            case ALBUM_REQUEST_CODE:
                Uri selectUri = data.getData();
                File imgFile = getHeadImgFile();
                if (selectUri != null) {
                    UCrop.of(selectUri, Uri.fromFile(imgFile))
                            .useSourceImageAspectRatio()
                            .start(this);
                }
                break;
            case UCrop.REQUEST_CROP:
                if (resultCode == RESULT_OK) {
                    final Uri output = UCrop.getOutput(data);
                    // 其次把文件插入到系统图库
                    try {
                        MediaStore.Images.Media.insertImage(getContentResolver(),
                                output.getPath(), "head.png", null);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    // 最后通知图库更新
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, output));
                    Net.instance().uploadDoctorHeadImage(mUser.getToken(), mUser.getUser_id(), output.getPath(), new Net.OnNext<Result<String>>() {
                                @Override
                                public void onNext(@NonNull Result<String> result) {
                                    if (result.getCode() == 100) {
                                        mUser.setHead_img_path(output.getPath());
                                        ObjectPreference.saveObject(getApplicationContext(), mUser);
                                        Util.setImageView(DoctorActivity.this, mHeadIv, output.getPath());
                                    }
                                }
                            },
                            new Net.OnError() {
                                @Override
                                public void onError(@NonNull Throwable e) {
                                    Log.d("uploadHead", "onError: " + e.toString());
                                }
                            });
                } else if (resultCode == UCrop.RESULT_ERROR) {

                }
                break;
            default:
                break;

        }
    }

    public void changeHeadView(Uri uri) {
        GlideApp.with(this)
                .load(uri)
                .placeholder(R.drawable.head)
                .circleCrop().into(mHeadIv);
    }

    private void saveImageToGallery(Bitmap bmp) {
        File file = getHeadImgFile();
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Uri uri = Uri.fromFile(file);
        UCrop.of(uri, uri)
                .useSourceImageAspectRatio()
                .start(this);

    }

    private File getHeadImgFile() {
        // 首先保存图片
        File appDir = new File(Environment.getExternalStorageDirectory(), "wilddog");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = "head.png";
        File file = new File(appDir, fileName);
        return file;
    }

}
