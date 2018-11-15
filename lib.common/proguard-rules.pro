# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

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

#-----------------Retrofit Start-----------------------------#
#Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote retrofit2.Platform
# Platform used when running on RoboVM on iOS. Will not be used at runtime.
-dontnote retrofit2.Platform$IOS$MainThreadExecutor
# Platform used when running on Java 8 VMs. Will not be used at runtime.
-dontwarn retrofit2.Platform$Java8
# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions
#-----------------Retrofit End-------------------------------



#-----------------RxJava Start-----------------------------
# rx
-dontwarn rx.**
-keepclassmembers class rx.** { *; }
# retrolambda
-dontwarn java.lang.invoke.*
#-----------------RxJava End-------------------------------



#-----------------Okhttp Start-----------------------------
#okhttputils
-dontwarn com.zhy.http.**
-keep class com.zhy.http.**{*;}
#okhttp
-dontwarn okhttp3.**
-keep class okhttp3.**{*;}
#okio
-dontwarn okio.**
-keep class okio.**{*;}
#-----------------Okhttp End-------------------------------



#-----------------Gson Start-----------------------------
# removes such information by default, so configure it to keep all of it. -keepattributes Signature
# Gson specific classes
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }
# Application classes that will be serialized/deserialized over Gson -keep class com.google.gson.examples.android.model.** { *; }
-keep class com.google.gson.** { *;}
#-----------------Gson End-------------------------------



# Added for game demos
-dontwarn com.tencent.smtt.**
-keep public class com.tencent.smtt.sdk.TBSGamePlayer {
    public <fields>;
    public <methods>;
}
-keep public class com.tencent.smtt.sdk.TBSGamePlayerClient* {
    public <fields>;
    public <methods>;
}
-keep public class com.tencent.smtt.sdk.TBSGamePlayerClientExtension {
    public <fields>;
    public <methods>;
}
-keep public class com.tencent.smtt.sdk.TBSGamePlayerService* {
    public <fields>;
    public <methods>;
}
-keep public class com.tencent.smtt.utils.Apn {
    public <fields>;
    public <methods>;
}
-keep class com.tencent.smtt.** {
    *;
}
# end

-keep public class com.tencent.smtt.export.external.extension.proxy.ProxyWebViewClientExtension {
    public <fields>;
    public <methods>;
}
-keep class MTT.ThirdAppInfoNew {
    *;
}
-keep class com.tencent.mtt.MttTraceEvent {
    *;
}
# Game related
-keep public class com.tencent.smtt.gamesdk.* {
    public protected *;
}
-keep public class com.tencent.smtt.sdk.TBSGameBooter {
        public <fields>;
        public <methods>;
}
-keep public class com.tencent.smtt.sdk.TBSGameBaseActivity {
    public protected *;
}
-keep public class com.tencent.smtt.sdk.TBSGameBaseActivityProxy {
    public protected *;
}
-keep public class com.tencent.smtt.gamesdk.internal.TBSGameServiceClient {
    public *;
}
#----------------------------腾讯X5浏览器内核 END-----------------------------------------------

