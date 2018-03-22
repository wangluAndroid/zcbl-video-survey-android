package com.zcbl.client.zcbl_video_survey_library.ui.tx.bean;

/**
 * Created by serenitynanian on 2018/3/21.
 */

public class RoomState {
    public static final int Absent  = 0;            //会话不存在
    public static final int Empty   = 1;            //会话存在，但没有成员
    public static final int Created = 2;            //我创建了会话
    public static final int Jioned  = 4;            //我加入了会话
}
