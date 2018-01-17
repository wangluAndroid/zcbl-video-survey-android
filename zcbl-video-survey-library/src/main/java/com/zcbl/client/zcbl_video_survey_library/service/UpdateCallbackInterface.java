package com.zcbl.client.zcbl_video_survey_library.service;

/**
 * Created by serenitynanian on 2018/1/17.
 */
public interface UpdateCallbackInterface {
    void onError(String error);
    void onSuccess(String response);
}