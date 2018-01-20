package com.zcbl.client.zcbl_video_survey_library.zcbl_native;

/**
 * Created by serenitynanian on 2018/1/20.
 */

public class NativeData {

    static {
        System.loadLibrary("ZCBLData");
    }

    public static native String getAppId();

    public static native String getVideoKey();
}
