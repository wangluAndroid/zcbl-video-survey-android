package com.zcbl.client.zcbl_video_survey_library.bean;

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
    private String syncCommandNodePath;
    private String syncVideoConnectCommandNodePath;
    private String navigatorBarColor ;

    public ZCBLVideoSurveyModel() {
    }

    public ZCBLVideoSurveyModel(String siSurveyNo, String phoneNum, String longitude, String latitude, String caseAddress, String videoRoomId, String syncCommandNodePath, String syncVideoConnectCommandNodePath, String navigatorBarColor) {
        this.siSurveyNo = siSurveyNo;
        this.phoneNum = phoneNum;
        this.longitude = longitude;
        this.latitude = latitude;
        this.caseAddress = caseAddress;
        this.videoRoomId = videoRoomId;
        this.syncCommandNodePath = syncCommandNodePath;
        this.syncVideoConnectCommandNodePath = syncVideoConnectCommandNodePath;
        this.navigatorBarColor = navigatorBarColor;
    }

    public ZCBLVideoSurveyModel(String siSurveyNo,
                                String phoneNum,
                                String longitude,
                                String latitude,
                                String caseAddress,
                                String videoRoomId,
                                String syncCommandNodePath,
                                String syncVideoConnectCommandNodePath
                             ) {

        this.siSurveyNo = siSurveyNo; //查勘号
        this.phoneNum = phoneNum;//报案人手机号
        this.longitude = longitude;//经度------浮点型
        this.latitude = latitude;//纬度-----浮点型
        this.caseAddress = caseAddress;//事故地点
        this.videoRoomId = videoRoomId;//视频房间id
        this.syncVideoConnectCommandNodePath = syncVideoConnectCommandNodePath;//视频连接 指令节点路径
        this.syncCommandNodePath = syncCommandNodePath;//视频过程中 指令节点路径
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

    public String getSyncCommandNodePath() {
        return syncCommandNodePath;
    }

    public void setSyncCommandNodePath(String syncCommandNodePath) {
        this.syncCommandNodePath = syncCommandNodePath;
    }

    public String getSyncVideoConnectCommandNodePath() {
        return syncVideoConnectCommandNodePath;
    }

    public void setSyncVideoConnectCommandNodePath(String syncVideoConnectCommandNodePath) {
        this.syncVideoConnectCommandNodePath = syncVideoConnectCommandNodePath;
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
                ", syncCommandNodePath='" + syncCommandNodePath + '\'' +
                ", syncVideoConnectCommandNodePath='" + syncVideoConnectCommandNodePath + '\'' +
                ", navigatorBarColor='" + navigatorBarColor + '\'' +
                '}';
    }
}
