package com.zcbl.client.zcbl_video_survey_library.utils;


import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by serenity on 16-12-20.
 */

public class ZCBLPermissionHelper {



    public static final int PERMISSIONS_GRANTED = 0; // 权限授权
    public static final int PERMISSIONS_DENIED = 1; // 权限拒绝

    public static final int PERMISSION_REQUEST_CODE = 0; // 系统权限管理页面的参数
    public static final String EXTRA_PERMISSIONS = "com.zcbl.permission.extra_permission"; // 权限参数
    private final Context mContext;
    private final Activity mActivity;

    private final static int START_VIDEO = 0 ;


    public ZCBLPermissionHelper(Activity activity) {
        mActivity=activity;
        mContext = activity.getApplicationContext();
    }

    // 判断权限集合
    public boolean lacksPermissions(String... permissions) {
        for (String permission : permissions) {
            if (lacksPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    // 判断是否缺少权限
    private boolean lacksPermission(String permission) {
        return ContextCompat.checkSelfPermission(mContext, permission) ==
                PackageManager.PERMISSION_DENIED;
    }

    // 请求权限兼容低版本
    public void requestPermissions(String... permissions) {
        ActivityCompat.requestPermissions(mActivity, permissions, PERMISSION_REQUEST_CODE);
    }
}
