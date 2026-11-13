package com.appresizer;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class ConfigManager {

    public static final String PREFS_NAME = "appresizer_config";
    public static final String SELF_PACKAGE = "com.appresizer";
    public static final String GITHUB_REPO_URL = "https://github.com/Appluuicp2233/AppResizer";

    public static final String KEY_DEFAULT_DPI = "default_dpi";
    public static final String KEY_DEFAULT_WIDTH = "default_width";
    public static final String KEY_DEFAULT_HEIGHT = "default_height";
    public static final String KEY_KILL_METHOD = "kill_method";
    public static final String KEY_SHOW_SYSTEM = "show_system";
    public static final String KEY_SHOW_STATUS = "show_status";
    public static final String KEY_AGREED = "agreed";
    public static final String KEY_MODE_CHOSEN = "mode_chosen";
    public static final String KEY_DPI_METHOD = "dpi_method";
    public static final String KEY_FIRST_RUN = "first_run";
    public static final String KEY_FULLSCREEN_DIALOG = "fullscreen_dialog";
    public static final String KEY_LIST_MODE = "list_mode";
    public static final String KEY_LANG = "lang";

    public static final int LIST_MODE_SYSTEM = 0;
    public static final int LIST_MODE_ROOT = 1;

    // 语言选项：跟随系统 / 简体中文 / English / 日本語
    public static final String LANG_SYSTEM = "system";
    public static final String LANG_ZH = "zh";
    public static final String LANG_EN = "en";
    public static final String LANG_JA = "ja";

    public static final int PLACEHOLDER = 0;

    public static final int KILL_METHOD_FORCE = 0;
    public static final int KILL_METHOD_AM = 1;
    public static final int KILL_METHOD_NONE = 2;

    public static final int METHOD_CONFIGURATION = 0;
    public static final int METHOD_METRICS = 1;
    public static final int METHOD_RESOURCES_MANAGER = 2;
    public static final int METHOD_ACTIVITY = 3;
    public static final int METHOD_COMBINED = 4;

    public static final int METHOD_COUNT = 5;

    public static final int[] METHOD_VALUES = {
            METHOD_CONFIGURATION,
            METHOD_METRICS,
            METHOD_RESOURCES_MANAGER,
            METHOD_ACTIVITY,
            METHOD_COMBINED
    };

    public static final int DPI_MIN = 72;
    public static final int DPI_MAX = 960;
    public static final int DPI_STEP = 10;
    public static final int SIZE_MIN = 320;
    public static final int SIZE_MAX = 4320;

    private static ConfigManager instance;

    private final Context appContext;
    private final SharedPreferences prefs;

    private ConfigManager(Context context) {
        appContext = context.getApplicationContext();
        prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized ConfigManager get(Context context) {
        if (instance == null) {
            instance = new ConfigManager(context);
        }
        return instance;
    }

    public int getDefaultDpi() {
        return prefs.getInt(KEY_DEFAULT_DPI, PLACEHOLDER);
    }

    public void setDefaultDpi(int dpi) {
        prefs.edit().putInt(KEY_DEFAULT_DPI, dpi).commit();
        makeReadable();
    }

    public int getDefaultWidth() {
        return prefs.getInt(KEY_DEFAULT_WIDTH, PLACEHOLDER);
    }

    public void setDefaultWidth(int width) {
        prefs.edit().putInt(KEY_DEFAULT_WIDTH, width).commit();
        makeReadable();
    }

    public int getDefaultHeight() {
        return prefs.getInt(KEY_DEFAULT_HEIGHT, PLACEHOLDER);
    }

    public void setDefaultHeight(int height) {
        prefs.edit().putInt(KEY_DEFAULT_HEIGHT, height).commit();
        makeReadable();
    }

    public int getKillMethod() {
        return prefs.getInt(KEY_KILL_METHOD, KILL_METHOD_FORCE);
    }

    public void setKillMethod(int method) {
        prefs.edit().putInt(KEY_KILL_METHOD, method).commit();
        makeReadable();
    }

    public int getDpiMethod() {
        return prefs.getInt(KEY_DPI_METHOD, METHOD_CONFIGURATION);
    }

    public void setDpiMethod(int method) {
        prefs.edit().putInt(KEY_DPI_METHOD, method).commit();
        makeReadable();
    }

    public boolean isShowSystem() {
        return prefs.getBoolean(KEY_SHOW_SYSTEM, false);
    }

    public void setShowSystem(boolean show) {
        prefs.edit().putBoolean(KEY_SHOW_SYSTEM, show).commit();
        makeReadable();
    }

    public boolean isShowStatus() {
        return prefs.getBoolean(KEY_SHOW_STATUS, true);
    }

    public void setShowStatus(boolean show) {
        prefs.edit().putBoolean(KEY_SHOW_STATUS, show).commit();
        makeReadable();
    }

    public boolean isFullscreenDialog() {
        return prefs.getBoolean(KEY_FULLSCREEN_DIALOG, false);
    }

    public void setFullscreenDialog(boolean fullscreen) {
        prefs.edit().putBoolean(KEY_FULLSCREEN_DIALOG, fullscreen).commit();
        makeReadable();
    }

    public int getListMode() {
        return prefs.getInt(KEY_LIST_MODE, LIST_MODE_SYSTEM);
    }

    public void setListMode(int mode) {
        prefs.edit().putInt(KEY_LIST_MODE, mode).commit();
        makeReadable();
    }

    public String getLang() {
        return prefs.getString(KEY_LANG, LANG_SYSTEM);
    }

    public void setLang(String lang) {
        prefs.edit().putString(KEY_LANG, lang).commit();
        makeReadable();
    }

    public boolean isAgreed() {
        return prefs.getBoolean(KEY_AGREED, false);
    }

    public void setAgreed(boolean agreed) {
        prefs.edit().putBoolean(KEY_AGREED, agreed).commit();
        makeReadable();
    }

    public boolean isModeChosen() {
        return prefs.getBoolean(KEY_MODE_CHOSEN, false);
    }

    public void setModeChosen(boolean chosen) {
        prefs.edit().putBoolean(KEY_MODE_CHOSEN, chosen).commit();
        makeReadable();
    }

    public boolean isFirstRun() {
        return prefs.getBoolean(KEY_FIRST_RUN, true);
    }

    public void setFirstRun(boolean firstRun) {
        prefs.edit().putBoolean(KEY_FIRST_RUN, firstRun).commit();
        makeReadable();
    }

    public int getDpi(String packageName) {
        return prefs.getInt("dpi_" + packageName, PLACEHOLDER);
    }

    public int getWidth(String packageName) {
        return prefs.getInt("width_" + packageName, PLACEHOLDER);
    }

    public int getHeight(String packageName) {
        return prefs.getInt("height_" + packageName, PLACEHOLDER);
    }

    public int getMethod(String packageName) {
        return prefs.getInt("method_" + packageName, -1);
    }

    public boolean isConfigured(String packageName) {
        return getDpi(packageName) > 0 || getWidth(packageName) > 0 || getHeight(packageName) > 0;
    }

    public void saveConfig(String packageName, int dpi, int width, int height, int method) {
        prefs.edit()
                .putInt("dpi_" + packageName, dpi)
                .putInt("width_" + packageName, width)
                .putInt("height_" + packageName, height)
                .putInt("method_" + packageName, method)
                .commit();
        makeReadable();
    }

    public void reset(String packageName) {
        prefs.edit()
                .remove("dpi_" + packageName)
                .remove("width_" + packageName)
                .remove("height_" + packageName)
                .remove("method_" + packageName)
                .commit();
        makeReadable();
    }

    public File getPrefsFile() {
        return new File(appContext.getApplicationInfo().dataDir, "shared_prefs/" + PREFS_NAME + ".xml");
    }

    public File getMirrorFile() {
        return new File(appContext.getFilesDir(), "appresizer.conf");
    }

    public void writeMirror() {
        try {
            File file = getMirrorFile();
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            StringBuilder builder = new StringBuilder();
            for (java.util.Map.Entry<String, ?> entry : prefs.getAll().entrySet()) {
                Object value = entry.getValue();
                if (value == null) {
                    continue;
                }
                builder.append(entry.getKey()).append('=').append(String.valueOf(value)).append('\n');
            }
            java.io.FileOutputStream out = new java.io.FileOutputStream(file);
            out.write(builder.toString().getBytes("UTF-8"));
            out.flush();
            out.close();
            String path = file.getAbsolutePath();
            String dir = parent != null ? parent.getAbsolutePath() : path;
            ShellUtils.execRoot("chmod 755 " + dir + " ; chmod 644 " + path);
        } catch (Throwable ignored) {
        }
    }

    public void makeReadable() {
        // 全部放到后台线程执行，避免 su -c chmod 与文件写入阻塞主线程导致界面卡顿。
        final File file = getPrefsFile();
        final String dataDir = appContext.getApplicationInfo().dataDir;
        final String sharedPrefs = file.getParentFile() != null ? file.getParentFile().getAbsolutePath() : null;
        final String filePath = file.getAbsolutePath();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    writeMirror();
                } catch (Throwable ignored) {
                }
                if (!file.exists()) {
                    return;
                }
                try {
                    ShellUtils.execRoot("chmod 755 " + dataDir
                            + " ; chmod 755 " + sharedPrefs
                            + " ; chmod 644 " + filePath);
                } catch (Throwable ignored) {
                }
            }
        }).start();
    }

    public static int readOriginalDpi() {
        String[] files = {"/system/build.prop", "/vendor/build.prop", "/default.prop"};
        for (String path : files) {
            int dpi = readDensityFrom(path);
            if (dpi > 0) {
                return dpi;
            }
        }
        ShellUtils.Result result = ShellUtils.execRoot("getprop ro.sf.lcd_density");
        if (result.ok()) {
            String value = result.output.trim();
            try {
                int dpi = Integer.parseInt(value);
                if (dpi > 0) {
                    return dpi;
                }
            } catch (Throwable ignored) {
            }
        }
        return 0;
    }

    private static int readDensityFrom(String path) {
        BufferedReader reader = null;
        try {
            File file = new File(path);
            if (!file.exists()) {
                return 0;
            }
            reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("#")) {
                    continue;
                }
                int index = line.indexOf("ro.sf.lcd_density=");
                if (index < 0) {
                    continue;
                }
                String value = line.substring(index + "ro.sf.lcd_density=".length()).trim();
                int dpi = Integer.parseInt(value);
                if (dpi > 0) {
                    return dpi;
                }
            }
        } catch (Throwable ignored) {
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Throwable ignored) {
                }
            }
        }
        return 0;
    }

    public int getSystemDpi() {
        int original = readOriginalDpi();
        if (original > 0) {
            return original;
        }
        DisplayMetrics metrics = android.content.res.Resources.getSystem().getDisplayMetrics();
        return metrics.densityDpi;
    }

    public DisplayMetrics getSystemMetrics() {
        return android.content.res.Resources.getSystem().getDisplayMetrics();
    }
}
