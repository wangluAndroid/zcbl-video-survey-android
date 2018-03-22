package com.zcbl.client.zcbl_video_survey_library.ui.tx.http;


import com.zcbl.client.zcbl_video_survey_library.ui.tx.bean.PusherInfo;
import com.zcbl.client.zcbl_video_survey_library.ui.tx.bean.RoomInfo;

import java.util.List;

/**
 * Created by jac on 2017/10/30.
 */

public class HttpResponse {
    public int code;

    public String message;

    public transient static int CODE_OK = 0;

    public static class LoginInfo extends HttpResponse {
        public String userID;
        public int sdkAppID;
        public String accType;
        public String userSig;
    }

    public static class RoomList extends HttpResponse {
        public List<RoomInfo> rooms;
    }

    public static class PusherList extends HttpResponse {
        public String roomID;
        public String roomName;
        public String roomCreator;
        public String mixedPlayURL;
        public List<PusherInfo> pushers;
    }

    public static class CreateRoom extends HttpResponse {
        public String roomID;
    }

    public static class PushUrl extends HttpResponse {
        public String pushURL;
    }

}
