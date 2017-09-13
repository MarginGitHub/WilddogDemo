package com.zd.wilddogdemo.beans;

import java.io.Serializable;

/**
 * Created by dongjijin on 2017/9/12 0012.
 */

public class DialInfo implements Serializable {
    Doctor mDoctor;
    int maxConversationTime;

    public DialInfo(Doctor doctor) {
        mDoctor = doctor;
    }

    public Doctor getDoctor() {
        return mDoctor;
    }

    public void setDoctor(Doctor doctor) {
        mDoctor = doctor;
    }

    public int getMaxConversationTime() {
        return maxConversationTime;
    }

    public void setMaxConversationTime(int maxConversationTime) {
        this.maxConversationTime = maxConversationTime;
    }
}
