package com.zcbl.client.zcbl_video_survey_library;

import android.Manifest;

/**
 * Created by serenitynanian on 2017/12/15.
 */

public class Constants {

    public static final String TAG = "ZCBL";

    public static final int GO_TO_CONNECTION = 0;
    public static final int REFUSE_CONNECTION = 1;
    public static final int GO_TO_VIDEO_ROOM = 2;
    public static final int SURVEY_IS_OVER = 3;
    public static final int DISSMISS_DIALOG = 4;
    public static final int PERMISSIONS_REFUSE = 5;
    public static final int REFRESH_IMAGE_LIST = 6;

    // 所需的全部权限
    public static final String[] PERMISSIONS = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
    };

//    public static final String BASE_URL ="https://chakan.zhongchebaolian.com/";//生产环境
    public static final String BASE_URL ="https://survey.zhongchebaolian.com/";//测试环境

    public static final String UPLOAD_IMAGE_URL = BASE_URL+"boot-zcbl-survey-api/v1/upload/photo";
    public static final String VIDEO_CONNECTION_URL = BASE_URL+"boot-zcbl-survey-api/survey/v1/video/connect";
    public static final String WILDDOG_VIDEO_ID = "wd6476157034byycgg";
    public static final String WILDDOG_APP_ID = "wd7055430119ruyynm";
}
