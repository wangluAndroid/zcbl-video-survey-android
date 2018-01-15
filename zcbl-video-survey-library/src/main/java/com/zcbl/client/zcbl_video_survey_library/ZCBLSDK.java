package com.zcbl.client.zcbl_video_survey_library;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.wilddog.wilddogcore.WilddogApp;
import com.wilddog.wilddogcore.WilddogOptions;
import com.zcbl.client.zcbl_video_survey_library.bean.ZCBLVideoSurveyModel;

/**
 * Created by serenitynanian on 2018/1/12.
 */

public class ZCBLSDK {

    public static void init(Context context){
        // 初始化
        WilddogOptions syncOptions = new WilddogOptions.Builder().setSyncUrl("https://"+ ZCBLConstants.WILDDOG_APP_ID+".wilddogio.com").build();
        WilddogApp.initializeApp(context, syncOptions);
    }

    public static void goToVideoSurvey(Context context,ZCBLVideoSurveyModel zcblVideoSurveyModel){
        Intent intent = new Intent(context, ZCBLVideoSurveyConnectTransionActivity.class);
        intent.putExtra("zcbl_model", zcblVideoSurveyModel);
        context.startActivity(intent);
    }
}
