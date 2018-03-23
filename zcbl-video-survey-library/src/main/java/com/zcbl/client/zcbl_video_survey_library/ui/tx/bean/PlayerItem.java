package com.zcbl.client.zcbl_video_survey_library.ui.tx.bean;

import com.tencent.rtmp.TXLivePlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;

/**
 * Created by serenitynanian on 2018/3/22.
 */

public class PlayerItem {

    public TXCloudVideoView view;
    public PusherInfo pusher;
    public TXLivePlayer player;

    public PlayerItem(TXCloudVideoView view, PusherInfo pusher, TXLivePlayer player) {
        this.view = view;
        this.pusher = pusher;
        this.player = player;
    }

    public void resume(){
        this.player.resume();
    }

    public void pause(){
        this.player.pause();
    }

    public void destroy(){
        this.player.stopPlay(true);
        this.view.onDestroy();
    }
}
