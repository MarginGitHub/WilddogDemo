package com.zd.wilddogdemo.broadreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.zd.wilddogdemo.ui.KeppAliveActivity;

/**
 * Created by dongjijin on 2017/9/1 0001.
 */

public class KeepAliveBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d("BroadcastReceiver", "action: " + action);
        if (action.equals(Intent.ACTION_SCREEN_OFF)) {
            Intent aliveIntent = new Intent(context, KeppAliveActivity.class);
            aliveIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(aliveIntent);
        } else if (action.equals(Intent.ACTION_SCREEN_ON)) {
            context.sendBroadcast(new Intent("com.zd.keepalive.finish"));
            Intent main = new Intent(Intent.ACTION_MAIN);
            main.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            main.addCategory(Intent.CATEGORY_HOME);
            context.startActivity(main);
        }
    }
}
