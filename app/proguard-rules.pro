-keep class com.appresizer.XposedHook { *; }
-keep class com.appresizer.ConfigManager { *; }
-keep class de.robv.android.xposed.** { *; }
-keepclassmembers class com.appresizer.MainActivity {
    public static boolean isModuleActive();
    public static java.lang.String getFrameworkName();
}
-keepattributes *Annotation*
-dontwarn de.robv.android.xposed.**
