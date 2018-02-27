package com.zcbl.client.zcbl_video_survey_library.bean;

import java.io.Serializable;

/**
 * Created by serenitynanian on 2018/1/15.
 * 中车视频查勘对接所需参数model
 */

public class ZCBLRequireParamsModel implements Serializable{
    /**
     * 事故查勘号
     */
    private String siSurveyNo;

    /**
     * 当前APP登录手机号
     */
    private String phoneNum;

    /**
     * 报案事故时经度
     */
    private String longitude;

    /**
     * 报案事故时纬度
     */
    private String latitude;

    /**
     * 报案事故详细地点
     */
    private String caseAddress;

    /**
     * 导航条和状态栏颜色--针对5.0以上
     */
    private String navigatorBarColor ;

    public ZCBLRequireParamsModel() {
    }

    /**
     * @param siSurveyNo 事故查勘号
     * @param phoneNum 当前APP登录手机号
     * @param longitude 报案事故时经度
     * @param latitude 报案事故时纬度
     * @param caseAddress 报案事故详细地点
     * @param navigatorBarColor 导航栏和状态栏颜色
     */
    public ZCBLRequireParamsModel(String siSurveyNo, String phoneNum, String longitude, String latitude, String caseAddress, String navigatorBarColor) {
        this.siSurveyNo = siSurveyNo;
        this.phoneNum = phoneNum;
        this.longitude = longitude;
        this.latitude = latitude;
        this.caseAddress = caseAddress;
        this.navigatorBarColor = navigatorBarColor;
    }

    /**
     * @param siSurveyNo 事故查勘号
     * @param phoneNum 当前APP登录手机号
     * @param longitude 报案事故时经度
     * @param latitude 报案事故时纬度
     * @param caseAddress 报案事故详细地点
     */
    public ZCBLRequireParamsModel(String siSurveyNo, String phoneNum, String longitude, String latitude, String caseAddress) {
        this.siSurveyNo = siSurveyNo;
        this.phoneNum = phoneNum;
        this.longitude = longitude;
        this.latitude = latitude;
        this.caseAddress = caseAddress;
    }

    public String getSiSurveyNo() {
        return siSurveyNo;
    }

    public void setSiSurveyNo(String siSurveyNo) {
        this.siSurveyNo = siSurveyNo;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getCaseAddress() {
        return caseAddress;
    }

    public void setCaseAddress(String caseAddress) {
        this.caseAddress = caseAddress;
    }

    public String getNavigatorBarColor() {
        return navigatorBarColor;
    }

    public void setNavigatorBarColor(String navigatorBarColor) {
        this.navigatorBarColor = navigatorBarColor;
    }

    @Override
    public String toString() {
        return "ZCBLRequireParamsModel{" +
                "siSurveyNo='" + siSurveyNo + '\'' +
                ", phoneNum='" + phoneNum + '\'' +
                ", longitude='" + longitude + '\'' +
                ", latitude='" + latitude + '\'' +
                ", caseAddress='" + caseAddress + '\'' +
                ", navigatorBarColor='" + navigatorBarColor + '\'' +
                '}';
    }
}
