package com.zcbl.client.zcbl_video_survey_library;

import android.content.Context;
import android.content.Intent;
import com.wilddog.wilddogcore.WilddogApp;
import com.wilddog.wilddogcore.WilddogOptions;
import com.zcbl.client.zcbl_video_survey_library.bean.ZCBLRequireParamsModel;
import com.zcbl.client.zcbl_video_survey_library.bean.ZCBLVideoSurveyModel;
import com.zcbl.client.zcbl_video_survey_library.ui.activity.ZCBLVideoSurveyConnectTransionActivity;
import com.zcbl.client.zcbl_video_survey_library.utils.ZCBLCheckUtils;
import com.zcbl.client.zcbl_video_survey_library.zcbl_native.NativeData;

/**
 * Created by serenitynanian on 2018/1/12.
 * 中车初始化sdk
 */

public class ZCBLSDK {

    public static void init(Context context){
        // 初始化
        WilddogOptions syncOptions = new WilddogOptions.Builder().setSyncUrl("https://"+ NativeData.getAppId()+".wilddogio.com").build();
        WilddogApp.initializeApp(context, syncOptions);
    }

    public static void goToVideoSurvey(Context context,ZCBLRequireParamsModel params){
        ZCBLVideoSurveyModel model = ZCBLCheckUtils.checkParams(context,params);
        if(null != model ){
            Intent intent = new Intent(context, ZCBLVideoSurveyConnectTransionActivity.class);
            intent.putExtra("zcbl_model", model);
            context.startActivity(intent);
        }
    }

    /**
     * 开启debug 切换请求环境
     * @param isOpenDebug
     */
    public static void setDedug(boolean isOpenDebug){
        if(isOpenDebug){
            ZCBLConstants.BASE_URL = "https://survey.zhongchebaolian.com/";
        }
    }
}
