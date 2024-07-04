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

# Glide 4.12.0 rules
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$**
-keep public enum com.bumptech.glide.load.ImageHeaderParser$**
-keepclassmembers class com.bumptech.glide.RequestManager { <methods>; }
-keep class com.bumptech.glide.** { *; }
-keep class * extends com.bumptech.glide.load.Key { *; }
-keepclassmembers class * extends com.bumptech.glide.load.Key { *; }
-keep class * implements com.bumptech.glide.load.ResourceDecoder { *; }
-keep class * implements com.bumptech.glide.load.resource.transcode.ResourceTranscoder { *; }
-keep class * implements com.bumptech.glide.load.model.ModelLoader { *; }
-keep class * extends com.bumptech.glide.load.model.ModelLoader { *; }
-keep class * extends com.bumptech.glide.load.data.DataFetcher { *; }
-keep class * extends com.bumptech.glide.load.resource.drawable.DrawableResource { *; }

-keep class * extends android.graphics.drawable.Drawable { *; }
-keep class * extends android.graphics.Bitmap { *; }
-keep class * extends android.graphics.drawable.BitmapDrawable { *; }
