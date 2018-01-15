package com.zcbl.client.zcbl_video_survey_library;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
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
import com.zcbl.client.zcbl_video_survey_library.bean.ZCBLVideoSurveyModel;
import com.zcbl.client.zcbl_video_survey_library.service.ZCBLHttpUtils;
import com.zcbl.client.zcbl_video_survey_library.service.ZCBLToastUtils;
import com.zcbl.client.zcbl_video_survey_library.ui.activity.ZCBLVideoSurveyActivity;
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
    private static final String PACKAGE_URL_SCHEME = "package:"; // 方案
    private static final String TAG = "ZCBL_WilddogVideoModule";
    private ZCBLPermissionHelper mHelper; // 权限检测器
    private boolean isRequireCheck = true; // 是否需要系统权限检测

    private SyncReference ref;

    private ZCBLVideoSurveyModel ZCBLVideoSurveyModel;
    private String siSurveyNo ;
    private String phoneNum ;
    private String carNum ;
    private double longitude ;
    private double latitude ;
    private String caseAddress ;
    private String videoRoomId ;
    private String syncCommandNodePath ;
    private String syncVideoConnectCommandNodePath ;

    private ChildEventListener childEventListener;

    private ZCBLCustomLoadingDialogManager loading_dialog;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            Log.e(TAG, "视频建立连接异常，无法正常连接，35sAPP端重置");
            if (1 == what) {
                removeSync();
                dismissLoading();
                ZCBLToastUtils.showToast(ZCBLVideoSurveyConnectTransionActivity.this,"坐席繁忙，请稍后重试");
            }
        }
    };

    /**
     * 初始化生命周期方法
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_survey_connect_transition);
        initView();
        requestPermissions();
    }

    private void initView() {
        iv_goback = (ImageView) findViewById(R.id.video_transition_goback);
        iv_transition_image = (ImageView) findViewById(R.id.video_transition_image);
        tv_descption = (TextView) findViewById(R.id.video_transition_description);
        but_start_video = (Button)findViewById(R.id.video_transition_commit);
        iv_goback.setOnClickListener(this);
        but_start_video.setOnClickListener(this);

    }


    private void goToVideoroom(){
        WilddogAuth auth = WilddogAuth.getInstance();
        auth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(Task<AuthResult> var1) {
                if (var1.isSuccessful()) {
                    dismissLoading();
                    Intent intent = new Intent(ZCBLVideoSurveyConnectTransionActivity.this, ZCBLVideoSurveyActivity.class);
                    intent.putExtra("ZCBLVideoSurveyModel", (Serializable) ZCBLVideoSurveyModel);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    ZCBLVideoSurveyConnectTransionActivity.this.startActivityForResult(intent, ZCBLConstants.GO_TO_VIDEO_ROOM);
                } else {
                    dismissLoading();
                    ZCBLToastUtils.showToast(ZCBLVideoSurveyConnectTransionActivity.this, "登录失败,请查看日志寻找失败原因");
                    Log.e("error", var1.getException().getMessage());
                }
            }
        });
    }




    private void requestPermissions() {
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
                    requestConnect();
                }
            } else {
                isRequireCheck = true;
            }
        }else{
            // 全部权限都已获取
            requestConnect();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ZCBLPermissionHelper.PERMISSION_REQUEST_CODE && hasAllPermissionsGranted(grantResults)) {
            isRequireCheck = true;
            // TODO: 2018/1/12 进行视频token请求 然后请求连接视频
            requestConnect();
        } else {
            isRequireCheck = false;
            showMissingPermissionDialog();
        }
    }

    private void requestConnect() {
        showLoading();
        final String siSurveyNo = "b1c0b8350beb488db3857661b14e4f57";
        String token = "837afe94-e4cb-4bd4-acdf-6c6cbc936045";
        String time = "2018-01-12 12:05:11";
        String sign = "445CDED75E57B7C3F53639A2741FF0F4FE7BC614";
        final String phoneNum = "18201255299";
        final String carNum = "京CCC112";
        final String caseAddress = "android sdk";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("surveyNo",siSurveyNo);
            jsonObject.put("accesstoken",token );
            jsonObject.put("timestamp", time);
            jsonObject.put("sign", sign);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ZCBLHttpUtils.getInstance().post(ZCBLConstants.VIDEO_CONNECTION_URL,jsonObject, new ZCBLHttpUtils.UpdateCallback() {
            @Override
            public void onError(String error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissLoading();
                    }
                });
            }

            @Override
            public void onSuccess(final String response) {
                ZCBLVideoSurveyConnectTransionActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG,"--------视频请求连接---response---------->" + response);
                        try {
                            JSONObject obj = new JSONObject(response);
                            JSONObject data = obj.optJSONObject("data");
                            int bindIdleCode = data.optInt("bindIdleCode");
                            if(200 == bindIdleCode){
                                syncCommandNodePath = data.optString("syncCommandNodePath");
                                syncVideoConnectCommandNodePath = data.optString("syncVideoConnectCommandNodePath");
                                videoRoomId = data.optString("videoRoomId");
                                ZCBLVideoSurveyModel = new ZCBLVideoSurveyModel(siSurveyNo,phoneNum,carNum,longitude+"100",latitude+"90",caseAddress,
                                        videoRoomId,syncCommandNodePath,syncVideoConnectCommandNodePath);
                                addWilddogListener();
                            }else{
                                dismissLoading();
                                ZCBLToastUtils.showToast(ZCBLVideoSurveyConnectTransionActivity.this,"坐席繁忙，请稍后重试");
                            }
                        } catch (JSONException e) {
                            dismissLoading();
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
                Log.i(TAG, "onChildAdded: "+type);
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
                        dismissLoading();
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
            // TODO: 2018/1/12 发起视频连线
            requestConnect();
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
        iv_transition_image.setImageResource(R.drawable.ic_zuoxi_busy);
        tv_descption.setText(R.string.text_zuoxi_busy);
        but_start_video.setBackgroundResource(R.drawable.button_disable);
        but_start_video.setTextColor(getResources().getColor(R.color.color_ffffff));
        if(null == loading_dialog){
            loading_dialog = new ZCBLCustomLoadingDialogManager(this).initDialog().showDialog();
        }
        if(null!= loading_dialog && !loading_dialog.isShowing()){
            loading_dialog.showDialog();
        }
    }
    private void dismissLoading(){
        iv_transition_image.setImageResource(R.drawable.ic_zuoxi_free);
        tv_descption.setText(R.string.text_zuoxi_free);
        but_start_video.setBackgroundResource(R.drawable.ripple_selector_button);
        if (null != loading_dialog) {
            loading_dialog.dismissDialog();
        }
    }
}
