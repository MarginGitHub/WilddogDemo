package com.zd.wilddogdemo.cons;

/**
 * Created by dongjijin on 2017/9/1 0001.
 */

public class ConversationCons {
//    拨号
    public static final int RING_UP = 0x1;
//    挂断
    public static final int HANG_UP = 0x2;
//    被叫
    public static final int CALLED = 0x3;
//    连接上
    public static final int CONNECTED = 0x4;
//    被接受
    public static final int ACCEPTED = 0x5;
//    被拒绝
    public static final int REJECTED = 0x6;
//    接受
    public static final int ACCEPT = 0x0a;
//    拒绝
    public static final int REJECT = 0x0b;
//    超时
    public static final int TIMEOUT = 0x7;
//    通话中
    public static final int BUSY = 0x8;
//    接收到视频流
    public static final int STREAM_RECEIVED = 0x9;
//    停止拨号
    public static final int CLOSE = 0x0c;
//    账号余额不足
    public static final int BALANCE_NOT_ENOUGH = 0x0d;
}
