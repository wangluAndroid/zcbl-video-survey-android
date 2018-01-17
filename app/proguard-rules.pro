# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/serenitynanian/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
#---------------------------------基本指令区----------------------------------
#-dontwarn
#-ignorewarnings                # 抑制警告
#代码混淆压缩比，在0~7之间
-optimizationpasses 5
#混淆时不适用大小写混合，混合后的类名为小写
-dontusemixedcaseclassnames
#指定不去忽略非公共库的类
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
#不做预校验，preverify是proguard的四个步骤之一，Android不需要precerify,去掉这一步能够加快混淆速度
-dontpreverify
-verbose
-printmapping proguardMapping.txt
#google推荐算法
-optimizations !code/simplification/cast,!field/*,!class/merging/*
#避免混淆Annotation、内部类、泛型、匿名类
-keepattributes *Annotation*,InnerClasses
-keepattributes Signature
#抛出异常时保留代码行号
-keepattributes SourceFile,LineNumberTable

#处理support包
-dontnote android.support.**
-dontwarn android.support.**

#---------------------------------默认保留区---------------------------------
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View
-keep public class com.android.vending.licensing.ILicensingService
-keep class android.support.** {*;}
#保留本地native方法不被混淆
-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclassmembers class * extends android.app.Activity{
    public void *(android.view.View);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep public class * extends android.view.View{
    *** get*();
    void set*(***);
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
-keep class **.R$* {
 *;
}
-keepclassmembers class * {
    void *(**On*Event);
}
#----------------------------------------------------------------------------

#------------------self-----------------------
-keep class com.zcbl.client.zcbl_video_survey_library.**{*;}
-keep interface com.zcbl.client.zcbl_video_survey_library.service.UpdateCallbackInterface{*;}

#----------------wilddog-----------------------
-keep class com.fasterxml.jackson.**{*;}
-keep class com.fasterxml.jackson.databind.**{*;}
-dontwarn com.fasterxml.jackson.databind.**
-keep class com.fasterxml.jackson.core.**{*;}
-keep class org.w3c.dom.bootstrap.**{*;}

-keep class org.webrtc.**{*;}
-keep class de.tavendo.autobahn.**{*;}
-keep class com.wilddog.video.**{*;}
-keep class com.wilddog.client.**{*;}
-keep class com.wilddog.wilddogauth.**{*;}
-keep class com.wilddog.wilddongcore.**{*;}


#---------------okhttp3-------------------
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

#-dontwarn com.squareup.okhttp.**
#-keep class com.squareup.okhttp.** {*;}
#-keep interface com.squareup.okhttp.** {*;}
#-dontwarn okio.**
#-keep class okhttp3.**{*;}

#-------------------socket---------------
#-keep class socket.io-client.**{*;}
#-keep class io.socket.**{*;}
