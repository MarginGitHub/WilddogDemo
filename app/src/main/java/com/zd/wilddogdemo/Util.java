package com.zd.wilddogdemo;

import com.wilddog.client.WilddogSync;
import com.wilddog.wilddogauth.WilddogAuth;


/**
 * Created by dongjijin on 2017/8/28 0028.
 */

public class Util {
    public static void logout() {
        WilddogAuth auth = WilddogAuth.getInstance();
        WilddogSync.getInstance().getReference("user").child(auth.getCurrentUser().getUid()).removeValue();
//        WilddogSync.goOffline();
        auth.signOut();
    }
}
