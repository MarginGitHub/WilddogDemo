package com.zd.wilddogdemo.beans;

/**
 * Created by dongjijin on 2017/9/8 0008.
 */

public class Login {
    private boolean isDoctor;
    private String wilddogLoginToken;
    private String mobile;
    private String pwd;
    private long last_login_time;

    public boolean isDoctor() {
        return isDoctor;
    }

    public void setDoctor(boolean doctor) {
        isDoctor = doctor;
    }

    public String getWilddogLoginToken() {
        return wilddogLoginToken;
    }

    public void setWilddogLoginToken(String wilddogLoginToken) {
        this.wilddogLoginToken = wilddogLoginToken;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public long getLast_login_time() {
        return last_login_time;
    }

    public void setLast_login_time(long last_login_time) {
        this.last_login_time = last_login_time;
    }

    public boolean isOverdue() {
        final long maxAliveTime = 24 * 60 * 60;
        long currentTime = System.currentTimeMillis() / 1000;
        return (currentTime - last_login_time) > maxAliveTime;
    }

}
