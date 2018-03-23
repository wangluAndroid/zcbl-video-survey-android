package com.zcbl.client.zcbl_video_survey_library.ui.activity;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.tencent.rtmp.TXLivePusher;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.zcbl.client.zcbl_video_survey_library.R;
import com.zcbl.client.zcbl_video_survey_library.ZCBLConstants;
import com.zcbl.client.zcbl_video_survey_library.bean.ZCBLVideoSurveyModel;
import com.zcbl.client.zcbl_video_survey_library.service.UpdateCallbackInterface;
import com.zcbl.client.zcbl_video_survey_library.service.ZCBLHttpUtils;
import com.zcbl.client.zcbl_video_survey_library.ui.receiver.ZCBLBluetoothConnectionReceiver;
import com.zcbl.client.zcbl_video_survey_library.ui.receiver.ZCBLHeadsetReceiver;
import com.zcbl.client.zcbl_video_survey_library.ui.tx.IReceiveIMListener;
import com.zcbl.client.zcbl_video_survey_library.ui.tx.Impl_IRTCRoomListener;
import com.zcbl.client.zcbl_video_survey_library.ui.tx.RTCRoom;
import com.zcbl.client.zcbl_video_survey_library.ui.tx.bean.LoginInfoResponse;
import com.zcbl.client.zcbl_video_survey_library.ui.tx.bean.SelfAccountInfo;
import com.zcbl.client.zcbl_video_survey_library.utils.ZCBLBase64Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import static com.zcbl.client.zcbl_video_survey_library.ZCBLConstants.VIDEO_SURVEY_IS_OVER;


/**
 * Created by serenitynanian on 2017/12/15.
 * 1.所有web端关闭坐席，App端只要回调远端流退出回调，app必须退出房间，返回到进入界面
 * 2.所有的断开只返回到进入界面
 */

public class ZCBLVideoSurveyActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener,IReceiveIMListener {
    public final static String DOMAIN = "https://jiw5ccnh.qcloud.la/weapp/double_room";   //测试环境 https://drourwkp.qcloud.la
    private String userName = "王雨露";
    private String avatarUrl = "avatar";

    public final Handler uiHandler = new Handler();
    private boolean isAudioEnable = true;
    private boolean isVideoEnable = true;
    private TextView iv_goback;
    private ImageView iv_light;
    private ImageView iv_switch_camera;
    private ImageView iv_takepic;
    private ProgressBar progressbar;
    private boolean canTakePic = true ;
    private ZCBLVideoSurveyModel zcblVideoSurveyModel;

    private boolean isLightOn = false ;
    private ImageView iv_switch_audio;
    private ZCBLHeadsetReceiver zcblHeadsetReceiver;
    private ZCBLBluetoothConnectionReceiver blueAudioNoisyReceiver;
    private ImageView iv_layer;
    private boolean isRemoteAttach = false ;

    private RelativeLayout rootView;
    private int lastX ;
    private int lastY ;
    private int screenWidth  ;
    private int screenHeight ;
    private int left;
    private int top;
    private int right;
    private int bottom;

    private TXCloudVideoView remote_video_view ;
    private TXCloudVideoView local_video_view;
    private RTCRoom rtcRoom;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_wilddog_video);
        zcblVideoSurveyModel = (ZCBLVideoSurveyModel) getIntent().getSerializableExtra("ZCBLVideoSurveyModel");
        registerBroadcast();
        initView();
        initRTCRoom();
        getImLoginInfo();

        Log.e(ZCBLConstants.TAG, "onCreate: "+Thread.currentThread());
    }

    @Override
    public void receiveIMMessage(String type) {
        if ("WEB$$takePic".equals(type)) {
            remoteTakePic();
        } else if ("WEB$$openLight".equals(type)) {
            controlCameraLight();
        } else if ("WEB$$closeLight".equals(type)) {
            controlCameraLight();
        } else if ("WEB$$surveyIsOver".equals(type)) {
            ZCBLVideoSurveyActivity.this.setResult(VIDEO_SURVEY_IS_OVER);
            ZCBLVideoSurveyActivity.this.finish();
        } else if ("WEB$$takePic0".equals(type)) {
            //蒙版--45度角
            iv_layer.setImageResource(R.drawable.ic_layer_45);
            iv_layer.setVisibility(View.VISIBLE);
        } else if ("WEB$$takePic1".equals(type)) {
            //蒙版--车架号
            iv_layer.setImageResource(R.drawable.ic_layer_chejiahao);
            iv_layer.setVisibility(View.VISIBLE);
        } else if ("WEB$$takePic2".equals(type)) {
            //蒙版--驾驶证
            iv_layer.setImageResource(R.drawable.ic_layer_license);
            iv_layer.setVisibility(View.VISIBLE);
        } else if ("WEB$$openRemoteWindow".equals(type)) {
            remote_video_view.setVisibility(View.VISIBLE);
        } else if ("WEB$$closeRemoteWindow".equals(type)) {
            remote_video_view.setVisibility(View.INVISIBLE);
        }
    }

    private void remoteTakePic() {
        TXLivePusher livePusher = rtcRoom.getLivePusher();
        if (null != livePusher) {
            livePusher.snapshot(new TXLivePusher.ITXSnapshotListener() {
                @Override
                public void onSnapshot(Bitmap bitmap) {
                    if (null != bitmap) {
                        uploadImage(bitmap);
                    }
                }
            });
        }
    }

    private void registerBroadcast() {

        //动态注册耳机插入广播
//        zcblHeadsetReceiver = new ZCBLHeadsetReceiver();
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction("android.intent.action.HEADSET_PLUG");
//        this.registerReceiver(zcblHeadsetReceiver, intentFilter);
//
//        //动态注册蓝牙广播
//        blueAudioNoisyReceiver = new ZCBLBluetoothConnectionReceiver();
//        //蓝牙状态广播监听
//        IntentFilter audioFilter = new IntentFilter();
//        audioFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);//蓝牙设备连接或断开
//        audioFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);//本机开启、关闭蓝牙开关
//        this.registerReceiver(blueAudioNoisyReceiver, audioFilter);

    }

    private void uploadImage(Bitmap bitmap) {
        canTakePic = false ;
        progressbar.setVisibility(View.VISIBLE);
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);

                baos.flush();
                baos.close();

                byte[] bitmapBytes = baos.toByteArray();
                String result = ZCBLBase64Utils.encodeToString(bitmapBytes, true);
                if (TextUtils.isEmpty(result)) {
                    Toast.makeText(ZCBLVideoSurveyActivity.this,"上传图片失败，请重试",Toast.LENGTH_SHORT).show();
                    return ;
                }
                JSONObject json = new JSONObject();
                try {
                    json.put("photoContent", result);
                    json.put("longitude", zcblVideoSurveyModel.getLongitude());
                    json.put("latitude", zcblVideoSurveyModel.getLatitude());
                    json.put("shotLocation", zcblVideoSurveyModel.getCaseAddress());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                ZCBLHttpUtils.getInstance().post(ZCBLConstants.UPLOAD_IMAGE_URL,json,new UpdateCallbackInterface() {
                    @Override
                    public void onError(String error) {
                        ZCBLVideoSurveyActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressbar.setVisibility(View.GONE);
                                canTakePic = true ;
                                String tempStr = "APP$$PHOTO$$ERROR";
                                //// TODO: 2018/3/22 给web坐席发送消息 告知消息发送失败
                                Toast.makeText(ZCBLVideoSurveyActivity.this,"上传图片失败，请重试",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onSuccess(final String response) {
                        ZCBLVideoSurveyActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressbar.setVisibility(View.GONE);
                                canTakePic = true;
                                try {
                                    JSONObject object = new JSONObject(response);
                                    JSONObject obj = object.optJSONObject("data");

                                    String originalPhotoUrl = obj.optString("originalPhotoUrl");
                                    String watermarkPhotoUrl = obj.optString("watermarkPhotoUrl");
                                    String lon = zcblVideoSurveyModel.getLongitude();
                                    String lat = zcblVideoSurveyModel.getLatitude();

                                    String tempStr = "APP$$PHOTO$$";
                                    StringBuilder sb = new StringBuilder(tempStr);
                                    sb.append(originalPhotoUrl.replaceAll("/","%"))
                                            .append("&")
                                            .append(watermarkPhotoUrl.replaceAll("/","%"))
                                            .append("&")
                                            .append(lon)
                                            .append("&")
                                            .append(lat);
                                    Log.i(ZCBLConstants.TAG,"-------------upload--string---->"+sb.toString());
                                    //// TODO: 2018/3/22 通知坐席拍照成功 通过im告知坐席上传的图片信息

                                    Toast.makeText(ZCBLVideoSurveyActivity.this,"上传图片成功",Toast.LENGTH_SHORT).show();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }



                            }
                        });
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void getImLoginInfo() {
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
                        Log.w(ZCBLConstants.TAG,"获取登录信息失败，点击重试");
                        //失败后点击Title可以重试
                        //// TODO: 2018/3/22  登录失败之后的处理流程
                        retrygetLoginInfo();
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

                        doLoginImInit(selfAccountInfo);

                    }
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 进行im登录
     * @param selfAccountInfo
     */
    private void doLoginImInit(SelfAccountInfo selfAccountInfo) {
        rtcRoom.init(DOMAIN,selfAccountInfo, new RTCRoom.InitCallback() {
            @Override
            public void onError(int errCode, String errInfo) {

            }

            @Override
            public void onSuccess(String userId) {
                //IM初始化成功

            }
        });

    }

    private void initRTCRoom() {
        //初始化RCTRoom
        rtcRoom = new RTCRoom(this);
        rtcRoom.setRTCRoomListener(new Impl_IRTCRoomListener());
        rtcRoom.showLocalStreamToView(local_video_view);
        rtcRoom.setRemoteView(remote_video_view);
    }

    private void leaveRoom() {
        rtcRoom.exitRoom(null);
        setResult(VIDEO_SURVEY_IS_OVER);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.w(ZCBLConstants.TAG,"-------------ZCBLVideoSurveyActivity----onDestory--------");

        isRemoteAttach = false;
        isLightOn = false;

        if (null != zcblHeadsetReceiver) {
            unregisterReceiver(zcblHeadsetReceiver);
        }

        if (null != blueAudioNoisyReceiver) {
            unregisterReceiver(blueAudioNoisyReceiver);
        }

        if (null != rtcRoom) {
            rtcRoom.setRTCRoomListener(null);
            rtcRoom.unInit();
        }
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


        remote_video_view = (TXCloudVideoView) findViewById(R.id.rtmproom_video_remote);
        local_video_view = (TXCloudVideoView) findViewById(R.id.rtmproom_video_local);
        remote_video_view.setOnTouchListener(this);

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_goback) {
            exitRoomSuccess();

        } else if (id == R.id.iv_light) {
            controlCameraLight();

        } else if (id == R.id.iv_switch_camera) {
            rtcRoom.switchCamera();
        } else if (id == R.id.iv_takepic) {
            if (canTakePic) {
                //// TODO: 2018/3/22 拍照上传
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap bitmap = null ;
                        uploadImage(bitmap);
                    }
                });
            }
        } else if (id == R.id.iv_layer) {
            iv_layer.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 开启和关闭闪光灯
     */
    private void controlCameraLight() {
        if (!isLightOn) {
            iv_light.setImageResource(R.drawable.ic_light_open);
            controlCameraLight(!isLightOn);
            isLightOn = true;
        }else{
            iv_light.setImageResource(R.drawable.ic_lignt_close);
            controlCameraLight(!isLightOn);
            isLightOn = false ;
        }
    }

    private void controlCameraLight(boolean isOpen) {
        //mFlashTurnOn为true表示打开，否则表示关闭
        if (null != rtcRoom.getLivePusher()) {
            if (!rtcRoom.getLivePusher().turnOnFlashLight(isOpen)) {
                Log.e(ZCBLConstants.TAG,"闪光灯打开失败" );
            }
        }
    }

    /**
     * 移除监听
     */
    public void removeSync() {

    }

    /**
     * 离开直播间 提醒弹框
     */
    private void exitRoomSuccess(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("")
                .setMessage("视频查勘完成，确认退出？")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setResult(VIDEO_SURVEY_IS_OVER);

                        rtcRoom.exitRoom(new RTCRoom.ExitRoomCallback() {
                            @Override
                            public void onError(int errCode, String errInfo) {
                                Log.e(ZCBLConstants.TAG, "exitRoom failed, errorCode = " + errCode + " errMessage = "+errInfo );

                            }

                            @Override
                            public void onSuccess() {
                                Log.i(ZCBLConstants.TAG, "exitRoom Success");
                                finish();
                            }
                        });
                    }
                });
        builder.show();

    }

    private void retrygetLoginInfo(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("")
                .setMessage("获取登录信息失败，请重试？")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getImLoginInfo();
                    }
                });
        builder.show();
    }

    /**
     * 监听android物理按键
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            exitRoomSuccess();
            return false;
        }else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onStop() {
        super.onStop();
        local_video_view.onPause();  // mCaptureView 是摄像头的图像渲染view
        if(null != rtcRoom.getLivePusher()){
            rtcRoom.getLivePusher().pausePusher(); // 通知 SDK 进入“后台推流模式”了
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        local_video_view.onResume();     // mCaptureView 是摄像头的图像渲染view
        if(null != rtcRoom.getLivePusher()){
            rtcRoom.getLivePusher().resumePusher();  // 通知 SDK 重回前台推流
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                screenWidth = rootView.getWidth();
                screenHeight = rootView.getHeight();
                lastX = x ;
                lastY = y ;
                break ;
            case MotionEvent.ACTION_MOVE:
                int offsetX = x -lastX ;
                int offsetY = y - lastY ;
                left = remote_video_view.getLeft()+offsetX ;
                top = remote_video_view.getTop()+offsetY ;
                right = remote_video_view.getRight()+offsetX ;
                bottom = remote_video_view.getBottom()+offsetY ;
                if (left < 0) {
                    left = 0 ;
                    right = remote_video_view.getWidth();
                }
                if (top < 0) {
                    top = 0 ;
                    bottom = remote_video_view.getHeight();
                }
                if (right > screenWidth) {
                    left = screenWidth - remote_video_view.getWidth() ;
                    right = screenWidth;
                }
                if (bottom > screenHeight) {
                    top = screenHeight - remote_video_view.getHeight();
                    bottom = screenHeight;
                }
                remote_video_view.layout(left, top, right, bottom);
                lastX = x ;
                lastY = y ;
                break ;
            case MotionEvent.ACTION_UP:
                lastX = x ;
                lastY = y ;
                //将最后拖拽的位置定下来，否则页面刷新渲染后按钮会自动回到初始位置
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) v.getLayoutParams();
                /**
                 * 将xml中设置的layout_alignParentBottom不起作用
                 * 第一种方式：
                 * lp.setMargins(left,top,0,0);
                 * lp.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                 * 第二种方式：
                 * lp.setMargins(left,top,screenWidth-right,screenHeight-bottom);
                 */
                lp.setMargins(left,top,screenWidth-right,screenHeight-bottom);
                lp.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                v.setLayoutParams(lp);
                break ;
        }
        return true;
    }

}
