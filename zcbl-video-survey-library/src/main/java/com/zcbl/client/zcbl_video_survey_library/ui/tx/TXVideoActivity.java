package com.zcbl.client.zcbl_video_survey_library.ui.tx;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.zcbl.client.zcbl_video_survey_library.R;
import com.zcbl.client.zcbl_video_survey_library.ZCBLConstants;
import com.zcbl.client.zcbl_video_survey_library.ui.tx.bean.LoginInfoResponse;
import com.zcbl.client.zcbl_video_survey_library.ui.tx.bean.PusherInfo;
import com.zcbl.client.zcbl_video_survey_library.ui.tx.bean.SelfAccountInfo;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by serenitynanian on 2018/3/20.
 */

public class TXVideoActivity extends AppCompatActivity implements View.OnClickListener,RTCDoubleRoomActivityInterface {
    public final static String DOMAIN = "https://jiw5ccnh.qcloud.la/weapp/double_room";   //测试环境 https://drourwkp.qcloud.la
    private String userName = "中车宝联";
    private String avatarUrl = "avatar";
    private String userId;
    private TextView iv_goback;
    private ImageView iv_light;
    private ImageView iv_switch_camera;
    private ImageView iv_takepic;
    private ImageView iv_layer;
    private ProgressBar progressbar;
    private RelativeLayout rootView;
    private RTCRoom rtcRoom;
    private TXCloudVideoView rtmproom_video_local;
    private TXCloudVideoView rtmproom_video_remote;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tx_video);
        initView();
        initData();
        initRoom();
    }

    //初始化房间--获取用户登录信息
    private void initRoom() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .build();

        final MediaType MEDIA_JSON = MediaType.parse("application/json; charset=utf-8");

        final Request request = new Request.Builder()
                .url(DOMAIN.concat("/get_im_login_info"))//合并多个数组；合并多个字符串
                .post(RequestBody.create(MEDIA_JSON, "{\"userIDPrefix\":\"android\"}"))
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setTitle("获取登录信息失败，点击重试");
                        Log.w(ZCBLConstants.TAG,"获取登录信息失败，点击重试");
                        //失败后点击Title可以重试
//                        setRetryRunnable(new Runnable() {
//                            @Override
//                            public void run() {
//                                Toast.makeText(RTCDoubleRoomActivity.this, "重试...", Toast.LENGTH_SHORT).show();
//                                initRoom();
//                            }
//                        });
                        Log.w(ZCBLConstants.TAG,String.format("[Activity]获取登录信息失败{%s}", e.getMessage()));
                    }
                });
            }

            @Override
            public void onResponse(final Call call, okhttp3.Response response) throws IOException {
                String body = response.body().string();
                Gson gson = new Gson();
                try {
                    LoginInfoResponse resp = gson.fromJson(body, LoginInfoResponse.class);
                    if (resp.code != 0){
                        Log.w(ZCBLConstants.TAG,"获取登录信息失败，点击重试");
                        Log.w(ZCBLConstants.TAG,String.format("[Activity]获取登录信息失败：{%s}", resp.message));
                    }else {
                        final SelfAccountInfo selfAccountInfo = new SelfAccountInfo(
                                resp.userID,
                                userName,
                                avatarUrl,
                                resp.userSig,
                                resp.accType,
                                resp.sdkAppID);
                        Log.w(ZCBLConstants.TAG, "onResponse: "+selfAccountInfo.toString());

                        doInit(selfAccountInfo);

                    }
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }
            }
        });


    }

    //初始化IM
    private void doInit(SelfAccountInfo selfAccountInfo) {

        rtcRoom.init(DOMAIN,selfAccountInfo,rtmproom_video_local, new RTCRoom.InitCallback() {
            @Override
            public void onError(int errCode, String errInfo) {

            }

            @Override
            public void onSuccess(String userId) {
                TXVideoActivity.this.userId = userId ;
                //IM初始化成功
            }
        });




    }

    private void initData() {
        //初始化RCTRoom
        rtcRoom = new RTCRoom(this);
        rtcRoom.setRTCRoomListener(new MemberEventListener());
    }



    private void initView() {

        iv_goback = (TextView) findViewById(R.id.iv_goback);
        iv_goback.setOnClickListener(this);
        iv_light = (ImageView) findViewById(R.id.iv_light);
        iv_light.setOnClickListener(this);
        iv_switch_camera = (ImageView) findViewById(R.id.iv_switch_camera);
        iv_switch_camera.setOnClickListener(this);
        iv_takepic = (ImageView) findViewById(R.id.iv_takepic);
        iv_takepic.setOnClickListener(this);

        progressbar = (ProgressBar) findViewById(R.id.progressbar);

        rootView = (RelativeLayout) findViewById(R.id.rootView);

        iv_layer = (ImageView) findViewById(R.id.iv_layer);
        iv_layer.setOnClickListener(this);

        rtmproom_video_local = (TXCloudVideoView) findViewById(R.id.rtmproom_video_local);
        rtmproom_video_remote = (TXCloudVideoView) findViewById(R.id.rtmproom_video_remote);

    }

    @Override
    public void onClick(View v) {

    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        rtcRoom.setRTCRoomListener(null);
        rtcRoom.unInit();

    }

    @Override
    public RTCRoom getRTCRoom() {
        return rtcRoom;
    }

    @Override
    public String getSelfUserID() {
        return userId;
    }

    @Override
    public String getSelfUserName() {
        return userName;
    }

    @Override
    public void showGlobalLog(boolean enable) {

    }

    @Override
    public void printGlobalLog(String format, Object... args) {

    }

    private final class MemberEventListener implements IRTCRoomListener{

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
}
