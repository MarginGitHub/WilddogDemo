package com.zd.wilddogdemo.beans;

import java.io.Serializable;

/**
 * Created by dongjijin on 2017/9/8 0008.
 */

public class Login implements Serializable{
    private String nickName;
    private String mobile;
    private String password;
    private String avatarUrl;
    private boolean autoLogin;

    public Login() {
    }

    public Login(String mobile, String password, boolean autoLogin) {
        this.mobile = mobile;
        this.password = password;
        this.autoLogin = autoLogin;
    }

    public Login(String mobile, String password, String avatarUrl, boolean autoLogin) {
        this.mobile = mobile;
        this.password = password;
        this.avatarUrl = avatarUrl;
        this.autoLogin = autoLogin;
    }

    public Login(String nickName, String mobile, String password, String avatarUrl, boolean autoLogin) {
        this.nickName = nickName;
        this.mobile = mobile;
        this.password = password;
        this.avatarUrl = avatarUrl;
        this.autoLogin = autoLogin;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public boolean isAutoLogin() {
        return autoLogin;
    }

    public void setAutoLogin(boolean autoLogin) {
        this.autoLogin = autoLogin;
    }
}
