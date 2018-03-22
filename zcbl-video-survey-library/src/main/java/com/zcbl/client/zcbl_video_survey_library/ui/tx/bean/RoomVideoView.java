package com.zcbl.client.zcbl_video_survey_library.ui.tx.bean;

import android.view.View;
import android.widget.TextView;

import com.tencent.rtmp.ui.TXCloudVideoView;

/**
 * Created by serenitynanian on 2018/3/20.
 */

public class RoomVideoView {

    TXCloudVideoView view;
    boolean isUsed;
    String userID;
    String name = "";


    public RoomVideoView(TXCloudVideoView view, TextView titleView, String userID) {
        this.view = view;
        titleView.setText("");
        view.setVisibility(View.GONE);
        this.isUsed = false;
        this.userID = userID;
    }

    private void setUsed(boolean set){
        view.setVisibility(set ? View.VISIBLE : View.GONE);
        this.isUsed = set;
    }
}
