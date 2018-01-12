package com.zcbl.client.zcbl_video_survey_library;

import android.content.Context;

import com.wilddog.wilddogcore.WilddogApp;
import com.wilddog.wilddogcore.WilddogOptions;

/**
 * Created by serenitynanian on 2018/1/12.
 */

public class ZCBLSDK {

    public static void init(Context context){
        // 初始化
        WilddogOptions syncOptions = new WilddogOptions.Builder().setSyncUrl("https://"+Constants.WILDDOG_APP_ID+".wilddogio.com").build();
        WilddogApp.initializeApp(context, syncOptions);
    }
}
