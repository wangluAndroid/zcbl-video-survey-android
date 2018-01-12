package com.zcbl.client.zcbl_video_survey_library.bean;

import java.io.Serializable;

/**
 * Created by serenitynanian on 2017/12/22.
 */

public class WilddogVideoModel implements Serializable {
    private String siSurveyNo;
    private String phoneNum;
    private String carNum;
    private String longitude;
    private String latitude;
    private String caseAddress;
    private String videoRoomId;
    private String syncCommandNodePath;
    private String syncVideoConnectCommandNodePath;


    public WilddogVideoModel() {
    }

    public WilddogVideoModel(String siSurveyNo,
                             String phoneNum,
                             String carNum,
                             String longitude,
                             String latitude,
                             String caseAddress,
                             String videoRoomId,
                             String syncCommandNodePath,
                             String syncVideoConnectCommandNodePath
                             ) {

        this.siSurveyNo = siSurveyNo; //查勘号
        this.phoneNum = phoneNum;//报案人手机号
        this.carNum = carNum;//报案人车牌号
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

    public String getCarNum() {
        return carNum;
    }

    public void setCarNum(String carNum) {
        this.carNum = carNum;
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

    @Override
    public String toString() {
        return "WilddogVideoModel{" +
                "siSurveyNo='" + siSurveyNo + '\'' +
                ", phoneNum='" + phoneNum + '\'' +
                ", carNum='" + carNum + '\'' +
                ", longitude='" + longitude + '\'' +
                ", latitude='" + latitude + '\'' +
                ", caseAddress='" + caseAddress + '\'' +
                ", videoRoomId='" + videoRoomId + '\'' +
                ", syncCommandNodePath='" + syncCommandNodePath + '\'' +
                ", syncVideoConnectCommandNodePath='" + syncVideoConnectCommandNodePath + '\'' +
                '}';
    }
}