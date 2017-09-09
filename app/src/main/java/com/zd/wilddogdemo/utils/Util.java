package com.zd.wilddogdemo.utils;


import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.zd.wilddogdemo.R;
import com.zd.wilddogdemo.ui.fragment.AboutMeFragment;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static android.R.attr.data;


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
        for(Map.Entry<String, String> entry : list) {
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

    public static void setImageView(Context context, ImageView imageView, Object loader) {
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

}
