package com.zcbl.client.zcbl_video_survey_library.bean;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by serenitynanian on 2017/12/22.
 */

public class ZCBLVideoSurveyModel implements Serializable {
    private String siSurveyNo;
    private String phoneNum;
    private String longitude;
    private String latitude;
    private String caseAddress;
    private String videoRoomId;

    private String navigatorBarColor ;
    //tx
    private String customerServiceImAccount;
    private String customerServicePhone;
    private JSONObject data;

    public ZCBLVideoSurveyModel() {
    }

    public ZCBLVideoSurveyModel(String siSurveyNo, String phoneNum, String longitude, String latitude, String caseAddress, String videoRoomId, String customerServiceImAccount, String customerServicePhone, JSONObject orderData,String navigatorBarColor) {
        this.siSurveyNo = siSurveyNo;
        this.phoneNum = phoneNum;
        this.longitude = longitude;
        this.latitude = latitude;
        this.caseAddress = caseAddress;
        this.videoRoomId = videoRoomId;
        this.navigatorBarColor = navigatorBarColor;
        this.customerServiceImAccount = customerServiceImAccount ;
        this.customerServicePhone = customerServicePhone ;
        this.data = orderData ;
    }


    public String getCustomerServiceImAccount() {
        return customerServiceImAccount;
    }

    public void setCustomerServiceImAccount(String customerServiceImAccount) {
        this.customerServiceImAccount = customerServiceImAccount;
    }

    public String getCustomerServicePhone() {
        return customerServicePhone;
    }

    public void setCustomerServicePhone(String customerServicePhone) {
        this.customerServicePhone = customerServicePhone;
    }

    public JSONObject getData() {
        return data;
    }

    public void setData(JSONObject data) {
        this.data = data;
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

    public String getVideoRoomId() {
        return videoRoomId;
    }

    public void setVideoRoomId(String videoRoomId) {
        this.videoRoomId = videoRoomId;
    }


    public String getNavigatorBarColor() {
        return navigatorBarColor;
    }

    public void setNavigatorBarColor(String navigatorBarColor) {
        this.navigatorBarColor = navigatorBarColor;
    }


    @Override
    public String toString() {
        return "ZCBLVideoSurveyModel{" +
                "siSurveyNo='" + siSurveyNo + '\'' +
                ", phoneNum='" + phoneNum + '\'' +
                ", longitude='" + longitude + '\'' +
                ", latitude='" + latitude + '\'' +
                ", caseAddress='" + caseAddress + '\'' +
                ", videoRoomId='" + videoRoomId + '\'' +
                ", navigatorBarColor='" + navigatorBarColor + '\'' +
                ", customerServiceImAccount='" + customerServiceImAccount + '\'' +
                ", customerServicePhone='" + customerServicePhone + '\'' +
                ", data=" + data +
                '}';
    }
}
