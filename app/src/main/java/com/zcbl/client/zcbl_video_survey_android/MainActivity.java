package com.zcbl.client.zcbl_video_survey_android;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.zcbl.client.zcbl_video_survey_library.ZCBLSDK;
import com.zcbl.client.zcbl_video_survey_library.bean.ZCBLRequireParamsModel;


public class MainActivity extends AppCompatActivity {

    private TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text = (TextView) findViewById(R.id.text);
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String siSurveyNo = "b1c0b8350beb488db3857661b14e4f57";
                final String phoneNum = "837afe94-e4cb-4bd4-acdf-6c6cbc936045";
                final String caseAddress = "北京市天安门广场";
                ZCBLRequireParamsModel surveyModel = new ZCBLRequireParamsModel();
                surveyModel.setSiSurveyNo(siSurveyNo);
                surveyModel.setPhoneNum(phoneNum);
                surveyModel.setCaseAddress(caseAddress);
                surveyModel.setLatitude("90.000");
                surveyModel.setLongitude("125.000");
                ZCBLSDK.goToVideoSurvey(MainActivity.this,surveyModel);
            }
        });
    }
}
