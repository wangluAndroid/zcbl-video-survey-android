package com.zcbl.client.zcbl_video_survey_library.ui.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.zcbl.client.zcbl_video_survey_library.ui.receiver.ZCBLPlayerManager;

/**
 * Created by serenitynanian on 2018/1/3.
 */

public class ZCBLHeadsetReceiver extends BroadcastReceiver {

    private static final String TAG = "ZCBLHeadsetReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        ZCBLPlayerManager zcblPlayerManager = ZCBLPlayerManager.getInstants(context);
        switch (action){
            //插入和拔出耳机会触发此广播
            case Intent.ACTION_HEADSET_PLUG:
                int state = intent.getIntExtra("state", 0);
                if (state == 1){
                    Log.w(TAG,"------ZCBLHeadsetReceiver--------耳机插入-----------");
                    zcblPlayerManager.changeToHeadset();
                } else if (state == 0){
                    //如果没有插入耳机  每次进来 都先执行
                    Log.w(TAG,"------ZCBLHeadsetReceiver-------耳机拔出-----------");
                    zcblPlayerManager.changeToSpeaker();
                }
                break;
            default:
                break;
        }
    }
}
