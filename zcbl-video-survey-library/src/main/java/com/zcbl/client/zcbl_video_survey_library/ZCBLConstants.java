package com.zcbl.client.zcbl_video_survey_library;

import android.Manifest;

/**
 * Created by serenitynanian on 2017/12/15.
 */

public class ZCBLConstants {

    public static final String TAG = "ZCBL";


    public static final int GO_TO_VIDEO_ROOM = 1;
    public static final int VIDEO_SURVEY_IS_OVER = 2;

    // 所需的全部权限
    public static final String[] PERMISSIONS = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
    };

    public static String BASE_URL ="https://chakan.zhongchebaolian.com/";//生产环境
//    public static String BASE_URL ="https://survey.zhongchebaolian.com/";//测试环境

    public static final String UPLOAD_IMAGE_URL = "boot-zcbl-survey-api/v1/upload/photo";
    public static final String VIDEO_CONNECTION_URL = "boot-zcbl-survey-api/survey/v1/video/connect";

//    public static final String DOMAIN = "https://jiw5ccnh.qcloud.la/weapp/double_room";
    public static final String DOMAIN = "https://lvb.qcloud.com/weapp/double_room";

}
