package com.zcbl.client.zcbl_video_survey_library.ui.activity;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.MenuRes;
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

import com.tencent.rtmp.TXLivePusher;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.zcbl.client.zcbl_video_survey_library.R;
import com.zcbl.client.zcbl_video_survey_library.ZCBLConstants;
import com.zcbl.client.zcbl_video_survey_library.bean.ZCBLVideoSurveyModel;
import com.zcbl.client.zcbl_video_survey_library.service.UpdateCallbackInterface;
import com.zcbl.client.zcbl_video_survey_library.service.ZCBLHttpUtils;
import com.zcbl.client.zcbl_video_survey_library.ui.receiver.ZCBLBluetoothConnectionReceiver;
import com.zcbl.client.zcbl_video_survey_library.ui.receiver.ZCBLHeadsetReceiver;
import com.zcbl.client.zcbl_video_survey_library.ui.tx.listener.IReceiveIMListener;
import com.zcbl.client.zcbl_video_survey_library.ui.tx.RTCRoom;
import com.zcbl.client.zcbl_video_survey_library.ui.tx.VideoDisplayPresenter;
import com.zcbl.client.zcbl_video_survey_library.ui.tx.bean.RoomManager;
import com.zcbl.client.zcbl_video_survey_library.ui.tx.listener.IVideoDisplayListener;
import com.zcbl.client.zcbl_video_survey_library.utils.ZCBLBase64Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.zcbl.client.zcbl_video_survey_library.ZCBLConstants.VIDEO_SURVEY_IS_OVER;


/**
 * Created by serenitynanian on 2017/12/15.
 * 1.所有web端关闭坐席，App端只要回调远端流退出回调，app必须退出房间，返回到进入界面
 * 2.所有的断开只返回到进入界面
 */

public class ZCBLTXVideoSurveyActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener,IReceiveIMListener,IVideoDisplayListener {
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

    private VideoDisplayPresenter mPresenter ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_wilddog_video);
//        zcblVideoSurveyModel = (ZCBLVideoSurveyModel) getIntent().getSerializableExtra("ZCBLVideoSurveyModel");
        initView();
        initVideoDisplayPresenter();
        //初始化IM
        mPresenter.initIMMessagesListener();
        // 2018/3/23 显示本地流
        mPresenter.showLocalStreamToView();
        // 2018/3/23  推本地流
        mPresenter.startPushStream();


    }

    //初始化presenter
    private void initVideoDisplayPresenter() {
        RoomManager roomManager = (RoomManager) getIntent().getSerializableExtra("roomManager");
        mPresenter = new VideoDisplayPresenter(this, roomManager);
        mPresenter.setTXCloudView(local_video_view,remote_video_view);
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
            ZCBLTXVideoSurveyActivity.this.setResult(VIDEO_SURVEY_IS_OVER);
            ZCBLTXVideoSurveyActivity.this.finish();
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
        TXLivePusher livePusher = mPresenter.getLivePusher();
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
                    Toast.makeText(ZCBLTXVideoSurveyActivity.this,"上传图片失败，请重试",Toast.LENGTH_SHORT).show();
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
                        ZCBLTXVideoSurveyActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressbar.setVisibility(View.GONE);
                                canTakePic = true ;
                                String tempStr = "APP$$PHOTO$$ERROR";
                                //// TODO: 2018/3/22 给web坐席发送消息 告知消息发送失败
                                Toast.makeText(ZCBLTXVideoSurveyActivity.this,"上传图片失败，请重试",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onSuccess(final String response) {
                        ZCBLTXVideoSurveyActivity.this.runOnUiThread(new Runnable() {
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

                                    Toast.makeText(ZCBLTXVideoSurveyActivity.this,"上传图片成功",Toast.LENGTH_SHORT).show();
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

        if (null != mPresenter) {
//            mPresenter.setRTCRoomListener(null);
//            mPresenter.unInit();
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
            goBack();

        } else if (id == R.id.iv_light) {
            controlCameraLight();

        } else if (id == R.id.iv_switch_camera) {
            mPresenter.switchCamera();
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
        if (null != mPresenter.getLivePusher()) {
            if (!mPresenter.getLivePusher().turnOnFlashLight(isOpen)) {
                Log.e(ZCBLConstants.TAG,"闪光灯打开失败" );
            }
        }
    }



    private void goBack(){
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
                mPresenter.exitRoom();
            }
        });
        builder.show();
    }


    /**
     * 离开直播间 提醒弹框
     */
    @Override
    public void exitRoomSuccess(){
        onDebugLog("exit room Success");
        setResult(VIDEO_SURVEY_IS_OVER);
        finish();
    }

    @Override
    public void exitRoomFailure(int code, String errorInfo) {
        onDebugLog("exit room failure code: " + code + " errmsg: " + errorInfo);

    }

    @Override
    public void exitIMFailure(int code, String desc) {
        onDebugLog("exit im failure code: " + code + " errmsg: " + desc);
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
            goBack();
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
        if(null != mPresenter&&null != mPresenter.getLivePusher()){
            mPresenter.getLivePusher().pausePusher(); // 通知 SDK 进入“后台推流模式”了
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        local_video_view.onResume();     // mCaptureView 是摄像头的图像渲染view
        if(null != mPresenter && null != mPresenter.getLivePusher()){
            mPresenter.getLivePusher().resumePusher();  // 通知 SDK 重回前台推流
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

    @Override
    public void onDebugLog(String log) {
        Log.e(ZCBLConstants.TAG, "onDebugLog: "+log);
    }

    @Override
    public void pushStreamSuccess() {
        onDebugLog("push Stream Success ");
    }

    @Override
    public void pushStreamFailure(int code, String errorInfo) {
        Log.e(ZCBLConstants.TAG, "pushStreamFailure:  code-->"+code +",errorInfo--->"+errorInfo );
    }
}
