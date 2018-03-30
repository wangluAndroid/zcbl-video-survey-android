package com.zcbl.client.zcbl_video_survey_library.ui.tx.bean;

import android.support.annotation.Nullable;
import android.util.Log;

import com.zcbl.client.zcbl_video_survey_library.ZCBLConstants;
import com.zcbl.client.zcbl_video_survey_library.ui.tx.http.HttpRequests;
import com.zcbl.client.zcbl_video_survey_library.ui.tx.http.HttpResponse;
import com.zcbl.client.zcbl_video_survey_library.ui.tx.listener.IVideoDisplayListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by serenitynanian on 2018/3/21.
 */

public class RoomManager implements Serializable{

    public HashMap<String, PusherInfo> mPushers = new LinkedHashMap<>();
    public String roomId;
    public String roomName;
    public String selfUserID;
    public String selfUserName;
    public String avatarUrl;
    public int roomState = RoomState.Absent;
    public String pushUrl ;

    private String longitude;
    private String latitude;
    private String caseAddress;
    private String zuoxiIMAccount ;
    private String surveyInfo;


    public String getSurveyInfo() {
        return surveyInfo;
    }

    public void setSurveyInfo(String surveyInfo) {
        this.surveyInfo = surveyInfo;
    }

    public String getZuoxiIMAccount() {
        return zuoxiIMAccount;
    }

    public void setZuoxiIMAccount(String zuoxiIMAccount) {
        this.zuoxiIMAccount = zuoxiIMAccount;
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

    public String getPushUrl() {
        return pushUrl;
    }

    public void setPushUrl(String pushUrl) {
        this.pushUrl = pushUrl;
    }

    public boolean isDestroyed() {
        return roomState == RoomState.Absent;
    }

    public void setState(int state) {
        roomState = state;
    }

    public int getState() {
        return roomState;
    }

    public boolean isState(int state){
        return (state & roomState) == state;
    }

    public String getAvatarUrl() {
        return avatarUrl == null ? "null" : avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getRoomId() {
        return roomId;
    }

    public synchronized void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName == null ? "未命名" : roomName;
    }

    public synchronized void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getSelfUserID() {
        return selfUserID;
    }

    public synchronized void setSelfUserID(String selfUserID) {
        this.selfUserID = selfUserID;
    }

    public void setSelfUserName(String selfUserName) {
        this.selfUserName = selfUserName;
    }

    public String getSelfUserName() {
        return selfUserName == null ? "null" : selfUserName;
    }

    @Override
    public String toString() {
        return "RoomManager{" +
                "mPushers=" + mPushers +
                ", roomId='" + roomId + '\'' +
                ", roomName='" + roomName + '\'' +
                ", selfUserID='" + selfUserID + '\'' +
                ", selfUserName='" + selfUserName + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                ", roomState=" + roomState +
                '}';
    }

    public synchronized void clean() {
        roomId = "";
        roomName = "";
        roomState = RoomState.Absent;
        mPushers.clear();
    }

    public synchronized void mergerPushers(List<PusherInfo> members, List<PusherInfo> newMembers, List<PusherInfo> delMembers){

        if (members == null) {
            if (delMembers != null) {
                delMembers.clear();
                for (Map.Entry<String, PusherInfo> entry : mPushers.entrySet()) {
                    delMembers.add(entry.getValue());
                }
            }
            mPushers.clear();
            return;
        }

        HashMap<String, PusherInfo> memberHashMap = new LinkedHashMap<>();
        for (PusherInfo member : members) {
            if (member.userID != null && !member.userID.equals(selfUserID)){
                if (!mPushers.containsKey(member.userID)) {
                    if (newMembers != null)
                        newMembers.add(member);
                }
                memberHashMap.put(member.userID, member);
            }
        }

        if (delMembers != null) {
            for (Map.Entry<String, PusherInfo> entry : mPushers.entrySet()) {
                if (!memberHashMap.containsKey(entry.getKey())) {
                    delMembers.add(entry.getValue());
                }
            }
        }

        this.mPushers.clear();
        this.mPushers = memberHashMap;
    }

//    public void updatePushers(HttpRequests mHttpRequest,final IRTCRoomListener roomListenerCallback){
//
//        if (roomId == null) return;
//
//        mHttpRequest.getPushers(roomId, new HttpRequests.OnResponseCallback<HttpResponse.PusherList>() {
//            @Override
//            public void onResponse(int retcode, @Nullable String retmsg, @Nullable final HttpResponse.PusherList data) {
//                if (retcode == HttpResponse.CODE_OK && data != null && data.pushers != null) {
//
//                    List<PusherInfo> newMembers = new ArrayList<>();
//                    List<PusherInfo> delMembers = new ArrayList<>();
//                    mergerPushers(data.pushers, newMembers, delMembers);
//
//                    roomListenerCallback.onDebugLog(String.format("[RTCRoom][updatePushers] pushers.size  All(%d), new(%d), remove(%d)",
//                            data.pushers.size(), newMembers.size(), delMembers.size()));
//
//                    for (PusherInfo member : delMembers) {
//                        roomListenerCallback.onPusherQuit(member);
//                    }
//
//                    for (PusherInfo member : newMembers) {
//                        roomListenerCallback.onPusherJoin(member);
//                    }
//                }
//            }
//        });
//    }


    public void updatePushers(HttpRequests mHttpRequest,final IVideoDisplayListener iVideoDisplayListener){
        if (roomId == null) return;
        Log.i(ZCBLConstants.TAG, "updatePushers:--roomId----> "+roomId);
        mHttpRequest.getPushers(roomId, new HttpRequests.OnResponseCallback<HttpResponse.PusherList>() {
            @Override
            public void onResponse(int retcode, @Nullable String retmsg, @Nullable final HttpResponse.PusherList data) {
                if (retcode == HttpResponse.CODE_OK && data != null && data.pushers != null) {

                    List<PusherInfo> newMembers = new ArrayList<>();
                    List<PusherInfo> delMembers = new ArrayList<>();
                    mergerPushers(data.pushers, newMembers, delMembers);

                    iVideoDisplayListener.onDebugLog(String.format("[RoomManager][updatePushers] pushers.size  All(%d), new(%d), remove(%d)", data.pushers.size(), newMembers.size(), delMembers.size()));
                    for (PusherInfo member : delMembers) {
                        iVideoDisplayListener.onPusherQuit(member);
                    }

                    for (PusherInfo member : newMembers) {
                        if (!member.userID.equals(selfUserID)) {
                            iVideoDisplayListener.getRemoteStreamSuccess();
                            iVideoDisplayListener.onPusherJoin(member);
                        } else {
                            iVideoDisplayListener.onDebugLog("--onPusherJoin--->"+member.toString());
                        }
                    }
                    if (data.pushers.size() <= 1) {
                        iVideoDisplayListener.getRemoteStreamFailure(-1,"获取远端流失败");
                    }
//                    PusherInfo pushInfo = data.pushers.get(0);
//                    iVideoDisplayListener.onPusherJoin(pushInfo);
                } else {
                    Log.e(ZCBLConstants.TAG, "get push url failure");
                }
            }
        });
    }
}
