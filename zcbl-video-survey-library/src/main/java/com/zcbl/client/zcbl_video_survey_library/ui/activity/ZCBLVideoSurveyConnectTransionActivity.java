package com.zcbl.client.zcbl_video_survey_library.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.wilddog.client.ChildEventListener;
import com.wilddog.client.DataSnapshot;
import com.wilddog.client.SyncError;
import com.wilddog.client.SyncReference;
import com.wilddog.client.WilddogSync;
import com.wilddog.wilddogauth.WilddogAuth;
import com.wilddog.wilddogauth.core.Task;
import com.wilddog.wilddogauth.core.listener.OnCompleteListener;
import com.wilddog.wilddogauth.core.result.AuthResult;
import com.zcbl.client.zcbl_video_survey_library.R;
import com.zcbl.client.zcbl_video_survey_library.ZCBLConstants;
import com.zcbl.client.zcbl_video_survey_library.bean.ZCBLVideoSurveyModel;
import com.zcbl.client.zcbl_video_survey_library.service.UpdateCallbackInterface;
import com.zcbl.client.zcbl_video_survey_library.service.ZCBLHttpUtils;
import com.zcbl.client.zcbl_video_survey_library.service.ZCBLToastUtils;
import com.zcbl.client.zcbl_video_survey_library.ui.customview.ZCBLCustomLoadingDialogManager;
import com.zcbl.client.zcbl_video_survey_library.utils.ZCBLPermissionHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;



/**
 * Created by serenitynanian on 2018/1/11.
 * 视频查勘过渡页面
 * 初始化查勘所需资源
 */
public class ZCBLVideoSurveyConnectTransionActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView iv_goback;
    private TextView tv_descption;
    private ImageView iv_transition_image ;
    private Button but_start_video ;
    private ImageView video_transition_tel;
    private static final String PACKAGE_URL_SCHEME = "package:"; // 方案
    private static final String TAG = "ZCBL_WilddogVideoModule";
    private ZCBLPermissionHelper mHelper; // 权限检测器
    private boolean isRequireCheck = true; // 是否需要系统权限检测

    private SyncReference ref;

    private ZCBLVideoSurveyModel zcblVideoSurveyModel;
    private String siSurveyNo ;
    private String phoneNum ;
    private String syncVideoConnectCommandNodePath ;
    private String customerServicePhone;

    private ChildEventListener childEventListener;

    private ZCBLCustomLoadingDialogManager loading_dialog;
    private CountDownTimer timer = new CountDownTimer(16*1000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            but_start_video.setEnabled(false);
            but_start_video.setBackgroundResource(R.drawable.button_disable);
            but_start_video.setTextColor(getResources().getColor(R.color.color_ffffff));
            but_start_video.setText("坐席繁忙，请稍后重试  "+ millisUntilFinished / 1000 + "s");
            tv_descption.setText(R.string.text_zuoxi_busy);
            video_transition_title.setText(R.string.text_zuoxi_busy);
            iv_transition_image.setImageResource(R.drawable.ic_zuoxi_busy);
        }

        @Override
        public void onFinish() {
            resumeStatus();
        }
    };

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            Log.e(TAG, "视频建立连接异常，无法正常连接，35sAPP端重置");
            if (1 == what) {
                startCountdownAndDismissLoading();
                removeSync();
                ZCBLToastUtils.showToast(ZCBLVideoSurveyConnectTransionActivity.this,"坐席繁忙，请稍后重试");
            }
        }
    };
    private TextView video_transition_title;


    /**
     * 初始化生命周期方法
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_survey_connect_transition);
        initView();
        initData();
        requestPermissions(false);
    }

    private void initData() {
        Intent intent = getIntent();
        zcblVideoSurveyModel = (ZCBLVideoSurveyModel) intent.getSerializableExtra("zcbl_model");
        siSurveyNo = zcblVideoSurveyModel.getSiSurveyNo();
        phoneNum = zcblVideoSurveyModel.getPhoneNum();
    }

    private void initView() {
        iv_goback = (ImageView) findViewById(R.id.video_transition_goback);
        iv_transition_image = (ImageView) findViewById(R.id.video_transition_image);
        tv_descption = (TextView) findViewById(R.id.video_transition_description);
        but_start_video = (Button)findViewById(R.id.video_transition_commit);
        video_transition_tel = (ImageView) findViewById(R.id.video_transition_tel);
        video_transition_title = (TextView) findViewById(R.id.video_transition_title);
        video_transition_tel.setOnClickListener(this);
        iv_goback.setOnClickListener(this);
        but_start_video.setOnClickListener(this);

    }


    private void goToVideoroom(){
        WilddogAuth auth = WilddogAuth.getInstance();
        auth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(Task<AuthResult> var1) {
                if (var1.isSuccessful()) {
                    resumeStatus();
                    dismissLoading();
                    Intent intent = new Intent(ZCBLVideoSurveyConnectTransionActivity.this, ZCBLVideoSurveyActivity.class);
                    intent.putExtra("ZCBLVideoSurveyModel", (Serializable) zcblVideoSurveyModel);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    ZCBLVideoSurveyConnectTransionActivity.this.startActivityForResult(intent, ZCBLConstants.GO_TO_VIDEO_ROOM);
                } else {
                    startCountdownAndDismissLoading();
                    ZCBLToastUtils.showToast(ZCBLVideoSurveyConnectTransionActivity.this, "登录失败,请查看日志寻找失败原因");
                    Log.e("error", var1.getException().getMessage());
                }
            }
        });
    }




    private void requestPermissions(boolean isClick) {
        int sdk=android.os.Build.VERSION.SDK_INT;
        if (null != mHelper) {
            mHelper = null ;
        }
        mHelper = new ZCBLPermissionHelper(this);
        if (sdk>=23){
            if (isRequireCheck) {
                String[] permissions = ZCBLConstants.PERMISSIONS;
                if (mHelper.lacksPermissions(permissions)) {
                    mHelper.requestPermissions(permissions); // 请求权限
                } else {
                     // 全部权限都已获取
                    if(isClick){
                        requestConnect();
                    }
                }
            } else {
                isRequireCheck = true;
            }
        }else{
            // 全部权限都已获取
            if(isClick){
                requestConnect();
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ZCBLPermissionHelper.PERMISSION_REQUEST_CODE && hasAllPermissionsGranted(grantResults)) {
            isRequireCheck = true;
//            requestConnect();
        } else {
            isRequireCheck = false;
            showMissingPermissionDialog();
        }
    }

    private void requestConnect() {
        showLoading();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("surveyNo",siSurveyNo);
            jsonObject.put("accesstoken",phoneNum );
            jsonObject.put("useApp",0);//0:其他第三方app  1：自己的app
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ZCBLHttpUtils.getInstance().post(ZCBLConstants.VIDEO_CONNECTION_URL,jsonObject, new UpdateCallbackInterface() {
            @Override
            public void onError(final String error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startCountdownAndDismissLoading();
                        ZCBLToastUtils.showToast(ZCBLVideoSurveyConnectTransionActivity.this,error);
                    }
                });
            }

            @Override
            public void onSuccess(final String response) {
                ZCBLVideoSurveyConnectTransionActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject obj = new JSONObject(response);
                            JSONObject data = obj.optJSONObject("data");
                            int bindIdleCode = data.optInt("bindIdleCode");
                            if(200 == bindIdleCode){
                                String syncCommandNodePath = data.optString("syncCommandNodePath");
                                syncVideoConnectCommandNodePath = data.optString("syncVideoConnectCommandNodePath");
                                String videoRoomId = data.optString("videoRoomId");
                                zcblVideoSurveyModel.setVideoRoomId(videoRoomId);
                                zcblVideoSurveyModel.setSyncCommandNodePath(syncCommandNodePath);
                                zcblVideoSurveyModel.setSyncVideoConnectCommandNodePath(syncVideoConnectCommandNodePath);
                                addWilddogListener();
                            }else{
                                startCountdownAndDismissLoading();
                                ZCBLToastUtils.showToast(ZCBLVideoSurveyConnectTransionActivity.this,"坐席繁忙，请稍后重试");
                            }
                            customerServicePhone = obj.optString("customerServicePhone");
                            if (!TextUtils.isEmpty(customerServicePhone)) {
                                video_transition_tel.setVisibility(View.VISIBLE);
                            }
                        } catch (JSONException e) {
                            startCountdownAndDismissLoading();
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    // 含有全部的权限
    private boolean hasAllPermissionsGranted(@NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }



    private void addWilddogListener() {
        if (null != handler) {
            handler.sendEmptyMessageDelayed(1, 35 * 1000);
        }
        Log.i(TAG, "syncVideoConnectCommandNodePath: "+syncVideoConnectCommandNodePath);
        ref = WilddogSync.getInstance().getReference(syncVideoConnectCommandNodePath);
        childEventListener = ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String type = dataSnapshot.getValue().toString();
                if (!TextUtils.isEmpty(type)) {
                    if ("WEB$$goToConnection".equals(type) ) {
                        if (null != handler) {
                            handler.removeMessages(1);
                        }
                        removeSync();
                        goToVideoroom();
                        Log.i(ZCBLConstants.TAG,"----------goToConnection----1------->");
                    } else if ("WEB$$refuseConnection".equals(type)) {
                        if (null != handler) {
                            handler.removeMessages(1);
                        }
                        startCountdownAndDismissLoading();
                        removeSync();
                        ZCBLToastUtils.showToast(ZCBLVideoSurveyConnectTransionActivity.this,"坐席繁忙，请稍后重试");
                        Log.i(ZCBLConstants.TAG,"-----------refuseConnection---------------");
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

    /**
     * 启动倒计时 隐藏加载框
     */
    private void startCountdownAndDismissLoading() {
        if (null != timer) {
            timer.start();
        }
        dismissLoading();
    }

    /**
     * 移除监听
     */
    public void removeSync() {
        if (null != ref) {
            if (null != childEventListener) {
                ref.removeEventListener(childEventListener);
            }
            ref = null ;
        }
    }


    // 显示缺失权限提示
    private void showMissingPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("帮助");
        builder.setMessage("当前应用缺少必要权限");

        // 拒绝, 退出应用
        builder.setNegativeButton("退出", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isRequireCheck = true ;
//                finish();

            }
        });

        builder.setPositiveButton("设置", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isRequireCheck = true ;
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse(PACKAGE_URL_SCHEME + ZCBLVideoSurveyConnectTransionActivity.this.getPackageName()));
                ZCBLVideoSurveyConnectTransionActivity.this.startActivity(intent);
            }
        });

        builder.show();
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.video_transition_goback) {
            finish();
        } else if (id == R.id.video_transition_commit) {
            // 发起视频连线
            requestPermissions(true);
        } else if (id == R.id.video_transition_tel) {
            //呼客服
            Intent intent = new Intent(Intent.ACTION_DIAL);
            Uri data = Uri.parse("tel:" + customerServicePhone);
            intent.setData(data);
            startActivity(intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != loading_dialog) {
            if (loading_dialog.isShowing()) {
                loading_dialog.dismissDialog();
            }
            loading_dialog = null ;
        }

        isRequireCheck = true ;

        if(null != handler){
            handler.removeMessages(1);
            handler = null ;
        }
    }

    private void showLoading(){
//        iv_transition_image.setImageResource(R.drawable.ic_zuoxi_busy);
        tv_descption.setText(R.string.text_zuoxi_connecting);
        but_start_video.setBackgroundResource(R.drawable.button_disable);
        but_start_video.setTextColor(getResources().getColor(R.color.color_ffffff));
        video_transition_title.setText(R.string.text_zuoxi_connecting);
        if(null == loading_dialog){
            loading_dialog = new ZCBLCustomLoadingDialogManager(this).initDialog().showDialog();
        }
        if(null!= loading_dialog && !loading_dialog.isShowing()){
            loading_dialog.showDialog();
        }
    }
    private void dismissLoading(){

        if (null != loading_dialog) {
            loading_dialog.dismissDialog();
        }
    }

    /**
     * 恢复UI初始状态
     */
    private void resumeStatus(){
        iv_transition_image.setImageResource(R.drawable.ic_zuoxi_free);
        but_start_video.setEnabled(true);
        but_start_video.setText(R.string.but_zuoxi_free);
        but_start_video.setBackgroundResource(R.drawable.ripple_selector_button);
        tv_descption.setText(R.string.text_zuoxi_free);
        video_transition_title.setText(R.string.title_video_connection);
    }
}
