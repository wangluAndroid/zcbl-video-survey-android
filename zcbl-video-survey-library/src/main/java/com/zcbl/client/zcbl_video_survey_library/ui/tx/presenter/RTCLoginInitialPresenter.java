package com.zcbl.client.zcbl_video_survey_library.ui.tx.presenter;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.tencent.imsdk.TIMCallBack;
import com.tencent.imsdk.TIMLogLevel;
import com.tencent.imsdk.TIMManager;
import com.tencent.imsdk.TIMSdkConfig;
import com.zcbl.client.zcbl_video_survey_library.BuildConfig;
import com.zcbl.client.zcbl_video_survey_library.ZCBLConstants;
import com.zcbl.client.zcbl_video_survey_library.bean.ZCBLVideoSurveyModel;
import com.zcbl.client.zcbl_video_survey_library.ui.tx.bean.LoginInfoResponse;
import com.zcbl.client.zcbl_video_survey_library.ui.tx.bean.RoomManager;
import com.zcbl.client.zcbl_video_survey_library.ui.tx.bean.RoomState;
import com.zcbl.client.zcbl_video_survey_library.ui.tx.bean.SelfAccountInfo;
import com.zcbl.client.zcbl_video_survey_library.ui.tx.http.HttpRequests;
import com.zcbl.client.zcbl_video_survey_library.ui.tx.http.HttpResponse;
import com.zcbl.client.zcbl_video_survey_library.ui.tx.listener.ILoginInitialListener;

import java.io.IOException;
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

public class RTCLoginInitialPresenter {


    private String userName = "中车宝联App";
    private String avatarUrl = "http://www.zhongchebaolian.com/images/logo.png";

    private final HttpRequests mHttpRequest;
    private RoomManager mRoomManager = new RoomManager();
    private Handler mHandler;
    private Context mContext;

    private SelfAccountInfo selfAccountInfo;
    private ILoginInitialListener loginInitialListener;

    private ZCBLVideoSurveyModel zcblVideoSurveyModel;


    public RTCLoginInitialPresenter(Context context) {
        mContext = context;
        mHttpRequest = HttpRequests.getHttpReqeust(ZCBLConstants.DOMAIN);
    }

    public void setLoginInitialListener(ILoginInitialListener loginInitialListener) {
        this.loginInitialListener = loginInitialListener;
    }


    /**
     * 获取IM登录信息
     */
    public void getImLoginInfo(ZCBLVideoSurveyModel zcblVideoSurveyModel) {
        this.zcblVideoSurveyModel = zcblVideoSurveyModel ;
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .build();

        final MediaType MEDIA_JSON = MediaType.parse("application/json; charset=utf-8");

        final Request request = new Request.Builder()
                .url(ZCBLConstants.DOMAIN.concat("/get_im_login_info"))//合并多个数组；合并多个字符串
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
                        Log.w(ZCBLConstants.TAG,String.format("[Activity]获取登录信息失败{%s}", e.getMessage()));
                        loginInitialListener.getIMLoginInfoFailure(-1,e.getMessage());
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
                        loginInitialListener.getIMLoginInfoFailure(resp.code,resp.message);
                        Log.w(ZCBLConstants.TAG,String.format("[Activity]获取登录信息失败：{%s}", resp.message));
                    }else {
                        final SelfAccountInfo selfAccountInfo = new SelfAccountInfo(
                                resp.userID,
                                userName,
                                avatarUrl,
                                resp.userSig,
                                resp.accType,
                                resp.sdkAppID);
                        loginInitialListener.getIMLoginInfoSuccess(selfAccountInfo);
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
        this.selfAccountInfo = selfAccountInfo ;
        final String selfUserID   = selfAccountInfo.userID;
        final String selfUserSig  = selfAccountInfo.userSig;

        final String selfUserName = selfAccountInfo.userName;
        final String selfHeadPic  = selfAccountInfo.userAvatar;
        final int selfAppID       = selfAccountInfo.sdkAppID;

        mRoomManager.setState(RoomState.Absent);
        mRoomManager.setSelfUserID(selfUserID);
        mRoomManager.setSelfUserName(selfUserName);
        mRoomManager.setAvatarUrl(selfHeadPic);
        mRoomManager.setRoomName("Android");
        mRoomManager.setRoomId(zcblVideoSurveyModel.getVideoRoomId());
        mRoomManager.setLongitude(zcblVideoSurveyModel.getLongitude());
        mRoomManager.setLatitude(zcblVideoSurveyModel.getLatitude());
        mRoomManager.setCaseAddress(zcblVideoSurveyModel.getCaseAddress());
        mRoomManager.setZuoxiIMAccount(zcblVideoSurveyModel.getCustomerServiceImAccount());
        mRoomManager.setSurveyInfo(zcblVideoSurveyModel.getData().toString());

        //IM登录请求
        imLogin();
    }



    /**
     * IM登录
     */
    private void imLogin() {
        //初始化SDK基本配置   1400066983
        TIMSdkConfig config = new TIMSdkConfig(selfAccountInfo.sdkAppID)
                .setLogLevel(TIMLogLevel.DEBUG)
                .enableLogPrint(BuildConfig.DEBUG)
                .setLogPath(Environment.getExternalStorageDirectory().getPath() + "/justfortest/");
        //初始化SDK
        TIMManager.getInstance().init(mContext, config);

        // identifier为用户名，userSig 为用户登录凭证
        TIMManager.getInstance().login(selfAccountInfo.userID, selfAccountInfo.userSig, new TIMCallBack() {
            @Override
            public void onError(int code, String desc) {
                //错误码code和错误描述desc，可用于定位请求失败原因
                //错误码code列表请参见错误码表
                loginInitialListener.iMLoginFailure(code,desc);
            }

            @Override
            public void onSuccess() {
                loginInitialListener.iMLoginSuccess();
                getPushUrl();
            }
        });
    }

    /**
     * 获取推流地址
     */
    private void getPushUrl(){
        mHttpRequest.getPushUrl(mRoomManager.getSelfUserID(), new HttpRequests.OnResponseCallback<HttpResponse.PushUrl>() {
            @Override
            public void onResponse(int retcode, @Nullable String retmsg, @Nullable HttpResponse.PushUrl data) {
                if (retcode == HttpResponse.CODE_OK && data != null && data.pushURL != null) {
                    final String pushURL = data.pushURL;
                    loginInitialListener.getPushUrlSuccess(pushURL);
                    mRoomManager.setPushUrl(pushURL);
                    loginInitialListener.goToNextPage(mRoomManager);
                }else {
                    loginInitialListener.getPushUrlFailure(retcode, "获取推流地址失败");
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

    /**
     * 反初始化
     */
    public void destory() {
        loginInitialListener.onDebugLog("[RTCRoom] unInit");
        mContext = null;
        mHandler = null;
    }


    /**
     * 重新获取IM登录信息
     */
    public void retrygetIMLoginInfoLogin(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
                .setTitle("")
                .setMessage("获取IM登录信息失败，请重试？")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getImLoginInfo(zcblVideoSurveyModel);
                    }
                });
        builder.show();
    }

    /**
     * 重新进行IM登录
     */
    public void retryIMLogin(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
                .setTitle("")
                .setMessage("登录IM失败，请重试？")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        imLogin();
                    }
                });
        builder.show();
    }

    /**
     * 重新获取pushUrl
     */
    public void retryGetPushUrl(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
                .setTitle("")
                .setMessage("获取PushUrl失败，请重试？")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        getPushUrl();
                    }
                });
        builder.show();
    }

    /**
     * 重新创建房间
     */
    public void retryCreateRoom(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
                .setTitle("")
                .setMessage("创建视频房间失败，请重试？")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        createRoom();
                    }
                });
        builder.show();
    }

}
