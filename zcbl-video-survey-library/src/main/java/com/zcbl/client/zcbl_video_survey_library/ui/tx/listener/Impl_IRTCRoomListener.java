package com.zcbl.client.zcbl_video_survey_library.ui.tx.listener;

import android.util.Log;

import com.zcbl.client.zcbl_video_survey_library.ZCBLConstants;
import com.zcbl.client.zcbl_video_survey_library.ui.tx.bean.PusherInfo;

import java.util.List;

/**
 * Created by serenitynanian on 2018/3/22.
 */

public class Impl_IRTCRoomListener implements IRTCRoomListener {

    @Override
    public void onGetPusherList(List<PusherInfo> pusherInfoList) {

    }

    @Override
    public void onPusherJoin(PusherInfo pusherInfo) {
        Log.e(ZCBLConstants.TAG,String.format("[RTCRoom] onPusherJoin, UserID {%s} PlayUrl {%s}", pusherInfo.userID, pusherInfo.accelerateURL));
    }

    @Override
    public void onPusherQuit(PusherInfo pusherInfo) {
        Log.e(ZCBLConstants.TAG,String.format("[RTCRoom] onPusherQuit, UserID {%s} PlayUrl {%s}", pusherInfo.userID, pusherInfo.accelerateURL));
    }

    @Override
    public void onRoomClosed(String roomId) {
        Log.e(ZCBLConstants.TAG,String.format("[RTCRoom] onRoomClosed, RoomId {%s}", roomId));
    }

    @Override
    public void onDebugLog(String log) {
        Log.e(ZCBLConstants.TAG, "onDebugLog: ------>"+log);
    }

    @Override
    public void onRecvRoomTextMsg(String roomid, String userid, String userName, String userAvatar, String msg) {

    }

    @Override
    public void onError(int errorCode, String errorMessage) {

    }
}