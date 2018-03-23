package com.zcbl.client.zcbl_video_survey_library.ui.tx.listener;

import com.zcbl.client.zcbl_video_survey_library.ui.tx.bean.RoomManager;
import com.zcbl.client.zcbl_video_survey_library.ui.tx.bean.SelfAccountInfo;

/**
 * Created by serenitynanian on 2018/3/23.
 */

public interface ILoginInitialListener {
    void getIMLoginInfoSuccess(SelfAccountInfo selfAccountInfo);
    void getIMLoginInfoFailure(int code ,String errorInfo);

    void iMLoginSuccess();
    void iMLoginFailure(int code ,String errorInfo);

    void getPushUrlSuccess(String pushUrl);
    void getPushUrlFailure(int code ,String errorInfo);

    void createRoomSuccess(RoomManager roomManager,String roomId);
    void createRoomFailure(int code ,String errorInfo);


    void onDebugLog(String desc);

}
