package com.zcbl.client.zcbl_video_survey_library.ui.tx.listener;

import com.zcbl.client.zcbl_video_survey_library.ui.tx.bean.PusherInfo;
import com.zcbl.client.zcbl_video_survey_library.ui.tx.bean.RoomManager;

/**
 * Created by serenitynanian on 2018/3/23.
 */

public interface IVideoDisplayListener {

    void onDebugLog(String log);

    void receiveIMMessage(String string);

    void pushStreamSuccess();
    void pushStreamFailure(int code, String errorInfo);

    void exitRoomSuccess();
    void exitRoomFailure(int code, String errorInfo);

    void hangupSuccess();
    void hangupFailure(int code, String errorInfo);

    void takePicSuccess();
    void takePicFailure(int code, String errorInfo);

    void exitIMFailure(int code, String desc);
    void exitIMSuccess();

    void createRoomSuccess(RoomManager roomManager, String roomId);
    void createRoomFailure(int code ,String errorInfo);

    void sendMessageSuccess(String type);
    void sendMessageFailure(int code ,String errorInfo);

    void onPusherQuit(PusherInfo member);
    void onPusherJoin(PusherInfo member);

    void getRemoteStreamSuccess();
    void getRemoteStreamFailure(int code ,String errorInfo);
}
