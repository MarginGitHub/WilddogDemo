package com.zd.wilddogdemo.utils;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.yalantis.ucrop.UCrop;
import com.zd.wilddogdemo.R;
import com.zd.wilddogdemo.beans.User;
import com.zd.wilddogdemo.storage.ObjectPreference;
import com.zd.wilddogdemo.storage.memory.ObjectProvider;
import com.zd.wilddogdemo.ui.LoginActivity;
import com.zd.wilddogdemo.ui.user.fragments.AboutMeFragment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static java.security.AccessController.getContext;


/**
 * Created by dongjijin on 2017/8/28 0028.
 */

public class Util {

    public static String md5(String param) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(param.getBytes());
            StringBuilder builder = new StringBuilder();
            for (byte b : md5.digest()) {
                if ((b & 0xFF) < 0x10)
                    builder.append("0");
                builder.append(Integer.toHexString(b & 0xFF));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
//    public static String sign(String ts, String apiKey, String mobile, String password, int flag) {
//        Map<String, String> data = new HashMap<>();
//        data.put("ts", ts);
//        data.put("apiKey", apiKey);
//        data.put("mobile", mobile);
//        data.put("password", password);
//        data.put("flag", flag + "");
//        List<Map.Entry<String, String>> list = new ArrayList<>(data.entrySet());
//        Collections.sort(list, new Comparator<Map.Entry<String, String>>() {
//            @Override
//            public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
//                return o1.getKey().compareTo(o2.getKey());
//            }
//        });
//        StringBuilder builder = new StringBuilder();
//        for(Map.Entry<String, String> entry : list) {
//            builder.append(entry.getValue());
//        }
//        return md5(builder.toString());
//    }

    public static String sign(Map<String, String> params) {
        List<Map.Entry<String, String>> list = new ArrayList<>(params.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, String>>() {
            @Override
            public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : list) {
            builder.append(entry.getValue());
        }
        return md5(builder.toString());
    }

    public static boolean isPhoneValid(String phone) {
        if (TextUtils.isEmpty(phone) || !TextUtils.isDigitsOnly(phone) || phone.length() != 11) {
            return false;
        }
        String regex = "^((13[0-9])|(14[5|7])|(15([0-3]|[5-9]))|(18[0,2,5-9]))\\d{8}$";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(phone).find();
    }

    public static boolean isPasswordValid(String password) {
        return !(TextUtils.isEmpty(password) || password.length() < 6);
    }

    public static void setImageView(Context context, ImageView imageView, Object loader, int placeholder, boolean circleCrop) {
        GlideRequest<Drawable> load = GlideApp.with(context).load(loader).placeholder(R.drawable.head);
        if (circleCrop) {
            load = load.circleCrop();
        }
        load.into(imageView);
    }

    public static void setAvatarView(Context context, ImageView imageView, Object loader) {
        GlideApp.with(context).load(loader).placeholder(R.drawable.head).circleCrop().into(imageView);
    }

    public static void setImageView(Activity activity, ImageView imageView, Object loader) {
        GlideApp.with(activity).load(loader).placeholder(R.drawable.head).circleCrop().into(imageView);
    }

    public static void setImageView(AboutMeFragment fragment, ImageView imageView, Object loader) {
        GlideApp.with(fragment).load(loader).placeholder(R.drawable.head).circleCrop().into(imageView);
    }

    public static void setImageView(View view, ImageView imageView, Object loader) {
        GlideApp.with(view).load(loader).placeholder(R.drawable.head).circleCrop().into(imageView);
    }

//

    public static File getHeadImgFile(String fileName) {
        // 首先保存图片
        File appDir = new File(Environment.getExternalStorageDirectory(), "wilddog");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        File file = new File(appDir, fileName);
        return file;
    }

    public static void saveUser(Context context, User user) {
//        内存中保存
        ObjectProvider.sharedInstance().set(user);
//        SharePreference中保存
        ObjectPreference.saveObject(context, user);
    }

    public static User getUser(Context context) {
//        内存中获取
        User user = ObjectProvider.sharedInstance().get(User.class);
        if (user == null) {
            return ObjectPreference.getObject(context, User.class);
        }
        return user;
    }

    public static void pushActivity(Activity activity) {
        ActivityStack activityStack = ObjectProvider.sharedInstance().get(ActivityStack.class);
        if (activityStack == null) {
            activityStack = new ActivityStack();
        }
        activityStack.add(activity);
    }

    public static void removeActivity(Activity activity) {
        ActivityStack activityStack = ObjectProvider.sharedInstance().get(ActivityStack.class);
        if (activityStack != null) {
            activityStack.remove(activity);
        }
    }

    public static void clearActivityStack() {
        ActivityStack activityStack = ObjectProvider.sharedInstance().get(ActivityStack.class);
        if (activityStack != null) {
            activityStack.clear();
        }
    }

    static class ActivityStack {
        List<Activity> mActivities;

        public ActivityStack() {
            mActivities = new ArrayList<>();
        }

        public void add(Activity activity) {
            mActivities.add(activity);
        }

        public void remove(Activity activity) {
            mActivities.remove(activity);
        }

        public void clear() {
            for (Activity a : mActivities) {
                a.finish();
            }
            mActivities.clear();
        }
    }


}
