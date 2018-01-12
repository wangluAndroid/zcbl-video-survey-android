package com.zcbl.client.zcbl_video_survey_library.ui.receiver;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


/**
 * Created by serenitynanian on 2018/1/3.
 */

public class BluetoothConnectionReceiver extends BroadcastReceiver {
    private static final String TAG = "BluetoothReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        PlayerManager playerManager = PlayerManager.getInstants(context);
        if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(intent.getAction())) {//蓝牙连接状态
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, -1);
            if (state == BluetoothAdapter.STATE_CONNECTED) {
                //连接或失联，切换音频输出（到蓝牙、或者强制仍然扬声器外放）
                //蓝牙连接成功回调此处的回调，如果一开始就已经连接成功了 不会主动回调此接口
                Log.w(TAG, "----------蓝牙连接了----STATE_CONNECTED-----");
                playerManager.changeToBluetooth();

            }else if(state == BluetoothAdapter.STATE_CONNECTING){
                Log.w(TAG,"----------蓝牙连接了----STATE_CONNECTING-----");

            } else if (state == BluetoothAdapter.STATE_DISCONNECTING) {
                Log.w(TAG,"----------蓝牙断开----STATE_DISCONNECTING-----");

            } else if (state == BluetoothAdapter.STATE_DISCONNECTED) {
                //主动从蓝牙设置界面断开连接的蓝牙才会回调此处
                Log.w(TAG,"----------蓝牙断开----STATE_DISCONNECTED-----");
                playerManager.changeToSpeaker();
            }
        } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {//本地蓝牙打开或关闭
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
            //直接开启和关闭蓝牙开关回调此处，不会回调上面的BluetoothAdapter.STATE_DISCONNECTING和STATE_DISCONNECTED
            if (state == BluetoothAdapter.STATE_TURNING_OFF) {
                //断开，切换音频输出
                Log.w(TAG, "------蓝牙连接状态改变了-----STATE_TURNING_OFF----");
            }else if(state == BluetoothAdapter.STATE_OFF){
                Log.w(TAG, "------蓝牙连接状态改变了-----STATE_OFF----");
                playerManager.changeToSpeaker();
            } else if (state == BluetoothAdapter.STATE_ON) {
                Log.w(TAG, "------蓝牙连接状态改变了-----STATE_ON----");
            }
        }
    }
}
