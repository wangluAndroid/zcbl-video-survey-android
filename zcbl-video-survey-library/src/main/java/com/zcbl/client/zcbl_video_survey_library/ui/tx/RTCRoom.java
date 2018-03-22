package com.zcbl.client.zcbl_video_survey_library.ui.tx;

import android.app.Activity;
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
import android.util.Log;
import android.view.View;

import com.tencent.imsdk.TIMCallBack;
import com.tencent.imsdk.TIMElem;
import com.tencent.imsdk.TIMElemType;
import com.tencent.imsdk.TIMManager;
import com.tencent.imsdk.TIMMessage;
import com.tencent.imsdk.TIMMessageListener;
import com.tencent.imsdk.TIMTextElem;
import com.tencent.rtmp.ITXLivePushListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePushConfig;
import com.tencent.rtmp.TXLivePusher;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.zcbl.client.zcbl_video_survey_library.R;
import com.zcbl.client.zcbl_video_survey_library.ZCBLConstants;
import com.zcbl.client.zcbl_video_survey_library.ui.tx.bean.PusherInfo;
import com.zcbl.client.zcbl_video_survey_library.ui.tx.bean.RoomManager;
import com.zcbl.client.zcbl_video_survey_library.ui.tx.bean.RoomState;
import com.zcbl.client.zcbl_video_survey_library.ui.tx.bean.SelfAccountInfo;
import com.zcbl.client.zcbl_video_survey_library.ui.tx.http.HttpRequests;
import com.zcbl.client.zcbl_video_survey_library.ui.tx.http.HttpResponse;

import java.util.List;

/**
 * Created by serenitynanian on 2018/3/20.
 */

public class RTCRoom{

    private final HttpRequests mHttpRequest;
    private final HeartBeatThread mHeartBeatThread;
    private int mBeautyStyle = TXLiveConstants.BEAUTY_STYLE_SMOOTH;
    private int mBeautyLevel = 5;
    private int mWhiteningLevel = 5;
    private int mRuddyLevel = 5;

    public static int RTCROOM_VIDEO_RATIO_9_16    = 1; //视频分辨率为9:16
    public static int RTCROOM_VIDEO_RATIO_3_4     = 2; //视频分辨率为3:4
    public static int RTCROOM_VIDEO_RATIO_1_1     = 3; //视频分辨率为1:1


    private RoomManager mRoomManager = new RoomManager();


    private boolean enableOneKeyBeauty = true;

    private RTCDoubleRoomActivityInterface myInterface;


    private Handler mHandler;
    private Context mContext;
    private TXLivePusher mTXLivePusher;
    private TXLivePushListenerImpl mTXLivePushListener;

    private RoomListenerCallback        roomListenerCallback;

    /**
     * RTCRoom 初始化Callback
     */
    public interface InitCallback  {
        void onError(int errCode, String errInfo);
        void onSuccess(String userId);
    }

    public RTCRoom(Context context) {
        mContext = context;
        myInterface = (RTCDoubleRoomActivityInterface) context;
        mHandler = new Handler(Looper.getMainLooper());
        roomListenerCallback = new RoomListenerCallback(null);
        mHttpRequest = new HttpRequests("https://jiw5ccnh.qcloud.la/weapp/double_room");
        mHeartBeatThread = new HeartBeatThread();
    }


    public void init(String domain, SelfAccountInfo selfAccountInfo, TXCloudVideoView rtmproom_video_local, final InitCallback initCallback) {

        final String selfUserID   = selfAccountInfo.userID;
        final String selfUserName = selfAccountInfo.userName;
        final String selfUserSig  = selfAccountInfo.userSig;
        final String selfHeadPic  = selfAccountInfo.userAvatar;
        final int selfAppID       = selfAccountInfo.sdkAppID;


        mRoomManager.setState(RoomState.Absent);
        mRoomManager.setSelfUserID(selfUserID);
        mRoomManager.setSelfUserName(selfUserName);
        mRoomManager.setAvatarUrl(selfHeadPic);

//        initCallback.onSuccess(selfUserID);

        //IM 初始化成功后 开始设置camera影像到界面上
        //展示本地流到手机界面TXCloudVideoView上
        showLocalStreamToView(rtmproom_video_local);


        //设置消息监听
        TIMManager.getInstance().addMessageListener(new TIMMessageListener() {
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
                        Log.e(ZCBLConstants.TAG, "onNewMessages:---sender "+msg.getSender() );

                        String text = ((TIMTextElem) elem).getText();


                        Log.e(ZCBLConstants.TAG, "onNewMessages:---sender "+text );

                    } else if (elemType == TIMElemType.Image) {
                        //处理图片消息
                    }
                return true; //返回true将终止回调链，不再调用下一个新消息监听器
            }
        });

        String ss = "eJxtjUFPgzAAhf9LrxpXWmDUZIcxFoKxGizC3KXBtUCDZQ10Ccb434cTs4vX9733vi*QPbK70hgleGk57gW4B44LIfR9EmBw*8sPh*Ops9x*GjlxhDD2Z6SE7KyqlOwncBpkzx0POR72IcEBdLmLl3NzEC2-iP4zWKV-nq-bOZejUb3kZWUvAuQRNC3-HlU9ZXT7uknSaFEUWVaMdRO96COFTIldm*gur94bty0Ni5p6E261GvbrpN6HNI2fZcwequBm-eEkaUjI6DFE88XbDkkt0XLMoYyf6Ap8nwFoHlXL";
        // identifier为用户名，userSig 为用户登录凭证
        TIMManager.getInstance().login("user_1521536093804_437", ss, new TIMCallBack() {
            @Override
            public void onError(int code, String desc) {
                //错误码code和错误描述desc，可用于定位请求失败原因
                //错误码code列表请参见错误码表
                Log.e(ZCBLConstants.TAG, "login failed. code: " + code + " errmsg: " + desc);
                initCallback.onError(code,desc);
            }

            @Override
            public void onSuccess() {
                Log.e(ZCBLConstants.TAG, "login succ");
                initCallback.onSuccess(selfUserID);

                createRoom("888", new CreateRoomCallback() {
                    @Override
                    public void onError(int errCode, String errInfo) {
                        Log.e(ZCBLConstants.TAG, "onError:----创建房间失败 ");
                    }

                    @Override
                    public void onSuccess(String roomId) {
                        Log.e(ZCBLConstants.TAG, "onSuccess:----创建房间成功---roomId---->"+roomId);
                    }
                });
            }
        });


    }

    /**
     * 展示本地流到手机界面TXCloudVideoView上
     * @param rtmproom_video_local
     */
    private void showLocalStreamToView(TXCloudVideoView rtmproom_video_local) {
        startLocalPreview(rtmproom_video_local);
        setPauseImage(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.pause_publish));
        setBitrateRange(400, 800);
        setVideoRatio(RTCRoom.RTCROOM_VIDEO_RATIO_3_4);
        setHDAudio(true);
        setBeautyFilter(mBeautyStyle, mBeautyLevel, mWhiteningLevel, mRuddyLevel);
    }

    /**
     * RTCRoom 创建房间Callback
     */
    public interface CreateRoomCallback {
        void onError(int errCode, String errInfo);
        void onSuccess(String name);
    }

    public void createRoom(final String roomName,final CreateRoomCallback callback) {
        //1. 在应用层调用startLocalPreview，启动本地预览
        //2. 请求CGI:get_push_url，异步获取到推流地址pushUrl
        mHttpRequest.getPushUrl(mRoomManager.getSelfUserID(), new HttpRequests.OnResponseCallback<HttpResponse.PushUrl>() {
            @Override
            public void onResponse(int retcode, @Nullable String retmsg, @Nullable HttpResponse.PushUrl data) {
                if (retcode == HttpResponse.CODE_OK && data != null && data.pushURL != null) {
                    final String pushURL = data.pushURL;

                    //3.开始推流
                    startPushStream(pushURL, new PusherStreamCallback() {
                        @Override
                        public void onError(int errCode, String errInfo) {
                            callback.onError(errCode, errInfo);
                        }

                        @Override
                        public void onSuccess() {
                            //推流过程中，可能会重复收到PUSH_EVT_PUSH_BEGIN事件，onSuccess可能会被回调多次，如果已经创建的房间，直接返回
                            String roomID = mRoomManager.getRoomId();
                            if (roomID != null && roomID.length() > 0) {
                                return;
                            }
                            //4.推流成功，请求CGI:create_room
                            doCreateRoom(pushURL, new CreateRoomCallback() {
                                @Override
                                public void onError(int errCode, String errInfo) {
                                    callback.onError(errCode, errInfo);
                                }

                                @Override
                                public void onSuccess(final String roomId) {

                                    callback.onSuccess(roomId);
                                    mRoomManager.setRoomId(roomId);
                                    mRoomManager.setState(RoomState.Created);

                                    //5.调用IM的joinGroup，参数是roomId（roomId就是groupId）
                                    mHeartBeatThread.startHeartbeart(); //启动心跳

                                }
                            });
                        }
                    });

                }
                else {
                    callback.onError(retcode, "获取推流地址失败");
                }
            }
        });

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


    private void doCreateRoom(final String pushURL, final CreateRoomCallback callback){
        final String userId = mRoomManager.getSelfUserID();
        final String userName = mRoomManager.getSelfUserName();
        final String avatarUrl = mRoomManager.getAvatarUrl();
        final String roomName = mRoomManager.getRoomName();

        mHttpRequest.createRoom(userId, roomName, userName, avatarUrl, pushURL, new HttpRequests.OnResponseCallback<HttpResponse.CreateRoom>() {
            @Override
            public void onResponse(int retcode, @Nullable String retmsg, @Nullable HttpResponse.CreateRoom data) {
                if (retcode != HttpResponse.CODE_OK || data == null || data.roomID == null) {
                    roomListenerCallback.onDebugLog("[RTCRoom] 创建会话错误： " + retmsg);
                    callback.onError(retcode, retmsg);
                } else {

                    mRoomManager.setState(RoomState.Created | RoomState.Empty);
                    mRoomManager.setRoomId(data.roomID);
                    mRoomManager.setRoomName(roomName);

                    roomListenerCallback.printLog("[RTCRoom] 创建会话 ID{%s} 成功 ", data.roomID);
                    callback.onSuccess(data.roomID);
                }
            }//onResponse
        });
    }

    private void startPushStream(final String url, final PusherStreamCallback callback){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mTXLivePushListener != null) {
                    if (mTXLivePushListener.cameraEnable() == false) {
                        callback.onError(-1, "获取摄像头权限失败，请前往隐私-相机设置里面打开应用权限");
                        return;
                    }
                    if (mTXLivePushListener.micEnable() == false) {
                        callback.onError(-1, "获取摄像头权限失败，请前往隐私-相机设置里面打开应用权限");
                        return;
                    }
                }
                if (mTXLivePusher != null) {
                    roomListenerCallback.printLog("[RTCRoom] 开始推流 PushUrl {%s}", url);
                    mTXLivePushListener.setCallback(callback);
                    mTXLivePusher.startPusher(url);
                }
            }
        });
    }

    /**
     * 设置房间事件回调
     * @param listener
     */
    public void setRTCRoomListener(IRTCRoomListener listener) {
        roomListenerCallback.setRoomMemberEventListener(listener);
    }


    /**
     * 反初始化
     */
    public void unInit() {
        roomListenerCallback.onDebugLog("[RTCRoom] unInit");
        mContext = null;
        mHandler = null;

    }


    private class RoomListenerCallback implements IRTCRoomListener{

        private final Handler handler;
        private IRTCRoomListener roomMemberEventListener;

        public RoomListenerCallback(IRTCRoomListener roomMemberEventListener) {
            this.roomMemberEventListener = roomMemberEventListener;
            handler = new Handler(Looper.getMainLooper());
        }

        public void setRoomMemberEventListener(IRTCRoomListener roomMemberEventListener) {
            this.roomMemberEventListener = roomMemberEventListener;
        }

        @Override
        public void onPusherJoin(final PusherInfo member) {
            printLog("[RTCRoom] onPusherJoin, UserID {%s} PlayUrl {%s}", member.userID, member.accelerateURL);
            new Exception("-----RTCRoom->onPusherJoin------").printStackTrace();
            if (roomMemberEventListener != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        roomMemberEventListener.onPusherJoin(member);
                    }
                });
            }
        }

        @Override
        public void onPusherQuit(final PusherInfo member) {
            printLog("[RTCRoom] onPusherQuit, UserID {%s} PlayUrl {%s}", member.userID, member.accelerateURL);
            if(roomMemberEventListener != null)
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        roomMemberEventListener.onPusherQuit(member);
                    }
                });
        }

        @Override
        public void onRoomClosed(final String roomId) {
            printLog("[RTCRoom] onRoomClosed, RoomId {%s}", roomId);
            if(roomMemberEventListener != null)
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        roomMemberEventListener.onRoomClosed(roomId);
                    }
                });
        }

        public void printLog(String format, Object ...args){
            String line = String.format(format, args);
            onDebugLog(line);
        }
        @Override
        public void onDebugLog(final String line) {
            if(roomMemberEventListener != null)
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        roomMemberEventListener.onDebugLog(line);
                    }
                });
        }

        @Override
        public void onGetPusherList(final List<PusherInfo> pusherInfoList) {
            if(roomMemberEventListener != null)
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        roomMemberEventListener.onGetPusherList(pusherInfoList);
                    }
                });
        }

        @Override
        public void onRecvRoomTextMsg(final String roomId, final String userId, final String userName, final String headPic, final String message) {
            if(roomMemberEventListener != null)
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        roomMemberEventListener.onRecvRoomTextMsg(roomId, userId, userName, headPic, message);
                    }
                });
        }

        @Override
        public void onError(final int errorCode, final String errorMessage) {
            if(roomMemberEventListener != null)
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        roomMemberEventListener.onError(errorCode, errorMessage);
                    }
                });
        }
    }


    /**
     * 启动摄像头预览
     * @param videoView 摄像头预览组件
     */
    public synchronized void startLocalPreview(final @NonNull TXCloudVideoView videoView) {
        roomListenerCallback.onDebugLog("[RTCRoom] startLocalPreview");

        initLivePusher();

        if (mTXLivePusher != null) {
            //将界面元素和pusher对象关联起来，从而能够将手机摄像头采集到的画面渲染到手机屏幕上
            ((Activity)mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    videoView.setVisibility(View.VISIBLE);
                    mTXLivePusher.startCameraPreview(videoView);

                }
            });
        }
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
            mTXLivePusher.setBeautyFilter(TXLiveConstants.BEAUTY_STYLE_SMOOTH, 5, 3, 2);
            mTXLivePusher.setVideoQuality(TXLiveConstants.VIDEO_QUALITY_REALTIEM_VIDEOCHAT, true, true);

            mTXLivePushListener = new TXLivePushListenerImpl();
            mTXLivePusher.setPushListener(mTXLivePushListener);
        }
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



    private interface PusherStreamCallback {
        void onError(int errCode, String errInfo);
        void onSuccess();
    }

    private class TXLivePushListenerImpl implements ITXLivePushListener {
        private boolean mCameraEnable = true;
        private boolean mMicEnable = true;
        private PusherStreamCallback mCallback = null;

        public void setCallback(PusherStreamCallback callback) {
            mCallback = callback;
        }

        public boolean cameraEnable() {
            return mCameraEnable;
        }

        public boolean micEnable() {
            return mMicEnable;
        }

        @Override
        public void onPushEvent(int event, Bundle param) {
            if (event == TXLiveConstants.PUSH_EVT_PUSH_BEGIN) {
                roomListenerCallback.onDebugLog("[RTCRoom] 推流成功");
                if (mCallback != null) {
                    mCallback.onSuccess();
                }
            } else if (event == TXLiveConstants.PUSH_ERR_OPEN_CAMERA_FAIL) {
                mCameraEnable = false;
                roomListenerCallback.onDebugLog("[RTCRoom] 推流失败：打开摄像头失败");
                if (mCallback != null) {
                    mCallback.onError(-1, "获取摄像头权限失败，请前往隐私-相机设置里面打开应用权限");
                }
                else {
                    roomListenerCallback.onError(-1, "获取摄像头权限失败，请前往隐私-相机设置里面打开应用权限");
                }
            } else if (event == TXLiveConstants.PUSH_ERR_OPEN_MIC_FAIL) {
                mMicEnable = false;
                roomListenerCallback.onDebugLog("[RTCRoom] 推流失败：打开麦克风失败");
                if (mCallback != null) {
                    mCallback.onError(-1, "获取麦克风权限失败，请前往隐私-麦克风设置里面打开应用权限");
                }
                else {
                    roomListenerCallback.onError(-1, "获取麦克风权限失败，请前往隐私-麦克风设置里面打开应用权限");
                }
            } else if (event == TXLiveConstants.PUSH_ERR_NET_DISCONNECT) {
                roomListenerCallback.onDebugLog("[LiveRoom] 推流失败：网络断开");
                roomListenerCallback.onError(-1, "网络断开，推流失败");
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
            roomListenerCallback.printLog("start userID {%s} heartbeat", mRoomManager.getSelfUserID());
            handler.postDelayed(heartBeatRunnable, 1000);
        }

        private void stopHeartbeat(){
            roomListenerCallback.printLog("stop userID {%s} heartbeat", mRoomManager.getSelfUserID());
            stopHeartbeat = true;
            handler.removeCallbacks(heartBeatRunnable);
        }
    }

}
