package com.zcbl.client.zcbl_video_survey_library.service;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.text.TextUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by serenitynanian on 2018/1/16.
 */

public class ZCBLCrashHandler implements Thread.UncaughtExceptionHandler {


    //文件夹目录
    private static final String PATH = Environment.getExternalStorageDirectory().getPath() + "/zcbl_spck/crash_log/";
    //文件名
    private static final String FILE_NAME = "crash";
    //文件名后缀
    private static final String FILE_NAME_SUFFIX = ".trace";
    //上下文
    private Context mContext;

    //单例模式
    private static ZCBLCrashHandler sInstance = new ZCBLCrashHandler();
    private boolean crashing;
    private Thread.UncaughtExceptionHandler mDefaultHandler;

    private ZCBLCrashHandler() {
    }

    public static ZCBLCrashHandler getInstance() {
        return sInstance;
    }

    /**
     * 初始化方法
     *
     * @param context
     */
    public void init(Context context) {
        //将当前实例设为系统默认的异常处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
        //获取Context，方便内部使用
        mContext = context.getApplicationContext();

        crashing = false;
        //获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {

        if (crashing) {
            return;
        }
        crashing = true;

        // 打印异常信息
        throwable.printStackTrace();
        // 我们没有处理异常 并且默认异常处理不为空 则交给系统处理
        if (!handlelException(throwable) && mDefaultHandler != null) {
            // 系统处理
            mDefaultHandler.uncaughtException(thread, throwable);
        }

        //延时1秒杀死进程
        SystemClock.sleep(1000);
    }


    private boolean handlelException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        try {
            // 异常信息
            String crashReport = getCrashReport(ex);
            // TODO: 上传日志到服务器
            // 保存到sd卡
            dumpExceptionToSDCard(ex);

        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 导出异常信息到SD卡
     *
     * @param ex
     */
    private void dumpExceptionToSDCard(Throwable ex) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return;
        }
        //创建文件夹
        File dir = new File(PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        //获取当前时间
        long current = System.currentTimeMillis();
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(current));
        //以当前时间创建log文件
        File file = new File(PATH + FILE_NAME + time + FILE_NAME_SUFFIX);
        try {
            //输出流操作
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            //导出手机信息和异常信息
            PackageManager pm = mContext.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
            pw.println("发生异常时间：" + time);
            pw.println("应用版本：" + pi.versionName);
            pw.println("应用版本号：" + pi.versionCode);
            pw.println("android版本号：" + Build.VERSION.RELEASE);
            pw.println("android版本号API：" + Build.VERSION.SDK_INT);
            pw.println("手机制造商:" + Build.MANUFACTURER);
            pw.println("手机型号：" + Build.MODEL);
            ex.printStackTrace(pw);
            //关闭输出流
            pw.close();
        } catch (Exception e) {

        }
    }

    /**
     * 获取异常信息
     *
     * @param ex
     * @return
     */
    private String getCrashReport(Throwable ex) {
        StringBuffer exceptionStr = new StringBuffer();
        PackageManager pm = mContext.getPackageManager();
        PackageInfo pinfo = null;
        try {
            pinfo = pm.getPackageInfo(mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pinfo != null) {
                if (ex != null) {
                    //app版本信息
                    exceptionStr.append("App Version：" + pinfo.versionName);
                    exceptionStr.append("_" + pinfo.versionCode + "\n");

                    //手机系统信息
                    exceptionStr.append("OS Version：" + Build.VERSION.RELEASE);
                    exceptionStr.append("_");
                    exceptionStr.append(Build.VERSION.SDK_INT + "\n");

                    //手机制造商
                    exceptionStr.append("Vendor: " + Build.MANUFACTURER + "\n");

                    //手机型号
                    exceptionStr.append("Model: " + Build.MODEL + "\n");

                    String errorStr = ex.getLocalizedMessage();
                    if (TextUtils.isEmpty(errorStr)) {
                        errorStr = ex.getMessage();
                    }
                    if (TextUtils.isEmpty(errorStr)) {
                        errorStr = ex.toString();
                    }
                    exceptionStr.append("Exception: " + errorStr + "\n");
                    StackTraceElement[] elements = ex.getStackTrace();
                    if (elements != null) {
                        for (int i = 0; i < elements.length; i++) {
                            exceptionStr.append(elements[i].toString() + "\n");
                        }
                    }
                } else {
                    exceptionStr.append("no exception. Throwable is null\n");
                }
                return exceptionStr.toString();
            } else {
                return "";
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }


    /**
     * 上传异常信息到服务器
     *
     * @param ex
     */
    private void uploadExceptionToServer(Throwable ex) {

    }
}
