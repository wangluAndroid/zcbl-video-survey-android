package com.zcbl.client.zcbl_video_survey_library.ui.tx.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by jac on 2017/11/14.
 * Copyright © 2013-2017 Tencent Cloud. All Rights Reserved.
 */

public class SelfAccountInfo implements Parcelable {

    public String   userID;
    public String   userName;
    public String   userAvatar;
    public String   userSig;
    public String   accType;
    public int      sdkAppID;


    public SelfAccountInfo() {

    }

    public SelfAccountInfo(String userID, String userName, String headPicUrl, String userSig, String accType, int sdkAppID) {
        this.userID = userID;
        this.userName = userName;
        this.userAvatar = headPicUrl;
        this.userSig = userSig;
        this.accType = accType;
        this.sdkAppID = sdkAppID;
    }

    protected SelfAccountInfo(Parcel in) {
        this.userID = in.readString();
        this.userName = in.readString();
        this.userAvatar = in.readString();
        this.userSig = in.readString();
        this.accType = in.readString();
        this.sdkAppID = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.userID);
        dest.writeString(this.userName);
        dest.writeString(this.userAvatar);
        dest.writeString(this.userSig);
        dest.writeString(this.accType);
        dest.writeInt(this.sdkAppID);
    }

    public static final Creator<SelfAccountInfo> CREATOR = new Creator<SelfAccountInfo>() {
        @Override
        public SelfAccountInfo createFromParcel(Parcel source) {
            return new SelfAccountInfo(source);
        }

        @Override
        public SelfAccountInfo[] newArray(int size) {
            return new SelfAccountInfo[size];
        }
    };

    @Override
    public String toString() {
        return "SelfAccountInfo{" +
                "userID='" + userID + '\'' +
                ", userName='" + userName + '\'' +
                ", userAvatar='" + userAvatar + '\'' +
                ", userSig='" + userSig + '\'' +
                ", accType='" + accType + '\'' +
                ", sdkAppID=" + sdkAppID +
                '}';
    }
}
