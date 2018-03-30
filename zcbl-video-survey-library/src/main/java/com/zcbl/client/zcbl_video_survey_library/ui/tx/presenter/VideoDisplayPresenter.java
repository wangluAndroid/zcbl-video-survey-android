package com.zcbl.client.zcbl_video_survey_library.ui.tx.presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.tencent.imsdk.TIMCallBack;
import com.tencent.imsdk.TIMConversation;
import com.tencent.imsdk.TIMConversationType;
import com.tencent.imsdk.TIMElem;
import com.tencent.imsdk.TIMElemType;
import com.tencent.imsdk.TIMManager;
import com.tencent.imsdk.TIMMessage;
import com.tencent.imsdk.TIMMessageListener;
import com.tencent.imsdk.TIMTextElem;
import com.tencent.imsdk.TIMValueCallBack;
import com.tencent.rtmp.ITXLivePlayListener;
import com.tencent.rtmp.ITXLivePushListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePlayer;
import com.tencent.rtmp.TXLivePushConfig;
import com.tencent.rtmp.TXLivePusher;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.zcbl.client.zcbl_video_survey_library.R;
import com.zcbl.client.zcbl_video_survey_library.ZCBLConstants;
import com.zcbl.client.zcbl_video_survey_library.ui.activity.ZCBLVideoSurveyActivity;
import com.zcbl.client.zcbl_video_survey_library.ui.tx.bean.PlayerItem;
import com.zcbl.client.zcbl_video_survey_library.ui.tx.bean.PusherInfo;
import com.zcbl.client.zcbl_video_survey_library.ui.tx.bean.RoomManager;
import com.zcbl.client.zcbl_video_survey_library.ui.tx.bean.RoomState;
import com.zcbl.client.zcbl_video_survey_library.ui.tx.http.HttpRequests;
import com.zcbl.client.zcbl_video_survey_library.ui.tx.http.HttpResponse;
import com.zcbl.client.zcbl_video_survey_library.ui.tx.listener.IVideoDisplayListener;
import com.zcbl.client.zcbl_video_survey_library.utils.DateUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by serenitynanian on 2018/3/23.
 */

public class VideoDisplayPresenter {
    private final HttpRequests mHttpRequest;
    private HeartBeatThread mHeartBeatThread;


    public static int RTCROOM_VIDEO_RATIO_9_16    = 1; //视频分辨率为9:16
    public static int RTCROOM_VIDEO_RATIO_3_4     = 2; //视频分辨率为3:4
    public static int RTCROOM_VIDEO_RATIO_1_1     = 3; //视频分辨率为1:1
    private final MyTIMMessageListener myIMListener;

    private int mBeautyStyle = TXLiveConstants.BEAUTY_STYLE_SMOOTH;
    private int mBeautyLevel = 0;
    private int mWhiteningLevel = 0;
    private int mRuddyLevel = 0;


    private HashMap<String, PlayerItem> mPlayers     = new LinkedHashMap<>();

    private Context mContext ;
    private Handler mHandler ;
    private RoomManager mRoomManager ;
    private TXCloudVideoView local_video_view;
    private TXCloudVideoView remote_video_view;
    private TXLivePusher mTXLivePusher;
    private IVideoDisplayListener mVideoDisplayListener ;
    private TXLivePushListenerImpl mTXLivePushListener;
    private ZCBLVideoSurveyActivity zcblVideoSurveyActivity ;

    public VideoDisplayPresenter(Context context, RoomManager roomManager) {
        mContext = context;
        zcblVideoSurveyActivity = (ZCBLVideoSurveyActivity) context;
        mVideoDisplayListener = (IVideoDisplayListener) context;
        mRoomManager = roomManager ;
        myIMListener = new MyTIMMessageListener();
        mHandler = new Handler(Looper.getMainLooper());
        mHttpRequest = HttpRequests.getHttpReqeust(ZCBLConstants.DOMAIN);
        mHeartBeatThread = new HeartBeatThread();
    }

    public void setTXCloudView(TXCloudVideoView local_video_view, TXCloudVideoView remote_video_view) {
        this.local_video_view = local_video_view;
        this.remote_video_view = remote_video_view ;
    }


    /**
     * 发送消息给坐席
     */
    public void sendIMToZuoxi( String sendMessage, final String type) {
        //获取单聊会话
        String zuoxiIMAccount = mRoomManager.getZuoxiIMAccount();
        TIMConversation conversation = TIMManager.getInstance().getConversation(
                TIMConversationType.C2C,    //会话类型：单聊
                zuoxiIMAccount);                      //会话对方用户帐号//对方id
        Log.e(ZCBLConstants.TAG, "--sendIMToZuoxi---sendMessage:"+sendMessage);
        TIMMessage msg = new TIMMessage();
        TIMTextElem elem = new TIMTextElem();
        String requireSendMessage = "";
        if (type.equals("requestConnect")) {
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(sendMessage);
                jsonObject.put("selToID", mRoomManager.getSelfUserID());
                requireSendMessage = jsonObject.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            requireSendMessage = sendMessage ;
        }
        elem.setText(requireSendMessage);
        //将elem添加到消息
        if(msg.addElement(elem) != 0) {
            Log.e(ZCBLConstants.TAG, "--sendIMToZuoxi---addElement failed");
            return;
        }
        //发送消息
        conversation.sendMessage(msg, new TIMValueCallBack<TIMMessage>() {//发送消息回调
            @Override
            public void onError(int code, String desc) {//发送消息失败
                //错误码code和错误描述desc，可用于定位请求失败原因
                //错误码code含义请参见错误码表
                mVideoDisplayListener.sendMessageFailure(code,desc);
            }

            @Override
            public void onSuccess(TIMMessage msg) {//发送消息成功
                mVideoDisplayListener.sendMessageSuccess(type);
            }
        });
    }


    public class MyTIMMessageListener implements TIMMessageListener{

        @Override
        public boolean onNewMessages(List<TIMMessage> list) {
            //todo 通过onNewMessage抛出的消息不一定是未读的消息，只是本地曾经没有过的消息
            //消息的内容解析请参考消息收发文档中的消息解析说明
            TIMMessage msg =list.get(0) ;
            TIMElem elem = msg.getElement(0);
            //获取当前元素的类型
            TIMElemType elemType = elem.getType();
            Log.d(ZCBLConstants.TAG, "elem type: " + elemType.name());
            if (elemType == TIMElemType.Text) {
                //处理文本消息
                String text = ((TIMTextElem) elem).getText();
                Log.e(ZCBLConstants.TAG, "-------onNewMessages:--date->"+ DateUtils.convertLongToyyyyMMddHHmmss(msg.timestamp())+"---sender--> "+msg.getSender()+"----消息内容--->"+text);
                if (!TextUtils.isEmpty(text)) {
                    zcblVideoSurveyActivity.receiveIMMessage(text);
                }
            }
            return true; //返回true将终止回调链，不再调用下一个新消息监听器
        }
    }

    /**
     * 初始化IM
     */
    public void initIMMessagesListener(){
        //设置消息监听
        TIMManager.getInstance().addMessageListener(myIMListener);
    }


    /**
     * 创建房间
     */
    public void createRoom(){
        final String userId = mRoomManager.getSelfUserID();
        final String userName = mRoomManager.getSelfUserName();
        final String avatarUrl = mRoomManager.getAvatarUrl();
        final String roomName = mRoomManager.getRoomName();
        final String pushURL = mRoomManager.getPushUrl();
        final String roomId = mRoomManager.getRoomId();

        mHttpRequest.createRoom(roomId,userId, roomName, userName, avatarUrl, pushURL, new HttpRequests.OnResponseCallback<HttpResponse.CreateRoom>() {
            @Override
            public void onResponse(int retcode, @Nullable String retmsg, @Nullable HttpResponse.CreateRoom data) {
                if (retcode != HttpResponse.CODE_OK || data == null || data.roomID == null) {
                    mVideoDisplayListener.createRoomFailure(retcode, retmsg);
                } else {
                    mHeartBeatThread.startHeartbeart();
                    mRoomManager.setState(RoomState.Created | RoomState.Empty);
                    mRoomManager.setRoomId(data.roomID);
                    mRoomManager.setRoomName(roomName);
                    mRoomManager.setState(RoomState.Created);
                    mVideoDisplayListener.createRoomSuccess(mRoomManager,data.roomID);
                    sendIMToZuoxi(mRoomManager.getSurveyInfo(),"requestConnect");
                }
            }
        });
    }

    /**
     * 展示本地流到手机界面TXCloudVideoView上
     */
    public void showLocalStreamToView() {
        startLocalPreview(local_video_view);
        setPauseImage(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.pause_publish));
        setBitrateRange(400, 800);
        setVideoRatio(RTCROOM_VIDEO_RATIO_3_4);
        setHDAudio(true);
        setBeautyFilter(mBeautyStyle, mBeautyLevel, mWhiteningLevel, mRuddyLevel);
    }


    /**
     * 启动摄像头预览
     * @param videoView 摄像头预览组件
     */
    public synchronized void startLocalPreview(final @NonNull TXCloudVideoView videoView) {
        mVideoDisplayListener.onDebugLog("[RTCRoom] startLocalPreview");
        initLivePusher();
        if (mTXLivePusher != null) {
            //将界面元素和pusher对象关联起来，从而能够将手机摄像头采集到的画面渲染到手机屏幕上
            videoView.setVisibility(View.VISIBLE);
            mTXLivePusher.startCameraPreview(videoView);
        }
    }

    private void unInitLivePusher() {
        if (mTXLivePusher != null) {
            mTXLivePushListener = null;
            mTXLivePusher.setPushListener(null);
            mTXLivePusher.stopCameraPreview(true);
            mTXLivePusher.stopPusher();
            mTXLivePusher = null;
        }
    }

    /**
     * 反初始化
     */
    public void unInit() {
        zcblVideoSurveyActivity.onDebugLog("----unInit---反初始化--");
        mContext = null;
        mHandler = null;
        TIMManager.getInstance().removeMessageListener(myIMListener);
        mHeartBeatThread.stopHeartbeat();
        mHeartBeatThread.quit();
        mRoomManager = null ;
        mHeartBeatThread = null;
        mVideoDisplayListener =null ;
        mContext = null ;
        mRoomManager = null ;

    }

    /**
     * 初始化TXLivePusher
     */
    private void initLivePusher() {
        if (mTXLivePusher == null) {
            TXLivePushConfig config = new TXLivePushConfig();
            config.setPauseFlag(TXLiveConstants.PAUSE_FLAG_PAUSE_VIDEO | TXLiveConstants.PAUSE_FLAG_PAUSE_AUDIO);
            config.setFrontCamera(false);//设置后置摄像头
            config.setTouchFocus(false);//关闭手动对焦   变成自动对焦
            config.setHomeOrientation(TXLiveConstants.VIDEO_ANGLE_HOME_RIGHT);
            mTXLivePusher = new TXLivePusher(this.mContext);
            mTXLivePusher.setConfig(config);
            //关闭美颜
            mTXLivePusher.setBeautyFilter(TXLiveConstants.BEAUTY_STYLE_SMOOTH, 0, 0, 0);
            mTXLivePusher.setVideoQuality(TXLiveConstants.VIDEO_QUALITY_REALTIEM_VIDEOCHAT, true, true);

            mTXLivePushListener = new TXLivePushListenerImpl();
            mTXLivePusher.setPushListener(mTXLivePushListener);
        }
    }

    private void runOnUiThread(final Runnable runnable){
        if (mHandler != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    runnable.run();
                }
            });
        }
    }


    /**
     * 开始向远端推流
     */
    public void startPushStream(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mTXLivePushListener != null) {
                    if (mTXLivePushListener.cameraEnable() == false) {
                        mVideoDisplayListener.pushStreamFailure(-1, "获取摄像头权限失败，请前往隐私-相机设置里面打开应用权限");
                        return;
                    }
                    if (mTXLivePushListener.micEnable() == false) {
                        mVideoDisplayListener.pushStreamFailure(-1, "获取摄像头权限失败，请前往隐私-相机设置里面打开应用权限");
                        return;
                    }
                }
                if (mTXLivePusher != null) {
                    mVideoDisplayListener.onDebugLog("[RTCRoom] 开始推流 PushUrl :"+mRoomManager.getPushUrl());
                    mTXLivePusher.startPusher(mRoomManager.getPushUrl());
                }
            }
        });
    }
    /**
     * 设置从前台切换到后台，推送的图片
     * @param bitmap
     */
    public void setPauseImage(final @Nullable Bitmap bitmap) {
        if (mTXLivePusher != null) {
            TXLivePushConfig config = mTXLivePusher.getConfig();
            config.setPauseImg(bitmap);
            config.setPauseFlag(TXLiveConstants.PAUSE_FLAG_PAUSE_VIDEO | TXLiveConstants.PAUSE_FLAG_PAUSE_AUDIO);
            mTXLivePusher.setConfig(config);
        }
    }

    /**
     * 从前台切换到后台，关闭采集摄像头数据
     * @param id 设置默认显示图片的资源文件
     */
    public void setPauseImage(final @IdRes int id){
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), id);
        if (mTXLivePusher != null) {
            TXLivePushConfig config = mTXLivePusher.getConfig();
            config.setPauseImg(bitmap);
            config.setPauseFlag(TXLiveConstants.PAUSE_FLAG_PAUSE_VIDEO | TXLiveConstants.PAUSE_FLAG_PAUSE_AUDIO);
            mTXLivePusher.setConfig(config);
        }
    }


    /**
     * 设置视频的码率区间
     * @param minBitrate
     * @param maxBitrate
     */
    public void setBitrateRange(int minBitrate, int maxBitrate) {
        if (mTXLivePusher != null) {
            TXLivePushConfig config = mTXLivePusher.getConfig();
            config.setMaxVideoBitrate(maxBitrate);
            config.setMinVideoBitrate(minBitrate);
            mTXLivePusher.setConfig(config);
        }
    }

    /**
     * 设置推流端视频的分辨率
     */
    public void setVideoRatio(int videoRatio) {
        if (mTXLivePusher != null) {
            TXLivePushConfig config = mTXLivePusher.getConfig();
            if (videoRatio == RTCROOM_VIDEO_RATIO_9_16) {
                config.setVideoResolution(TXLiveConstants.VIDEO_RESOLUTION_TYPE_360_640);
            }
            else if (videoRatio == RTCROOM_VIDEO_RATIO_3_4) {
                config.setVideoResolution(TXLiveConstants.VIDEO_RESOLUTION_TYPE_480_640);
            }
            else if (videoRatio == RTCROOM_VIDEO_RATIO_1_1) {
                config.setVideoResolution(TXLiveConstants.VIDEO_RESOLUTION_TYPE_480_480);
            }
            mTXLivePusher.setConfig(config);
        }
    }



    /**
     * 设置高清音频
     * @param enable true 表示启用高清音频（48K采样），否则 false（16K采样）
     */
    public void setHDAudio(boolean enable) {
        if (mTXLivePusher != null) {
            TXLivePushConfig config = mTXLivePusher.getConfig();
            config.setAudioSampleRate(enable ? 48000 : 16000);
            mTXLivePusher.setConfig(config);
        }
    }

    /**
     * 设置视频分辨率
     * @param resolution 视频分辨率参数值
     */
    public void setVideoResolution(int resolution) {
        if (mTXLivePusher != null) {
            TXLivePushConfig config = mTXLivePusher.getConfig();
            config.setVideoResolution(resolution);
            mTXLivePusher.setConfig(config);
        }
    }


    /**
     * 设置美颜效果.
     * @param style          美颜风格.三种美颜风格：0 ：光滑  1：自然  2：朦胧
     * @param beautyLevel    美颜等级.美颜等级即 beautyLevel 取值为0-9.取值为0时代表关闭美颜效果.默认值:0,即关闭美颜效果.
     * @param whiteningLevel 美白等级.美白等级即 whiteningLevel 取值为0-9.取值为0时代表关闭美白效果.默认值:0,即关闭美白效果.
     * @param ruddyLevel     红润等级.美白等级即 ruddyLevel 取值为0-9.取值为0时代表关闭美白效果.默认值:0,即关闭美白效果.
     * @return               是否成功设置美白和美颜效果. true:设置成功. false:设置失败.
     */
    public boolean setBeautyFilter(int style, int beautyLevel, int whiteningLevel, int ruddyLevel) {
        if (mTXLivePusher != null) {
            return mTXLivePusher.setBeautyFilter(style, beautyLevel, whiteningLevel, ruddyLevel);
        }
        return false;
    }

    /**
     * 调整摄像头焦距
     * @param  value 焦距，取值 0~getMaxZoom();
     * @return  true : 成功 false : 失败
     */
    public boolean setZoom(int value) {
        if (mTXLivePusher != null) {
            return mTXLivePusher.setZoom(value);
        }
        return false;
    }

    /**
     * 设置播放端水平镜像与否(tips：推流端前置摄像头默认看到的是镜像画面，后置摄像头默认看到的是非镜像画面)
     * @param enable true:播放端看到的是镜像画面,false:播放端看到的是镜像画面
     */
    public boolean setMirror(boolean enable) {
        if (mTXLivePusher != null) {
            return mTXLivePusher.setMirror(enable);
        }
        return false;
    }

    /**
     * 调整曝光
     * @param value 曝光比例，表示该手机支持最大曝光调整值的比例，取值范围从-1到1。
     *              负数表示调低曝光，-1是最小值，对应getMinExposureCompensation。
     *              正数表示调高曝光，1是最大值，对应getMaxExposureCompensation。
     *              0表示不调整曝光
     */
    public void setExposureCompensation(float value) {
        if (mTXLivePusher != null) {
            mTXLivePusher.setExposureCompensation(value);
        }
    }

    /**
     * 设置麦克风的音量大小.
     * <p>该接口用于混音处理,比如将背景音乐与麦克风采集到的声音混合后播放.
     * @param x: 音量大小,1为正常音量,建议值为0~2,如果需要调大音量可以设置更大的值.
     * @return 是否成功设置麦克风的音量大小. true:设置麦克风的音量成功. false:设置麦克风的音量失败.
     */
    public boolean setMicVolume(float x) {
        if (mTXLivePusher != null) {
            return mTXLivePusher.setMicVolume(x);
        }
        return false;
    }

    /**
     * 设置背景音乐的音量大小.
     * <p>该接口用于混音处理,比如将背景音乐与麦克风采集到的声音混合后播放.
     * @param x 音量大小,1为正常音量,建议值为0~2,如果需要调大背景音量可以设置更大的值.
     * @return  是否成功设置背景音乐的音量大小. true:设置背景音的音量成功. false:设置背景音的音量失败.
     */
    public boolean setBGMVolume(float x) {
        if (mTXLivePusher != null) {
            return mTXLivePusher.setBGMVolume(x);
        }
        return false;
    }

    /**
     * 设置图像渲染角度.
     * @param rotation 图像渲染角度.
     */
    public void setRenderRotation(int rotation) {
        if (mTXLivePusher != null) {
            mTXLivePusher.setRenderRotation(rotation);
        }
    }

    /**
     * setFilterImage 设置指定素材滤镜特效
     * @param bmp: 指定素材，即颜色查找表图片。注意：一定要用png图片格式！！！
     *           demo用到的滤镜查找表图片位于RTMPAndroidDemo/app/src/main/res/drawable-xxhdpi/目录下。
     */
    public void setFilter(Bitmap bmp) {
        if (mTXLivePusher != null) {
            mTXLivePusher.setFilter(bmp);
        }
    }

    /**
     * 停止摄像头预览
     */
    public synchronized void stopLocalPreview() {
        if (mTXLivePusher != null) {
            mTXLivePusher.setPushListener(null);
            mTXLivePusher.stopCameraPreview(true);
            mTXLivePusher.stopPusher();
        }

        unInitLivePusher();
    }

    /**
     * 切换摄像头
     */
    public void switchCamera() {
        if (mTXLivePusher != null) {
            mTXLivePusher.switchCamera();
        }
    }

    /**
     * 拿到远端流
     */
    public void getRemoteStream() {
        mRoomManager.updatePushers(mHttpRequest,mVideoDisplayListener);
    }

    /**
     * 展示远端流
     * flvUrl  远端流的URL
     */
    public void showRemoteStream(final @NonNull PusherInfo pusherInfo) {
        synchronized (this) {
            if (mPlayers.containsKey(pusherInfo.userID)){
                PlayerItem pusherPlayer = mPlayers.get(pusherInfo.userID);
                if (pusherPlayer.player.isPlaying()){
                    return;
                }else {
                    pusherPlayer = mPlayers.remove(pusherInfo.userID);
                    pusherPlayer.destroy();
                }
            }

            final TXLivePlayer player = new TXLivePlayer(mContext);
            remote_video_view.setVisibility(View.VISIBLE);
            player.setPlayerView(remote_video_view);
            player.enableHardwareDecode(true);
            PlayerItem pusherPlayer = new PlayerItem(remote_video_view, pusherInfo, player);
            mPlayers.put(pusherInfo.userID, pusherPlayer);

            player.setPlayListener(new ITXLivePlayListener() {
                @Override
                public void onPlayEvent(int event, Bundle param) {
                    if (event == TXLiveConstants.PLAY_EVT_PLAY_END || event == TXLiveConstants.PLAY_ERR_NET_DISCONNECT){
                        if (mPlayers.containsKey(pusherInfo.userID)) {
                            PlayerItem item = mPlayers.remove(pusherInfo.userID);
                            if (item != null) {
                                item.destroy();
                            }
                        }
                        // 刷新下pushers
                    }
                }

                @Override
                public void onNetStatus(Bundle status) {

                }
            });

            int result = player.startPlay(pusherInfo.accelerateURL, TXLivePlayer.PLAY_TYPE_LIVE_RTMP);
            if (result != 0){
                mVideoDisplayListener.onDebugLog(String.format("[RTCRoom] 播放成员 {%s} 地址 {%s} 失败",
                        pusherInfo.userID, pusherInfo.accelerateURL));
            }
        }
    }

    public void exitIMLogin(){
        //1. 应用层结束播放所有的流

        //2. 结束心跳
        mHeartBeatThread.stopHeartbeat();

        //3. 结束本地推流
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stopLocalPreview();
            }
        });

        //4. 关闭所有播放器，清理房间信息
        for (Map.Entry<String, PlayerItem> entry : mPlayers.entrySet()) {
            entry.getValue().destroy();
        }
        mPlayers.clear();

        //5. 调用IM的quitGroup
        TIMManager.getInstance().logout(new TIMCallBack() {
            @Override
            public void onError(int code, String desc) {

                //错误码code和错误描述desc，可用于定位请求失败原因
                //错误码code列表请参见错误码表
                mVideoDisplayListener.exitIMFailure(code,desc);
            }

            @Override
            public void onSuccess() {
                //登出成功
                mVideoDisplayListener.onDebugLog("IM logout success");
            }
        });
    }


    /**
     * 离开房间
     */
    public void exitRoom() {

        //1. 应用层结束播放所有的流

        //2. 结束心跳
        mHeartBeatThread.stopHeartbeat();

        //3. 结束本地推流
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stopLocalPreview();
            }
        });

        //4. 关闭所有播放器，清理房间信息
        for (Map.Entry<String, PlayerItem> entry : mPlayers.entrySet()) {
            entry.getValue().destroy();
        }
        mPlayers.clear();

        //5. 调用IM的quitGroup
        TIMManager.getInstance().logout(new TIMCallBack() {
            @Override
            public void onError(int code, String desc) {

                //错误码code和错误描述desc，可用于定位请求失败原因
                //错误码code列表请参见错误码表
                mVideoDisplayListener.exitIMFailure(code,desc);
            }

            @Override
            public void onSuccess() {
                //登出成功
                mVideoDisplayListener.onDebugLog("IM logout success");
            }
        });

        //6. 退出房间：请求CGI:delete_pusher，把自己从房间成员列表里删除（后台会判断如果是房间创建者退出房间，则会直接解散房间）
        mHttpRequest.delPusher(mRoomManager.getRoomId(), mRoomManager.getSelfUserID(), new HttpRequests.OnResponseCallback<HttpResponse>() {
            @Override
            public void onResponse(int retcode, @Nullable String retmsg, @Nullable HttpResponse data) {
                if (retcode == HttpResponse.CODE_OK || retcode == 5) {
                    mRoomManager.setState(RoomState.Empty);
                    mVideoDisplayListener.onDebugLog(String.format("[RTCRoom] UserID{%s} 退出房间 {%s}  成功", mRoomManager.getSelfUserID(), mRoomManager.getRoomId()));
                    mVideoDisplayListener.exitRoomSuccess();
                }
                else {
                    mVideoDisplayListener.onDebugLog(String.format("[RTCRoom] UserID{%s} 退出房间 {%s}  失败", mRoomManager.getSelfUserID(), mRoomManager.getRoomId()));
                    mVideoDisplayListener.exitRoomFailure(retcode,retmsg);
                }
            }
        });

        //清理roomManager
        mRoomManager.clean();
    }

    public TXLivePusher getLivePusher() {
        return mTXLivePusher;
    }

    private class TXLivePushListenerImpl implements ITXLivePushListener {
        private boolean mCameraEnable = true;
        private boolean mMicEnable = true;

        public boolean cameraEnable() {
            return mCameraEnable;
        }

        public boolean micEnable() {
            return mMicEnable;
        }

        @Override
        public void onPushEvent(int event, Bundle param) {
            if (event == TXLiveConstants.PUSH_EVT_PUSH_BEGIN) {
                mVideoDisplayListener.onDebugLog("[RTCRoom] 推流成功");
                mVideoDisplayListener.pushStreamSuccess();
            } else if (event == TXLiveConstants.PUSH_ERR_OPEN_CAMERA_FAIL) {
                mCameraEnable = false;
                mVideoDisplayListener.onDebugLog("[RTCRoom] 推流失败：打开摄像头失败");
                mVideoDisplayListener.pushStreamFailure(-1, "获取摄像头权限失败，请前往隐私-相机设置里面打开应用权限");
            } else if (event == TXLiveConstants.PUSH_ERR_OPEN_MIC_FAIL) {
                mMicEnable = false;
                mVideoDisplayListener.onDebugLog("[RTCRoom] 推流失败：打开麦克风失败");
                mVideoDisplayListener.pushStreamFailure(-1, "获取麦克风权限失败，请前往隐私-麦克风设置里面打开应用权限");
            } else if (event == TXLiveConstants.PUSH_ERR_NET_DISCONNECT) {
                mVideoDisplayListener.onDebugLog("[LiveRoom] 推流失败：网络断开");
                mVideoDisplayListener.pushStreamFailure(-1, "网络断开，推流失败");
            }
        }

        @Override
        public void onNetStatus(Bundle status) {

        }
    }


    private class HeartBeatThread extends HandlerThread {
        private Handler handler;
        private boolean stopHeartbeat = false;

        public HeartBeatThread() {
            super("RTCHeartBeatThread");
            this.start();
            handler = new Handler(this.getLooper());
        }

        private Runnable heartBeatRunnable = new Runnable() {
            @Override
            public void run() {
                boolean b = mHttpRequest.heartBeat(mRoomManager.selfUserID, mRoomManager.roomId);
                if (b || !stopHeartbeat){
                    handler.postDelayed(heartBeatRunnable, 5000);
                }
                stopHeartbeat = false;
            }
        };

        private void startHeartbeart(){
            stopHeartbeat();
            mVideoDisplayListener.onDebugLog(String.format("start userID {%s} heartbeat", mRoomManager.getSelfUserID()));
            handler.postDelayed(heartBeatRunnable, 1000);
        }

        private void stopHeartbeat(){
            mVideoDisplayListener.onDebugLog(String.format("stop userID {%s} heartbeat", mRoomManager.getSelfUserID()));
            stopHeartbeat = true;
            handler.removeCallbacks(heartBeatRunnable);
        }
    }

}
