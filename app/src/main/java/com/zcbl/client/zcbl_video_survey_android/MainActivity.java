package com.zcbl.client.zcbl_video_survey_android;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.zcbl.client.zcbl_video_survey_library.ZCBLSDK;
import com.zcbl.client.zcbl_video_survey_library.bean.ZCBLVideoSurveyModel;


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
//                Intent intent = new Intent(MainActivity.this, ZCBLVideoSurveyConnectTransionActivity.class);
//                startActivity(intent);
                ZCBLVideoSurveyModel surveyModel = new ZCBLVideoSurveyModel();
                ZCBLSDK.goToVideoSurvey(MainActivity.this,surveyModel);
            }
        });
    }
}
