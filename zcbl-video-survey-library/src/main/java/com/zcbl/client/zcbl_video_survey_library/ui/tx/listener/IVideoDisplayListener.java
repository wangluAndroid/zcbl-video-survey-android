package com.zcbl.client.zcbl_video_survey_library.ui.tx.listener;

/**
 * Created by serenitynanian on 2018/3/23.
 */

public interface IVideoDisplayListener {

    void onDebugLog(String log);


    void pushStreamSuccess();
    void pushStreamFailure(int code, String errorInfo);

    void exitRoomSuccess();
    void exitRoomFailure(int code, String errorInfo);

    void exitIMFailure(int code, String desc);
}
