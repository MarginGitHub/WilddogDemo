package com.zd.wilddogdemo.beans;

/**
 * Created by dongjijin on 2017/9/15 0015.
 */

public class VideoCallInfo {

    /**
     * video_call_id : 39E2D648EF224E918E76874227482D96
     * start : 1505445415
     * duration : 100
     * price : 20
     * amount : 20
     * nick_name : 我是医生
     * head_img_url : 2017-09-14/59b98ec77ab51.png
     */

    private String video_call_id;
    private long start;
    private long duration;
    private double price;
    private double amount;
    private String nick_name;
    private String head_img_url;

    public String getVideo_call_id() {
        return video_call_id;
    }

    public void setVideo_call_id(String video_call_id) {
        this.video_call_id = video_call_id;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
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
}
