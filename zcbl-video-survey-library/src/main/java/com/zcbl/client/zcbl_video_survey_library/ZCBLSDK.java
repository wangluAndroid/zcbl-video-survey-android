package com.zcbl.client.zcbl_video_survey_library;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;

import com.tencent.imsdk.TIMLogLevel;
import com.tencent.imsdk.TIMManager;
import com.tencent.imsdk.TIMSdkConfig;
import com.tencent.rtmp.TXLiveBase;
import com.zcbl.client.zcbl_video_survey_library.bean.ZCBLRequireParamsModel;
import com.zcbl.client.zcbl_video_survey_library.bean.ZCBLVideoSurveyModel;
import com.zcbl.client.zcbl_video_survey_library.ui.activity.ZCBLVideoSurveyConnectTransionActivity;
import com.zcbl.client.zcbl_video_survey_library.utils.ZCBLCheckUtils;

/**
 * Created by serenitynanian on 2018/1/12.
 * 中车初始化sdk
 */

public class ZCBLSDK {

    private static Context instance;

    public static void init(Context context){
        instance = context ;
        TXLiveBase.setConsoleEnabled(BuildConfig.DEBUG);

//        //初始化SDK基本配置   1400066983
//        TIMSdkConfig config = new TIMSdkConfig(1400066983)
//                .setLogLevel(TIMLogLevel.DEBUG)
//                .enableLogPrint(BuildConfig.DEBUG)
//                .setLogPath(Environment.getExternalStorageDirectory().getPath() + "/justfortest/");
//        //初始化SDK
//        TIMManager.getInstance().init(context, config);
    }

    public static void goToVideoSurvey(Context context,ZCBLRequireParamsModel params){
        ZCBLVideoSurveyModel model = ZCBLCheckUtils.checkParams(context,params);
        if(null != model ){
            Intent intent = new Intent(context, ZCBLVideoSurveyConnectTransionActivity.class);
            intent.putExtra("zcbl_model", model);
            context.startActivity(intent);
        }
    }

    public static Context getInstance() {
        return instance ;
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
