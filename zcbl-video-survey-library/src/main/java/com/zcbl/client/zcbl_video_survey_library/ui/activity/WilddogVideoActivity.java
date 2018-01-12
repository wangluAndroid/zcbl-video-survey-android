package com.zcbl.client.zcbl_video_survey_library.ui.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.wilddog.client.ChildEventListener;
import com.wilddog.client.DataSnapshot;
import com.wilddog.client.SyncError;
import com.wilddog.client.SyncReference;
import com.wilddog.client.WilddogSync;
import com.wilddog.video.base.LocalStream;
import com.wilddog.video.base.LocalStreamOptions;
import com.wilddog.video.base.WilddogVideoError;
import com.wilddog.video.base.WilddogVideoInitializer;
import com.wilddog.video.base.WilddogVideoView;
import com.wilddog.video.base.util.LogUtil;
import com.wilddog.video.base.util.logging.Logger;
import com.wilddog.video.room.CompleteListener;
import com.wilddog.video.room.RoomStream;
import com.wilddog.video.room.WilddogRoom;
import com.wilddog.wilddogauth.WilddogAuth;
import com.zcbl.client.zcbl_video_survey_library.Constants;
import com.zcbl.client.zcbl_video_survey_library.R;
import com.zcbl.client.zcbl_video_survey_library.bean.WilddogVideoModel;
import com.zcbl.client.zcbl_video_survey_library.service.HttpUtils;
import com.zcbl.client.zcbl_video_survey_library.ui.receiver.BluetoothConnectionReceiver;
import com.zcbl.client.zcbl_video_survey_library.ui.receiver.HeadsetReceiver;
import com.zcbl.client.zcbl_video_survey_library.utils.Base64Utils;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.zcbl.client.zcbl_video_survey_library.Constants.GO_TO_VIDEO_ROOM;
import static com.zcbl.client.zcbl_video_survey_library.Constants.REFRESH_IMAGE_LIST;


/**
 * Created by serenitynanian on 2017/12/15.
 * 1.所有web端关闭坐席，App端只要回调远端流退出回调，app必须退出房间，返回到进入界面
 * 2.所有的断开只返回到进入界面
 */

public class WilddogVideoActivity extends AppCompatActivity implements View.OnClickListener {

    private WilddogVideoView wilddog_video_view;

    private LocalStream localStream;
    private boolean isAudioEnable = true;
    private boolean isVideoEnable = true;
    private WilddogVideoInitializer initializer;
    private WilddogRoom room;
    private TextView iv_goback;
    private ImageView iv_light;
    private ImageView iv_switch_camera;
    private ImageView iv_takepic;
    private ProgressBar progressbar;
    private boolean canTakePic = true ;
    private SyncReference syncReference;
    private ChildEventListener childEventListener;
    private WilddogVideoModel wilddogVideoModel ;

    private boolean isLightOn = false ;
    private ImageView iv_switch_audio;
    private HeadsetReceiver headsetReceiver;
    private BluetoothConnectionReceiver blueAudioNoisyReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_wilddog_video);
        wilddogVideoModel = (WilddogVideoModel) getIntent().getSerializableExtra("wilddogVideoModel");
        registerBroadcast();
        initView();
        initRoomSDK();
        createLocalStream();
        joinRoom();
        initSync();
    }

    private void registerBroadcast() {

        //动态注册耳机插入广播
        headsetReceiver = new HeadsetReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.HEADSET_PLUG");
        this.registerReceiver(headsetReceiver, intentFilter);

        //动态注册蓝牙广播
        blueAudioNoisyReceiver = new BluetoothConnectionReceiver();
        //蓝牙状态广播监听
        IntentFilter audioFilter = new IntentFilter();
        audioFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);//蓝牙设备连接或断开
        audioFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);//本机开启、关闭蓝牙开关
        this.registerReceiver(blueAudioNoisyReceiver, audioFilter);

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
                String result = Base64Utils.encodeToString(bitmapBytes, true);
                if (TextUtils.isEmpty(result)) {
                    Toast.makeText(WilddogVideoActivity.this,"上传图片失败，请重试",Toast.LENGTH_SHORT).show();
                    return ;
                }
                JSONObject json = new JSONObject();
                try {
                    json.put("photoContent", result);
                    json.put("longitude", wilddogVideoModel.getLongitude());
                    json.put("latitude", wilddogVideoModel.getLatitude());
                    json.put("shotLocation", wilddogVideoModel.getCaseAddress());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                HttpUtils.getInstance().post(Constants.UPLOAD_IMAGE_URL,json,new HttpUtils.UpdateCallback() {
                    @Override
                    public void onError(String error) {
                        WilddogVideoActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressbar.setVisibility(View.GONE);
                                canTakePic = true ;
                                String tempStr = "APP$$PHOTO$$ERROR";
                                if (null != syncReference) {
                                    syncReference.push().setValue(tempStr);
                                }
                                Toast.makeText(WilddogVideoActivity.this,"上传图片失败，请重试",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onSuccess(final String response) {
                        WilddogVideoActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressbar.setVisibility(View.GONE);
                                canTakePic = true;
                                try {
                                    JSONObject object = new JSONObject(response);
                                    JSONObject obj = object.optJSONObject("data");

                                    String originalPhotoUrl = obj.optString("originalPhotoUrl");
                                    String watermarkPhotoUrl = obj.optString("watermarkPhotoUrl");
                                    String lon = wilddogVideoModel.getLongitude();
                                    String lat = wilddogVideoModel.getLatitude();

                                    String tempStr = "APP$$PHOTO$$";
                                    StringBuilder sb = new StringBuilder(tempStr);
                                    sb.append(originalPhotoUrl.replaceAll("/","%"))
                                            .append("&")
                                            .append(watermarkPhotoUrl.replaceAll("/","%"))
                                            .append("&")
                                            .append(lon)
                                            .append("&")
                                            .append(lat);
                                    System.out.println("-------------upload--string---->"+sb.toString());
                                    if (null != syncReference) {
                                        syncReference.push().setValue(sb.toString());
                                    }
                                    Toast.makeText(WilddogVideoActivity.this,"上传图片成功",Toast.LENGTH_SHORT).show();
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

    private void initSync() {
        syncReference = WilddogSync.getInstance().getReference(wilddogVideoModel.getSyncCommandNodePath());
        childEventListener = syncReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String type = dataSnapshot.getValue().toString();
                if (!TextUtils.isEmpty(type)) {
                    if ("WEB$$takePic".equals(type)) {
                        localStream.capturePicture(new LocalStream.CaptureListener() {
                            @Override
                            public void onCaptureCompleted(final Bitmap bitmap) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        uploadImage(bitmap);
                                    }
                                });
                            }

                            @Override
                            public void onError(WilddogVideoError wilddogVideoError) {
                                Log.e(Constants.TAG, "onError: 拍照---->"+wilddogVideoError.getMessage() );
                            }
                        },false);
                    } else if ("WEB$$openLight".equals(type)) {
                        controlCameraLight();
//                        Toast.makeText(WilddogVideoActivity.this, "开启闪光灯", Toast.LENGTH_LONG).show();
                    } else if ("WEB$$closeLight".equals(type)) {
                        controlCameraLight();
//                        Toast.makeText(WilddogVideoActivity.this, "关闭闪光灯", Toast.LENGTH_LONG).show();
                    } else if ("WEB$$surveyIsOver".equals(type)) {
                        WilddogVideoActivity.this.setResult(GO_TO_VIDEO_ROOM);
                        WilddogVideoActivity.this.finish();
                    }

                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(SyncError syncError) {

            }
        });
    }

    private void joinRoom() {
        room = new WilddogRoom(wilddogVideoModel.getVideoRoomId(), new WilddogRoom.Listener() {
            @Override
            public void onConnected(WilddogRoom wilddogRoom) {
                room.publish(localStream, new CompleteListener() {
                    //调用成功 videoError 对象为 null，否则通过 videoError 对象传递错误信息。
                    @Override
                    public void onComplete(final WilddogVideoError wilddogVideoError) {
                        if (wilddogVideoError != null) {
                            //失败
                            Log.e("error", "error:" + wilddogVideoError.getMessage());
                            Toast.makeText(WilddogVideoActivity.this, "推送流失败", Toast.LENGTH_SHORT).show();
                        } else {
//                            Toast.makeText(WilddogVideoActivity.this, "推送流成功", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onDisconnected(WilddogRoom wilddogRoom) {
//                Toast.makeText(WilddogVideoActivity.this, "服务器连接断开", Toast.LENGTH_SHORT).show();
                setResult(REFRESH_IMAGE_LIST);
                finish();
            }

            /**
             * Room 中有远端媒体流加入。回调中的 RoomStream 对象只包含描述流的基本信息，不包含媒体数据，需要调用 subscribe() 方法获取媒体数据。
             * @param wilddogRoom
             * @param roomStream
             */
            @Override
            public void onStreamAdded(WilddogRoom wilddogRoom, RoomStream roomStream) {
                room.subscribe(roomStream, new CompleteListener() {
                    @Override
                    public void onComplete(WilddogVideoError wilddogVideoError) {
                    }
                });
            }

            //Room 中有远端媒体流停止发布。
            @Override
            public void onStreamRemoved(WilddogRoom wilddogRoom, RoomStream roomStream) {
                if(null==roomStream) {
                    return;
                }
                room.unsubscribe(roomStream, new CompleteListener() {
                    @Override
                    public void onComplete(WilddogVideoError wilddogVideoError) {
                        setResult(REFRESH_IMAGE_LIST);
                        finish();
                    }
                });

            }

            /**
             * 注意：在这个地方来渲染video到视图上
             * @param wilddogRoom
             * @param roomStream
             */
            //收到远端媒体流数据。调用 RoomStream.attach() 方法在 WilddogVideoView 中预览媒体流。
            @Override
            public void onStreamReceived(WilddogRoom wilddogRoom, RoomStream roomStream) {
                // 在控件中显示
//                StreamHolder holder = new StreamHolder(false, System.currentTimeMillis(), roomStream);
//                holder.setId(roomStream.getStreamId());
//                streamHolders.add(holder);
//                handler.sendEmptyMessage(0);
                roomStream.enableVideo(false);
            }

            @Override
            public void onStreamChanged(WilddogRoom wilddogRoom, RoomStream roomStream) {
                // 混流使用
            }

            @Override
            public void onError(WilddogRoom wilddogRoom, final WilddogVideoError wilddogVideoError) {
                Toast.makeText(WilddogVideoActivity.this, "发生错误,请产看日志", Toast.LENGTH_SHORT).show();
                Log.e("error", "错误码:" + wilddogVideoError.getErrCode() + ",错误信息:" + wilddogVideoError.getMessage());
                setResult(REFRESH_IMAGE_LIST);
                finish();
            }
        });
        //加入 Room。成功加入 Room 会触发本地 onConnected 事件，否则触发 onError()) 事件。
        room.connect();
    }

    private void initRoomSDK() {
        LogUtil.setLogLevel(Logger.Level.DEBUG);
        WilddogVideoInitializer.initialize(WilddogVideoActivity.this, Constants.WILDDOG_VIDEO_ID, WilddogAuth.getInstance().getCurrentUser().getToken(false).getResult().getToken());
        initializer = WilddogVideoInitializer.getInstance();
    }

    private void createLocalStream() {
        LocalStreamOptions options = new LocalStreamOptions
                .Builder()
//                .maxBitrateBps(491520)
//                .minBitrateBps(245760)
//                .dimension(LocalStreamOptions.Dimension.DIMENSION_1080P)
                .dimension(LocalStreamOptions.Dimension.DIMENSION_MAX)
                .defaultCameraSource(LocalStreamOptions.CameraSource.BACK_CAMERA)
                .build();
        localStream = LocalStream.create(options);
        localStream.enableAudio(isAudioEnable);
        localStream.enableVideo(isVideoEnable);
//        localStream.setFlashMode(LocalStream.FlashMode.FLASH_MODE_TORCH);
//        localStream.switchCamera();
//        //将本地媒体流绑定到WilddogVideoView中
        localStream.attach(wilddog_video_view);


    }

    private void leaveRoom() {
        if (room != null) {
            room.disconnect();
            room = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.w(Constants.TAG,"-------------WilddogVideoActivity----onDestory--------");
        leaveRoom();
        if (null != syncReference) {
            if (null != childEventListener) {
                syncReference.removeEventListener(childEventListener);
            }
            syncReference = null ;
        }

        if (null!=localStream&&!localStream.isClosed()) {
            localStream.detach();
            localStream.close();
        }
       
        isLightOn = false;
        if (null != wilddog_video_view) {
            wilddog_video_view.release();
        }

        if (null != headsetReceiver) {
            unregisterReceiver(headsetReceiver);
        }

        if (null != blueAudioNoisyReceiver) {
            unregisterReceiver(blueAudioNoisyReceiver);
        }
    }


    private void initView() {
        wilddog_video_view = (WilddogVideoView)findViewById(R.id.wilddog_video_view);
        iv_goback = (TextView) findViewById(R.id.iv_goback);
        iv_goback.setOnClickListener(this);
        iv_light = (ImageView) findViewById(R.id.iv_light);
        iv_light.setOnClickListener(this);
        iv_switch_camera = (ImageView) findViewById(R.id.iv_switch_camera);
        iv_switch_camera.setOnClickListener(this);
        iv_takepic = (ImageView) findViewById(R.id.iv_takepic);
        iv_takepic.setOnClickListener(this);

//        iv_switch_audio = (ImageView) findViewById(R.id.iv_switch_audio);
//        iv_switch_audio.setOnClickListener(this);

        progressbar = (ProgressBar) findViewById(R.id.progressbar);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_goback) {
            exitRoomSuccess();

        } else if (id == R.id.iv_light) {
            controlCameraLight();

        } else if (id == R.id.iv_switch_camera) {
            if (localStream != null) {
                localStream.switchCamera();
            }

        } else if (id == R.id.iv_takepic) {
            if (canTakePic) {
                localStream.capturePicture(new LocalStream.CaptureListener() {
                    @Override
                    public void onCaptureCompleted(final Bitmap bitmap) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                uploadImage(bitmap);
                            }
                        });
                    }

                    @Override
                    public void onError(WilddogVideoError wilddogVideoError) {
                        Log.e(Constants.TAG, "WilddogVideoActivity---onError: 拍照---->"+wilddogVideoError.getMessage() );
                    }
                },false);
            }

//            case R.id.iv_switch_audio:
//                AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//                audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
//                audioManager.stopBluetoothSco();
//                audioManager.setBluetoothScoOn(false);
//                audioManager.setSpeakerphoneOn(false);
//
//                System.out.println("--------是否耳机线连接-------->"+audioManager.isWiredHeadsetOn());
//                System.out.println("--------扬声器是否打开-------->"+audioManager.isSpeakerphoneOn());
//                System.out.println("--------蓝牙耳机是否打开A2DP-------->"+audioManager.isBluetoothA2dpOn());
//                System.out.println("--------蓝牙耳机是否打开SCO-------->"+audioManager.isBluetoothScoOn());
//                break ;
        }
    }

    /**
     * 开启和关闭闪光灯
     */
    private void controlCameraLight() {
        if (!isLightOn) {
            iv_light.setImageResource(R.drawable.ic_light_open);
            localStream.setFlashMode(LocalStream.FlashMode.FLASH_MODE_TORCH);
            isLightOn = true;
        }else{
            iv_light.setImageResource(R.drawable.ic_lignt_close);
            localStream.setFlashMode(LocalStream.FlashMode.FLASH_MODE_OFF);
            isLightOn = false ;
        }
    }

    /**
     * 移除监听
     */
    public void removeSync() {
        if (null != syncReference) {
            if (null != childEventListener) {
                syncReference.removeEventListener(childEventListener);
            }
            syncReference = null ;
        }
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
//                        leaveRoom();
                        setResult(REFRESH_IMAGE_LIST);
                        finish();
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
        leaveRoom();
        setResult(REFRESH_IMAGE_LIST);
        finish();
    }
}