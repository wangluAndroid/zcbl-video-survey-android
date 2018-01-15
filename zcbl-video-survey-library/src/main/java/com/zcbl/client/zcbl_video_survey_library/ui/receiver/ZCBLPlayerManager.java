package com.zcbl.client.zcbl_video_survey_library.ui.receiver;

import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.util.Log;

import com.zcbl.client.zcbl_video_survey_library.ZCBLConstants;

/**
 * Created by serenitynanian on 2018/1/3.
 */

public class ZCBLPlayerManager {

    private final AudioManager audioManager;

    private ZCBLPlayerManager(Context context){
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    public static ZCBLPlayerManager getInstants(Context context){
        return new ZCBLPlayerManager(context);
    }

    /**
     * 切换到外放
     */
    public void changeToSpeaker(){
        //切换到外放时  先检测下 是否已经连接到蓝牙 ，如果连接到蓝牙 使用蓝牙播放
        Log.i(ZCBLConstants.TAG,"---------changeToSpeaker-------->"+audioManager.isBluetoothA2dpOn());
        if (audioManager.isBluetoothA2dpOn()) {
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            audioManager.startBluetoothSco();
            audioManager.setBluetoothScoOn(true);
            audioManager.setSpeakerphoneOn(false);
        }else{
            audioManager.setMode(AudioManager.MODE_NORMAL);
            audioManager.setSpeakerphoneOn(true);
            audioManager.stopBluetoothSco();
            audioManager.setBluetoothScoOn(false);
        }
    }

    /**
     * 切换到蓝牙
     */
    public void changeToBluetooth(){
        Log.i(ZCBLConstants.TAG,"-------------》"+audioManager.isWiredHeadsetOn());
        Log.i(ZCBLConstants.TAG,"-------------》"+audioManager.isBluetoothA2dpOn());
        if (!audioManager.isWiredHeadsetOn()) {
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            audioManager.startBluetoothSco();
            audioManager.setBluetoothScoOn(true);
            audioManager.setSpeakerphoneOn(false);
        }
    }

    /**
     * 切换到耳机模式
     */
    public void changeToHeadset(){
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        audioManager.stopBluetoothSco();
        audioManager.setBluetoothScoOn(false);
        audioManager.setSpeakerphoneOn(false);
    }

    /**
     * 切换到听筒
     */
    public void changeToReceiver(){
        audioManager.setSpeakerphoneOn(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        } else {
            audioManager.setMode(AudioManager.MODE_IN_CALL);
        }
    }

}
