package com.zcbl.client.zcbl_video_survey_library.ui.tx;

/**
 * Created by serenitynanian on 2018/3/20.
 */

public interface RTCDoubleRoomActivityInterface {

    RTCRoom getRTCRoom();
    String getSelfUserID();
    String getSelfUserName();
    void showGlobalLog(boolean enable);
    void printGlobalLog(String format, Object... args);
}
