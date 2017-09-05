package com.zd.wilddogdemo.beans;

import java.io.Serializable;

/**
 * Created by dongjijin on 2017/9/5 0005.
 */

public class Doctor implements Serializable{

    /**
     * user_id : D27F4C11CBAE480A8F36EC739F190260
     * nick_name : 张医生
     * sex : 0
     * ref_user_id : 10000000000000000000000000000001
     * ad_url : 2017-08-11/598d7759c8c63.png
     * video_price : 20
     * video_count : 0
     * follow_count : 0
     */

    private String user_id;
    private String nick_name;
    private String sex;
    private String ref_user_id;
    private String ad_url;
    private String video_price;
    private String video_count;
    private String follow_count;

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getNick_name() {
        return nick_name;
    }

    public void setNick_name(String nick_name) {
        this.nick_name = nick_name;
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

    public String getAd_url() {
        return ad_url;
    }

    public void setAd_url(String ad_url) {
        this.ad_url = ad_url;
    }

    public String getVideo_price() {
        return video_price;
    }

    public void setVideo_price(String video_price) {
        this.video_price = video_price;
    }

    public String getVideo_count() {
        return video_count;
    }

    public void setVideo_count(String video_count) {
        this.video_count = video_count;
    }

    public String getFollow_count() {
        return follow_count;
    }

    public void setFollow_count(String follow_count) {
        this.follow_count = follow_count;
    }

    @Override
    public String toString() {
        return "Doctor{" +
                "user_id='" + user_id + '\'' +
                ", nick_name='" + nick_name + '\'' +
                ", sex='" + sex + '\'' +
                ", ref_user_id='" + ref_user_id + '\'' +
                ", ad_url='" + ad_url + '\'' +
                ", video_price='" + video_price + '\'' +
                ", video_count='" + video_count + '\'' +
                ", follow_count='" + follow_count + '\'' +
                '}';
    }
}
