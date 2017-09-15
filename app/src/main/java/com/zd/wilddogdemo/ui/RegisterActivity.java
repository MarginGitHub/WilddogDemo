package com.zd.wilddogdemo.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import com.zd.wilddogdemo.R;
import com.zd.wilddogdemo.beans.Login;
import com.zd.wilddogdemo.beans.Result;
import com.zd.wilddogdemo.beans.User;
import com.zd.wilddogdemo.net.Net;
import com.zd.wilddogdemo.storage.ObjectPreference;
import com.zd.wilddogdemo.utils.Util;

import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.annotations.NonNull;

import static com.zd.wilddogdemo.utils.Util.isPasswordValid;

/**
 * A login screen that offers login via email/password.
 */
public class RegisterActivity extends AppCompatActivity {


    public static final int RESULT_CODE = 200;
    @BindView(R.id.phone)
    AutoCompleteTextView mPhone;
    @BindView(R.id.password1)
    AutoCompleteTextView mPassword1;
    @BindView(R.id.password2)
    AutoCompleteTextView mPassword2;
    @BindView(R.id.referee)
    AutoCompleteTextView mReferee;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Net.instance().removeRequest(RegisterActivity.class.getSimpleName());
    }

    @OnClick(R.id.register)
    public void onViewClicked() {
        final String phone = mPhone.getText().toString().trim();
        final String pwd1 = mPassword1.getText().toString().trim();
        String pwd2 = mPassword2.getText().toString().trim();
        final String referee = mReferee.getText().toString().trim();
        if (!Util.isPhoneValid(phone) || !Util.isPhoneValid(referee)) {
            Toast.makeText(this, "电话号码格式不正确", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Util.isPasswordValid(pwd1) || !Util.isPasswordValid(pwd2)) {
            Toast.makeText(this, "密码长度最少为6位且不能有特殊字符", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!pwd1.equals(pwd2)) {
            Toast.makeText(this, "密码不一致", Toast.LENGTH_SHORT).show();
            return;
        }

        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("正在注册，请稍等");
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        Net.instance().register(phone, pwd1, referee,
                new Net.OnNext<Result<User>>() {
                    @Override
                    public void onNext(Result<User> result) {
                        if (result.getCode() == 100) {
                            Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                            User user = result.getData();
                            Login login = new Login(user.getNick_name(), phone, pwd1, user.getHead_img_url(), false);
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            intent.putExtra("login", login);
                            setResult(RESULT_CODE, intent);
                            finish();
                        } else {
                            Toast.makeText(RegisterActivity.this, result.getMsg(), Toast.LENGTH_SHORT).show();
                        }

                        dialog.dismiss();
                    }
                },
                new Net.OnError() {
                    @Override
                    public void onError(@NonNull Throwable e) {
                        dialog.dismiss();
                    }
                }, RegisterActivity.class.getSimpleName());

    }
}

