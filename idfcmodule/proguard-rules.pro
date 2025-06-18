-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.content.ContentProvider
-keep class com.service.idfcmodule.models.**{*;}
-keep class com.service.idfcmodule.utils.**{*;}



-dontpreverify
-repackageclasses 'com.service.idfc'
-allowaccessmodification
-optimizations !code/simplification/arithmetic,!field
-keepattributes *Annotation*


-dontoptimize
#-dontobfuscate
-ignorewarnings

-dontpreverify
-optimizations !code/simplification/arithmetic
-useuniqueclassmembernames
-overloadaggressively



# To enable ProGuard in your project, edit project.properties
# to define the proguard.config property as described in that file.
# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in ${sdk.dir}/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the ProGuard
# include property in project.properties.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:
#-dontshrink

#-dontobfuscate
-ignorewarnings




# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class android.webkit.WebView {
#   public *;
#}

#-injars      bin/classes
#-injars      libs
#-outjars     bin/classes-processed.jar

# Using Google's License Verification Library


# Specifies to write out some more information during processing.
# If the program terminates with an exception, this option will print out the entire stack trace, instead of just the exception message.
-verbose

# Annotations are represented by attributes that have no direct effect on the execution of the code.
-keepattributes Annotation

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepattributes InnerClasses
-keep class **.R
-keep class *.R$ {
    <fields>;
}


# These options let obfuscated applications or libraries produce stack traces that can still be deciphered later on
-renamesourcefileattribute SourceFile
#-keepattributes SourceFile,LineNumberTable


#-dontusemixedcaseclassnames
#-dontskipnonpubliclibraryclasses
#-dontskipnonpubliclibraryclassmembers

# Enable proguard with Cordova
-keep class org.apache.cordova.* {}

-keep class com.worklight.androidgap.push.* {}
-keep class com.worklight.wlclient.push.* {}

# Enable proguard with Google libs
-keep class com.google.* {}
-dontwarn com.google.common.**
-dontwarn com.google.ads.**
-dontwarn com.google.android.gms.**

# apache.http
-optimizations !class/merging/vertical*,!class/merging/horizontal*,!code/simplification/arithmetic,!field/*,!code/allocation/variable

-keep class net.sqlcipher.* {}
-dontwarn net.sqlcipher.**

-keep class org.codehaus.* { }
-keepattributes *Annotation*,EnclosingMethod

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-assumenosideeffects class android.util.Log {
    public static *** d(...);
}

# These classes contain references to external jars which are not included in the default MobileFirst project.
-dontwarn com.worklight.common.internal.WLTrusteerInternal*
-dontwarn com.worklight.jsonstore.**
-dontwarn org.codehaus.jackson.map.ext.*


-dontwarn android.support.v4.**
-dontwarn android.net.SSLCertificateSocketFactory
-dontwarn android.net.http.*


# common ssl classes
-keepnames class org.apache.* {}
-keep public class org.apache.* {}
-dontwarn org.apache.commons.ssl.**

#event bus dependency
-keepattributes Annotation

# Only required if you use AsyncExecutor


-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}



