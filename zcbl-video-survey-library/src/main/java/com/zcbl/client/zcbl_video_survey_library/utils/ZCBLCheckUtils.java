package com.zcbl.client.zcbl_video_survey_library.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.zcbl.client.zcbl_video_survey_library.ZCBLConstants;
import com.zcbl.client.zcbl_video_survey_library.bean.ZCBLRequireParamsModel;
import com.zcbl.client.zcbl_video_survey_library.bean.ZCBLVideoSurveyModel;
import com.zcbl.client.zcbl_video_survey_library.service.ZCBLToastUtils;

/**
 * Created by serenitynanian on 2018/1/15.
 */

public class ZCBLCheckUtils {

    public static boolean checkStringEmpty(String param){
        if(TextUtils.isEmpty(param)){
            return true;
        }
        return false ;
    }

    /**
     * 校验所传参数
     * @param context
     * @param params
     * @return
     */
    public static ZCBLVideoSurveyModel checkParams(Context context, ZCBLRequireParamsModel params){
        if(null == params||null == context){
            ZCBLToastUtils.showToast(context,"所传参数不能为空");
//            throw new IllegalArgumentException("所传参数对象（ZCBLVideoSurveyModel）不能为空");
            Log.e(ZCBLConstants.TAG, "ZCBLSDK init：所传参数（ZCBLVideoSurveyModel）不能为空");
            return null;
        }
        String siSurveyNo = params.getSiSurveyNo();
        String phoneNum = params.getPhoneNum();
        String longitude = params.getLongitude();
        String latitude = params.getLatitude();
        String caseAddress = params.getCaseAddress();
        if(ZCBLCheckUtils.checkStringEmpty(siSurveyNo)){
            ZCBLToastUtils.showToast(context,"报案号不能为空");
            return null;
        }else if(ZCBLCheckUtils.checkStringEmpty(phoneNum)){
            ZCBLToastUtils.showToast(context,"手机号不能为空");
            return null;
        }else if(ZCBLCheckUtils.checkStringEmpty(longitude)){
            ZCBLToastUtils.showToast(context,"地理位置经度不能为空");
            return null;
        }else if(ZCBLCheckUtils.checkStringEmpty(latitude)){
            ZCBLToastUtils.showToast(context,"地理位置纬度不能为空");
            return null;
        }else if(ZCBLCheckUtils.checkStringEmpty(caseAddress)){
            ZCBLToastUtils.showToast(context,"地理详细位置不能为空");
            return null;
        }
        ZCBLVideoSurveyModel model = new ZCBLVideoSurveyModel(siSurveyNo, phoneNum, longitude, latitude, caseAddress, "", "", "");
        return model;
    }
}
