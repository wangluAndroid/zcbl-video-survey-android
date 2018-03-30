package com.zcbl.client.zcbl_video_survey_android;

import android.Manifest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.zcbl.client.zcbl_video_survey_library.bean.ZCBLVideoSurveyModel;
import com.zcbl.client.zcbl_video_survey_library.ui.activity.ZCBLVideoSurveyConnectTransionActivity;
import com.zcbl.client.zcbl_video_survey_library.utils.ZCBLPermissionHelper;


public class MainActivity extends AppCompatActivity {

    private TextView text;
    private ZCBLPermissionHelper mHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions();

        text = (TextView) findViewById(R.id.text);
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                final String phoneNum = "837afe94-e4cb-4bd4-acdf-6c6cbc936045";
//                final String siSurveyNo = "2bead42013e84354ab0db458cdd88416";
//                final String phoneNum = "14111111111";

                final String siSurveyNo = "ba09648301b849d9bcd45dc1effdb2bf";
                final String phoneNum = "18201255299";
                final String caseAddress = "北京市天安门广场";
                //此model为中车视频查勘实体类
//                ZCBLRequireParamsModel surveyModel = new ZCBLRequireParamsModel();
                ZCBLVideoSurveyModel surveyModel = new ZCBLVideoSurveyModel();
                surveyModel.setSiSurveyNo(siSurveyNo);
                surveyModel.setPhoneNum(phoneNum);
                surveyModel.setCaseAddress(caseAddress);
                surveyModel.setLatitude("90.000");
                surveyModel.setLongitude("125.000");
                surveyModel.setNavigatorBarColor("#0d70d8");
//                ZCBLSDK.goToVideoSurvey(MainActivity.this, surveyModel);
                //测试bug统计
//                ZCBLCrashHandler.getInstance().init(MainActivity.this);
//                String str = null ;
//                System.out.println(str.toString());
                Intent intent = new Intent(MainActivity.this, ZCBLVideoSurveyConnectTransionActivity.class);
                intent.putExtra("zcbl_model",surveyModel);
                startActivity(intent);
            }
        });
    }

    private void requestPermissions() {
        int sdk=android.os.Build.VERSION.SDK_INT;
        if (null != mHelper) {
            mHelper = null ;
        }
        if (sdk>=23){
                mHelper = new ZCBLPermissionHelper(this);

                String[] permissions ={Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE};
                if (mHelper.lacksPermissions(permissions)) {
                    mHelper.requestPermissions(permissions); // 请求权限
                } else {

                }

        }else{
            // 全部权限都已获取

        }

    }

}

