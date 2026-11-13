package com.appresizer;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

public class AppResizerApp extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        String lang = ConfigManager.get(base).getLang();
        super.attachBaseContext(LocaleUtils.wrap(base, lang));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // 全局防崩溃：任何未捕获异常都会进入崩溃界面，而非直接闪退。
        try {
            Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(getApplicationContext()));
        } catch (Throwable ignored) {
        }
    }
}
