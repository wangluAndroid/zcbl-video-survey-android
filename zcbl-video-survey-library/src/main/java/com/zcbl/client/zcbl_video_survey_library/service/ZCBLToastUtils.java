package com.zcbl.client.zcbl_video_survey_library.service;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by serenitynanian on 2018/1/12.
 */

public class ZCBLToastUtils {

    public static void showToast(Context context, String desc) {
        Toast.makeText(context, ""+desc, Toast.LENGTH_SHORT).show();
    }
}
