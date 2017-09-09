package com.zd.wilddogdemo.beans;

import android.net.Uri;

import java.io.Serializable;

/**
 * Created by dongjijin on 2017/9/5 0005.
 */

public class User implements Serializable{
    /**
     * user_id : BE2C9E30E31B46E5B3C5C585354E3FD4
     * mobile : 13700000002
     * nick_name : 13700000002
     * head_img_url : 2017-08-10/598bd17a493ca.jpg
     * sex : 0
     * ref_user_id : 10000000000000000000000000000001
     * login_count : 45
     * last_login_time : 1504086412
     * create_time : 1502290667
     * token : 00159a6898c5c278
     * agora_token : 1:d872b2a63c634c05a2f004732cc6fada:1577808000:86efd621eb6d9f4ae043add2aee8eaca
     * wilddog_token : eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhZG1pbiI6ZmFsc2UsImRlYnVnIjpmYWxzZSwidWlkIjoiQkUyQzlFMzBFMzFCNDZFNUIzQzVDNTg1MzU0RTNGRDQiLCJ2IjoxLCJpYXQiOjE1MDQwODY0MTJ9.U40NP1FaUClD8erKbl0wrAOniUljREnVTeAK_y3V82c
     */

    private String user_id;
    private String mobile;
    private String nick_name;
    private String head_img_url;
//    头像的本地地址
    private String head_img_path;
    private String sex;
    private String ref_user_id;
    private int login_count;
    private int last_login_time;
    private String create_time;
    private String token;
    private String agora_token;
    private String wilddog_token;
    private Double amount;

    /*****************************************/
    private boolean isDoctor;
    private String wilddogVideoToken;
    private String pwd;

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getNick_name() {
        return nick_name;
    }

    public void setNick_name(String nick_name) {
        this.nick_name = nick_name;
    }

    public String getHead_img_url() {
        return head_img_url;
    }

    public void setHead_img_url(String head_img_url) {
        this.head_img_url = head_img_url;
    }

    public String getHead_img_path() {
        return head_img_path;
    }

    public void setHead_img_path(String head_img_path) {
        this.head_img_path = head_img_path;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getRef_user_id() {
        return ref_user_id;
    }

    public void setRef_user_id(String ref_user_id) {
        this.ref_user_id = ref_user_id;
    }

    public int getLogin_count() {
        return login_count;
    }

    public void setLogin_count(int login_count) {
        this.login_count = login_count;
    }

    public int getLast_login_time() {
        return last_login_time;
    }

    public void setLast_login_time(int last_login_time) {
        this.last_login_time = last_login_time;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getAgora_token() {
        return agora_token;
    }

    public void setAgora_token(String agora_token) {
        this.agora_token = agora_token;
    }

    public String getWilddog_token() {
        return wilddog_token;
    }

    public void setWilddog_token(String wilddog_token) {
        this.wilddog_token = wilddog_token;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public boolean isDoctor() {
        return isDoctor;
    }

    public void setDoctor(boolean doctor) {
        isDoctor = doctor;
    }

    public String getWilddogVideoToken() {
        return wilddogVideoToken;
    }

    public void setWilddogVideoToken(String wilddogVideoToken) {
        this.wilddogVideoToken = wilddogVideoToken;
    }


    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public boolean isOverdue() {
        final long maxAliveTime = 24 * 60 * 60;
        long currentTime = System.currentTimeMillis() / 1000;
        return (currentTime - last_login_time) > maxAliveTime;
    }

    @Override
    public String toString() {
        return "User{" +
                "user_id='" + user_id + '\'' +
                ", mobile='" + mobile + '\'' +
                ", nick_name='" + nick_name + '\'' +
                ", head_img_url='" + head_img_url + '\'' +
                ", sex='" + sex + '\'' +
                ", ref_user_id='" + ref_user_id + '\'' +
                ", login_count=" + login_count +
                ", last_login_time=" + last_login_time +
                ", create_time='" + create_time + '\'' +
                ", token='" + token + '\'' +
                ", agora_token='" + agora_token + '\'' +
                ", wilddog_token='" + wilddog_token + '\'' +
                ", isDoctor=" + isDoctor +
                ", wilddogVideoToken='" + wilddogVideoToken + '\'' +
                ", pwd='" + pwd + '\'' +
                '}';
    }

    //    private boolean isDoctor;
//    private String uid;
//    private String nick_name;
//    private String head_img_url;
//    private String wilddog_login_token;
//    private String token;
//    private Long last_login_time;
//    private String phone;
//    private String password;
//
//    public boolean isDoctor() {
//        return isDoctor;
//    }
//
//    public void setDoctor(boolean doctor) {
//        isDoctor = doctor;
//    }
//
//    public String getUid() {
//        return uid;
//    }
//
//    public String getNick_name() {
//        return nick_name;
//    }
//
//    public void setNick_name(String nick_name) {
//        this.nick_name = nick_name;
//    }
//
//    public String getHead_img_url() {
//        return head_img_url;
//    }
//
//    public void setHead_img_url(String head_img_url) {
//        this.head_img_url = head_img_url;
//    }
//
//    public void setUid(String uid) {
//        this.uid = uid;
//    }
//
//    public String getWilddog_login_token() {
//        return wilddog_login_token;
//    }
//
//    public void setWilddog_login_token(String wilddog_login_token) {
//        this.wilddog_login_token = wilddog_login_token;
//    }
//
//    public String getToken() {
//        return token;
//    }
//
//    public void setToken(String token) {
//        this.token = token;
//    }
//
//    public Long getLast_login_time() {
//        return last_login_time;
//    }
//
//    public void setLast_login_time(Long last_login_time) {
//        this.last_login_time = last_login_time;
//    }
//
//    public String getPhone() {
//        return phone;
//    }
//
//    public void setPhone(String phone) {
//        this.phone = phone;
//    }
//
//    public String getPassword() {
//        return password;
//    }
//
//    public void setPassword(String password) {
//        this.password = password;
//    }
//
//    public boolean isOverdue(){
//        final long maxAliveTime = 24 * 60 * 60;
//        long currentTime = System.currentTimeMillis() / 1000;
//        return (currentTime - last_login_time) > maxAliveTime;
//    }
//
//    @Override
//    public String toString() {
//        return "User{" +
//                "isDoctor=" + isDoctor +
//                ", uid='" + uid + '\'' +
//                ", wilddog_login_token='" + wilddog_login_token + '\'' +
//                ", token='" + token + '\'' +
//                ", last_login_time=" + last_login_time +
//                ", phone='" + phone + '\'' +
//                '}';
//    }
}
