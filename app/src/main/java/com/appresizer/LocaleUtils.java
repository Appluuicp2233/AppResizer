package com.appresizer;

import android.content.Context;
import android.content.res.Configuration;

import java.util.Locale;

/**
 * 语言与密度上下文工具。
 * 统一在上下文层叠加：200 DPI + 用户选择的语言（中/英/日/跟随系统）。
 */
public final class LocaleUtils {

    private LocaleUtils() {
    }

    public static Locale resolveLocale(String lang) {
        if (lang == null) {
            lang = ConfigManager.LANG_SYSTEM;
        }
        switch (lang) {
            case ConfigManager.LANG_ZH:
                return new Locale("zh", "CN");
            case ConfigManager.LANG_EN:
                return new Locale("en");
            case ConfigManager.LANG_JA:
                return new Locale("ja");
            default:
                return Locale.getDefault();
        }
    }

    /**
     * 在给定 base 之上叠加 200 DPI 与所选语言，返回可用于 attachBaseContext 的上下文。
     */
    public static Context wrap(Context base, String lang) {
        Configuration configuration = new Configuration(base.getResources().getConfiguration());
        configuration.densityDpi = BaseActivity.APP_DENSITY;
        Locale locale = resolveLocale(lang);
        configuration.locale = locale;
        Locale.setDefault(locale);
        return base.createConfigurationContext(configuration);
    }
}
