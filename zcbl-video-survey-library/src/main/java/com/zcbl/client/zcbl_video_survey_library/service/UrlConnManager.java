package com.zcbl.client.zcbl_video_survey_library.service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * Created by serenitynanian on 2017/12/15.
 */

public class UrlConnManager {
    public static HttpURLConnection getHttpURLConnection(String url){
        HttpURLConnection mHttpURLConnection=null;
        try {
            URL mUrl=new URL(url);
            mHttpURLConnection=(HttpURLConnection)mUrl.openConnection();
            //设置链接超时时间
            mHttpURLConnection.setConnectTimeout(15000);
            //设置读取超时时间
            mHttpURLConnection.setReadTimeout(15000);
            //设置请求参数
            mHttpURLConnection.setRequestMethod("POST");
            //添加Header
            mHttpURLConnection.setRequestProperty("Connection","Keep-Alive");
            //接收输入流
            mHttpURLConnection.setDoInput(true);
            //传递参数时需要开启
            mHttpURLConnection.setDoOutput(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mHttpURLConnection ;
    }

    public static void postParams(OutputStream output, Map<String,String> paramsList) throws IOException{
        StringBuilder mStringBuilder=new StringBuilder();
//        for (NameValuePair pair:paramsList){
//            if(!TextUtils.isEmpty(mStringBuilder)){
//                mStringBuilder.append("&");
//            }
//            mStringBuilder.append(URLEncoder.encode(pair.getName(),"UTF-8"));
//            mStringBuilder.append("=");
//            mStringBuilder.append(URLEncoder.encode(pair.getValue(),"UTF-8"));
//        }
        BufferedWriter writer=new BufferedWriter(new OutputStreamWriter(output,"UTF-8"));
        writer.write(mStringBuilder.toString());
        writer.flush();
        writer.close();
    }
}
