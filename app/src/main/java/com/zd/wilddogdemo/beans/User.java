package com.zd.wilddogdemo.beans;

import java.io.Serializable;

/**
 * Created by dongjijin on 2017/9/5 0005.
 */

public class User implements Serializable{
    private boolean isDoctor;
    private String uid;
    private String wilddog_login_token;
    private String token;
    private Long last_login_time;
    private String phone;

    public boolean isDoctor() {
        return isDoctor;
    }

    public void setDoctor(boolean doctor) {
        isDoctor = doctor;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getWilddog_login_token() {
        return wilddog_login_token;
    }

    public void setWilddog_login_token(String wilddog_login_token) {
        this.wilddog_login_token = wilddog_login_token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getLast_login_time() {
        return last_login_time;
    }

    public void setLast_login_time(Long last_login_time) {
        this.last_login_time = last_login_time;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isOverdue(){
        final long maxAliveTime = 24 * 60 * 60;
        long currentTime = System.currentTimeMillis() / 1000;
        return (currentTime - last_login_time) > maxAliveTime;
    }

    @Override
    public String toString() {
        return "User{" +
                "isDoctor=" + isDoctor +
                ", uid='" + uid + '\'' +
                ", wilddog_login_token='" + wilddog_login_token + '\'' +
                ", token='" + token + '\'' +
                ", last_login_time=" + last_login_time +
                ", phone='" + phone + '\'' +
                '}';
    }
}
