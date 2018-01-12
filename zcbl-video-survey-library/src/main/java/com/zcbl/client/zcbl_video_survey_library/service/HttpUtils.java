package com.zcbl.client.zcbl_video_survey_library.service;

import android.text.TextUtils;
import android.util.Log;

import com.zcbl.client.zcbl_video_survey_library.Constants;
import com.zcbl.client.zcbl_video_survey_library.bean.WilddogVideoModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by serenitynanian on 2017/12/15.
 */

public class HttpUtils {

    private static HttpUtils mInstance;
    private OkHttpClient mOkHttpClient;

    public interface UpdateCallback{
        void onError(String error);
        void onSuccess(String response);
    }
    private HttpUtils() {

        SSLParams sslSocketFactory = getSslSocketFactory(null, null, null);
        mOkHttpClient=new OkHttpClient
                .Builder()
                .readTimeout(20, TimeUnit.SECONDS)//读取超时
                .connectTimeout(20, TimeUnit.SECONDS)//连接超时
                .writeTimeout(60, TimeUnit.SECONDS)//写入超时
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                })
//                .sslSocketFactory(sslSocketFactory.sSLSocketFactory,sslSocketFactory.trustManager)
                .build();
    }


    public static HttpUtils getInstance() {
        if (mInstance == null) {
            synchronized (HttpUtils.class) {
                if (mInstance == null) {
                    mInstance = new HttpUtils();
                }
            }
        }
        return mInstance;
    }

    public void post(final String url ,final JSONObject json, final UpdateCallback updateCallback){
        new Thread(new Runnable() {
            @Override
            public void run() {
                RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),json.toString());
                Request request = new Request.Builder()
                        .addHeader("Content-Type","application/json")
                        .url(url)
                        .post(requestBody)
                        .build();
                mOkHttpClient.newCall(request)
                        .enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                Log.i(Constants.TAG,"-----------HttpUtils请求---onFailure----->" + e.toString());
                                updateCallback.onError(e.toString());
                            }

                            @Override
                            public void onResponse(Call call, final Response response) throws IOException {
                                String result = response.body().string();
                                Log.i(Constants.TAG,"------------HttpUtils请求---onResponse---->"+result);
                                if(!TextUtils.isEmpty(result)){
                                    if(result.contains("\"rescode\":\"200\"")){
                                        updateCallback.onSuccess(result);

                                    }else{
                                        updateCallback.onError("图片上传返回失败");
                                    }
                                }else{
                                    updateCallback.onError("图片上传返回失败");
                                }
                            }
                        });
            }
        }).start();
    }

    public static class SSLParams
    {
        public SSLSocketFactory sSLSocketFactory;
        public X509TrustManager trustManager;
    }

    public static SSLParams getSslSocketFactory(InputStream[] certificates, InputStream bksFile, String password)
    {
        SSLParams sslParams = new SSLParams();
        try
        {
            TrustManager[] trustManagers = prepareTrustManager(certificates);
            KeyManager[] keyManagers = prepareKeyManager(bksFile, password);
            SSLContext sslContext = SSLContext.getInstance("TLS");
            X509TrustManager trustManager = null;
            if (trustManagers != null)
            {
                trustManager = new MyTrustManager(chooseTrustManager(trustManagers));
            } else
            {
                trustManager = new UnSafeTrustManager();
            }
            sslContext.init(keyManagers, new TrustManager[]{trustManager},null);
            sslParams.sSLSocketFactory = sslContext.getSocketFactory();
            sslParams.trustManager = trustManager;
            return sslParams;
        } catch (NoSuchAlgorithmException e)
        {
            throw new AssertionError(e);
        } catch (KeyManagementException e)
        {
            throw new AssertionError(e);
        } catch (KeyStoreException e)
        {
            throw new AssertionError(e);
        }
    }

    private class UnSafeHostnameVerifier implements HostnameVerifier
    {
        @Override
        public boolean verify(String hostname, SSLSession session)
        {
            return true;
        }
    }

    private static class UnSafeTrustManager implements X509TrustManager
    {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException
        {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException
        {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers()
        {
            return new X509Certificate[]{};
        }
    }

    private static TrustManager[] prepareTrustManager(InputStream... certificates)
    {
        if (certificates == null || certificates.length <= 0) return null;
        try
        {

            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);
            int index = 0;
            for (InputStream certificate : certificates)
            {
                String certificateAlias = Integer.toString(index++);
                keyStore.setCertificateEntry(certificateAlias, certificateFactory.generateCertificate(certificate));
                try
                {
                    if (certificate != null)
                        certificate.close();
                } catch (IOException e)

                {
                }
            }
            TrustManagerFactory trustManagerFactory = null;

            trustManagerFactory = TrustManagerFactory.
                    getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);

            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

            return trustManagers;
        } catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        } catch (CertificateException e)
        {
            e.printStackTrace();
        } catch (KeyStoreException e)
        {
            e.printStackTrace();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;

    }

    private static KeyManager[] prepareKeyManager(InputStream bksFile, String password)
    {
        try
        {
            if (bksFile == null || password == null) return null;

            KeyStore clientKeyStore = KeyStore.getInstance("BKS");
            clientKeyStore.load(bksFile, password.toCharArray());
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(clientKeyStore, password.toCharArray());
            return keyManagerFactory.getKeyManagers();

        } catch (KeyStoreException e)
        {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e)
        {
            e.printStackTrace();
        } catch (CertificateException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private static X509TrustManager chooseTrustManager(TrustManager[] trustManagers)
    {
        for (TrustManager trustManager : trustManagers)
        {
            if (trustManager instanceof X509TrustManager)
            {
                return (X509TrustManager) trustManager;
            }
        }
        return null;
    }


    private static class MyTrustManager implements X509TrustManager
    {
        private X509TrustManager defaultTrustManager;
        private X509TrustManager localTrustManager;

        public MyTrustManager(X509TrustManager localTrustManager) throws NoSuchAlgorithmException, KeyStoreException
        {
            TrustManagerFactory var4 = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            var4.init((KeyStore) null);
            defaultTrustManager = chooseTrustManager(var4.getTrustManagers());
            this.localTrustManager = localTrustManager;
        }


        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException
        {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException
        {
            try
            {
                defaultTrustManager.checkServerTrusted(chain, authType);
            } catch (CertificateException ce)
            {
                localTrustManager.checkServerTrusted(chain, authType);
            }
        }


        @Override
        public X509Certificate[] getAcceptedIssuers()
        {
            return new X509Certificate[0];
        }
    }

}
