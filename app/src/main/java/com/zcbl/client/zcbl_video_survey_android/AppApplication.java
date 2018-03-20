package com.zcbl.client.zcbl_video_survey_android;

import android.app.Application;

import com.zcbl.client.zcbl_video_survey_library.ZCBLSDK;

/**
 * Created by serenitynanian on 2018/1/12.
 */

public class AppApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ZCBLSDK.init(this);
        ZCBLSDK.setDedug(false);
    }
}
