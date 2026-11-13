package com.appresizer;

import android.app.Activity;
import android.app.Application;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;

import java.lang.reflect.Method;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class XposedHook implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    private static final String SELF_PACKAGE = "com.appresizer";
    private static final String PREFS_NAME = "appresizer_config";
    private static final String KEY_DPI_METHOD = "dpi_method";
    private static final String MIRROR_PATH = "/data/data/com.appresizer/files/appresizer.conf";

    private static final int METHOD_CONFIGURATION = 0;
    private static final int METHOD_METRICS = 1;
    private static final int METHOD_RESOURCES_MANAGER = 2;
    private static final int METHOD_ACTIVITY = 3;

    private static String sFramework = "Unknown";

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {
        sFramework = detectFramework();
        XposedBridge.log("AppResizer zygote ready, framework=" + sFramework);
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (lpparam == null || lpparam.packageName == null) {
            return;
        }
        if (SELF_PACKAGE.equals(lpparam.packageName)) {
            hookSelf(lpparam);
            return;
        }
        if ("android".equals(lpparam.packageName)) {
            return;
        }
        XSharedPreferences prefs = readPreferences();
        if (prefs == null) {
            return;
        }
        int dpi = getInt(prefs, "dpi_" + lpparam.packageName, 0);
        int width = getInt(prefs, "width_" + lpparam.packageName, 0);
        int height = getInt(prefs, "height_" + lpparam.packageName, 0);
        if (dpi <= 0 && width <= 0 && height <= 0) {
            return;
        }
        int method = getInt(prefs, "method_" + lpparam.packageName, -1);
        if (method < METHOD_CONFIGURATION || method > METHOD_ACTIVITY) {
            method = getInt(prefs, KEY_DPI_METHOD, METHOD_CONFIGURATION);
        }
        XposedBridge.log("AppResizer apply " + lpparam.packageName + " dpi=" + dpi + " width=" + width + " height=" + height + " method=" + method);
        hookPackage(lpparam, dpi, width, height, method);
    }

    private XSharedPreferences readPreferences() {
        try {
            XSharedPreferences prefs = new XSharedPreferences(SELF_PACKAGE, PREFS_NAME);
            prefs.reload();
            // Xposed API 82 的 XSharedPreferences 没有 isReadable() 方法；
            // 若目标进程读不到配置，getInt 默认返回 0，不影响功能。
            return prefs;
        } catch (Throwable throwable) {
            XposedBridge.log(throwable);
            return null;
        }
    }

    private int getInt(XSharedPreferences prefs, String key, int defaultValue) {
        if (prefs != null) {
            try {
                if (prefs.contains(key)) {
                    return prefs.getInt(key, defaultValue);
                }
            } catch (Throwable ignored) {
            }
        }
        String mirror = readMirror(key);
        if (mirror != null) {
            try {
                return Integer.parseInt(mirror.trim());
            } catch (Throwable ignored) {
            }
        }
        return defaultValue;
    }

    private String readMirror(String key) {
        java.io.BufferedReader reader = null;
        try {
            reader = new java.io.BufferedReader(new java.io.InputStreamReader(new java.io.FileInputStream(MIRROR_PATH), "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                int index = line.indexOf('=');
                if (index > 0 && key.equals(line.substring(0, index))) {
                    return line.substring(index + 1);
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
        return null;
    }

    private void hookSelf(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod("com.appresizer.MainActivity", lpparam.classLoader, "isModuleActive", new XC_MethodReplacement() {
                @Override
                protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                    return Boolean.TRUE;
                }
            });
        } catch (Throwable throwable) {
            XposedBridge.log(throwable);
        }
        try {
            XposedHelpers.findAndHookMethod("com.appresizer.MainActivity", lpparam.classLoader, "getFrameworkName", new XC_MethodReplacement() {
                @Override
                protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                    return sFramework;
                }
            });
        } catch (Throwable throwable) {
            XposedBridge.log(throwable);
        }
    }

    private void hookPackage(XC_LoadPackage.LoadPackageParam lpparam, final int dpi, final int width, final int height, int method) {
        switch (method) {
            case METHOD_CONFIGURATION:
                hookUpdateConfiguration(lpparam, dpi, width, height);
                break;
            case METHOD_METRICS:
                hookDisplayMetrics(lpparam, dpi, width, height);
                break;
            case METHOD_RESOURCES_MANAGER:
                hookResourcesManager(lpparam, dpi, width, height);
                break;
            case METHOD_ACTIVITY:
                hookActivityLevel(lpparam, dpi, width, height);
                break;
            default:
                hookUpdateConfiguration(lpparam, dpi, width, height);
                break;
        }
    }

    private void hookActivityLevel(final XC_LoadPackage.LoadPackageParam lpparam, final int dpi, final int width, final int height) {
        XC_MethodHook activityHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                Object activity = param.thisObject;
                if (activity instanceof Activity) {
                    applyToResources(((Activity) activity).getResources(), dpi, width, height);
                }
            }
        };
        try {
            // android.app.ActivityThread 是隐藏类，不能编译期直接引用，运行时经 classloader 反射加载。
            Class<?> activityThreadClass = Activity.class.getClassLoader().loadClass("android.app.ActivityThread");
            XposedHelpers.findAndHookMethod(Activity.class, "attach", android.content.Context.class, activityThreadClass,
                    android.app.Instrumentation.class, android.os.IBinder.class, int.class, boolean.class, Configuration.class, activityHook);
        } catch (Throwable throwable) {
            try {
                XposedBridge.hookAllMethods(Activity.class, "attach", activityHook);
            } catch (Throwable inner) {
                XposedBridge.log(inner);
            }
        }
        try {
            XposedBridge.hookAllMethods(Application.class, "attach", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    Object application = param.thisObject;
                    if (application instanceof Application) {
                        applyToResources(((Application) application).getResources(), dpi, width, height);
                    }
                }
            });
        } catch (Throwable throwable) {
            XposedBridge.log(throwable);
        }
        try {
            XposedBridge.hookAllMethods(Activity.class, "onConfigurationChanged", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    Object activity = param.thisObject;
                    if (activity instanceof Activity) {
                        applyToResources(((Activity) activity).getResources(), dpi, width, height);
                    }
                }
            });
        } catch (Throwable ignored) {
        }
    }

    private void applyToResources(Resources resources, int dpi, int width, int height) {
        if (resources == null) {
            return;
        }
        try {
            Configuration configuration = resources.getConfiguration();
            applyConfiguration(configuration, null, dpi, width, height);
            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        } catch (Throwable throwable) {
            XposedBridge.log(throwable);
        }
    }

    private void hookUpdateConfiguration(XC_LoadPackage.LoadPackageParam lpparam, final int dpi, final int width, final int height) {
        try {
            XposedHelpers.findAndHookMethod(Resources.class, "updateConfiguration", Configuration.class, DisplayMetrics.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    applyConfiguration((Configuration) param.args[0], (DisplayMetrics) param.args[1], dpi, width, height);
                }
            });
        } catch (Throwable throwable) {
            XposedBridge.log(throwable);
        }
        if (Build.VERSION.SDK_INT >= 17) {
            try {
                XposedHelpers.findAndHookMethod(Resources.class, "updateConfiguration", Configuration.class, DisplayMetrics.class, Configuration.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        applyConfiguration((Configuration) param.args[0], (DisplayMetrics) param.args[1], dpi, width, height);
                    }
                });
            } catch (Throwable ignored) {
            }
        }
    }

    private void hookDisplayMetrics(XC_LoadPackage.LoadPackageParam lpparam, final int dpi, final int width, final int height) {
        try {
            XposedHelpers.findAndHookMethod(Display.class, "getMetrics", DisplayMetrics.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    applyMetrics((DisplayMetrics) param.args[0], dpi, width, height);
                }
            });
        } catch (Throwable ignored) {
        }
        try {
            XposedHelpers.findAndHookMethod(Display.class, "getRealMetrics", DisplayMetrics.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    applyMetrics((DisplayMetrics) param.args[0], dpi, width, height);
                }
            });
        } catch (Throwable ignored) {
        }
        try {
            XposedHelpers.findAndHookMethod(Resources.class, "getDisplayMetrics", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    applyMetrics((DisplayMetrics) param.getResult(), dpi, width, height);
                }
            });
        } catch (Throwable ignored) {
        }
    }

    private void hookResourcesManager(XC_LoadPackage.LoadPackageParam lpparam, final int dpi, final int width, final int height) {
        Class<?> managerClass = findClass("android.app.ResourcesManager", lpparam.classLoader);
        if (managerClass == null) {
            XposedBridge.log("AppResizer ResourcesManager not visible, fallback to configuration injection");
            hookUpdateConfiguration(lpparam, dpi, width, height);
            return;
        }
        boolean hooked = false;
        for (Method candidate : managerClass.getDeclaredMethods()) {
            if (!"applyConfigurationToResourcesLocked".equals(candidate.getName())) {
                continue;
            }
            Class<?>[] types = candidate.getParameterTypes();
            if (types.length < 1 || !Configuration.class.isAssignableFrom(types[0])) {
                continue;
            }
            XposedBridge.hookMethod(candidate, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    for (Object arg : param.args) {
                        if (arg instanceof Configuration) {
                            applyConfiguration((Configuration) arg, null, dpi, width, height);
                        }
                    }
                }
            });
            hooked = true;
        }
        if (!hooked) {
            XposedBridge.log("AppResizer applyConfigurationToResourcesLocked not found, fallback to configuration injection");
            hookUpdateConfiguration(lpparam, dpi, width, height);
        }
    }

    private Class<?> findClass(String name, ClassLoader classLoader) {
        try {
            return Class.forName(name, false, classLoader);
        } catch (Throwable ignored) {
        }
        try {
            return Class.forName(name);
        } catch (Throwable ignored) {
        }
        try {
            return XposedHelpers.findClass(name, classLoader);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private void applyConfiguration(Configuration config, DisplayMetrics metrics, int dpi, int width, int height) {
        if (config != null) {
            if (dpi > 0) {
                config.densityDpi = dpi;
            }
            if (width > 0) {
                config.screenWidthDp = dpi > 0 ? Math.round(width * 160f / dpi) : width;
            }
            if (height > 0) {
                config.screenHeightDp = dpi > 0 ? Math.round(height * 160f / dpi) : height;
            }
            if (width > 0 && height > 0) {
                if (Build.VERSION.SDK_INT >= 26) {
                    config.smallestScreenWidthDp = Math.min(width, height);
                    config.screenLayout = (config.screenLayout & ~Configuration.SCREENLAYOUT_SIZE_MASK) | resolveScreenSize(width, height);
                } else if (Build.VERSION.SDK_INT >= 24) {
                    config.smallestScreenWidthDp = Math.min(width, height);
                    config.screenLayout = (config.screenLayout & ~Configuration.SCREENLAYOUT_SIZE_MASK) | resolveScreenSize(width, height);
                } else {
                    config.screenLayout = (config.screenLayout & ~Configuration.SCREENLAYOUT_SIZE_MASK) | resolveScreenSize(width, height);
                }
            }
        }
        if (metrics != null) {
            applyMetrics(metrics, dpi, width, height);
        }
    }

    private void applyMetrics(DisplayMetrics metrics, int dpi, int width, int height) {
        if (metrics == null) {
            return;
        }
        if (dpi > 0) {
            float fontScale = metrics.density > 0f ? metrics.scaledDensity / metrics.density : 1f;
            metrics.densityDpi = dpi;
            metrics.density = dpi / 160f;
            metrics.scaledDensity = metrics.density * fontScale;
        }
        if (width > 0) {
            metrics.widthPixels = width;
        }
        if (height > 0) {
            metrics.heightPixels = height;
        }
    }

    private int resolveScreenSize(int width, int height) {
        int min = Math.min(width, height);
        if (min >= 960) {
            return Configuration.SCREENLAYOUT_SIZE_XLARGE;
        }
        if (min >= 720) {
            return Configuration.SCREENLAYOUT_SIZE_LARGE;
        }
        if (min >= 480) {
            return Configuration.SCREENLAYOUT_SIZE_NORMAL;
        }
        return Configuration.SCREENLAYOUT_SIZE_SMALL;
    }

    private String detectFramework() {
        if (classExists("org.lsposed.lspd.core.Main") || classExists("org.lsposed.lspd.BuildConfig")) {
            return "LSPosed";
        }
        if (classExists("de.robv.android.xposed.XposedBridge")) {
            if (isEdXposed()) {
                return "EdXposed";
            }
            return "Xposed";
        }
        return "Unknown";
    }

    private boolean isEdXposed() {
        try {
            Class<?> bridge = Class.forName("de.robv.android.xposed.XposedBridge");
            Method method = bridge.getDeclaredMethod("getXposedVersion");
            method.setAccessible(true);
            Object version = method.invoke(null);
            if (version != null) {
                int value = Integer.parseInt(String.valueOf(version));
                return value >= 93;
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    private boolean classExists(String name) {
        try {
            Class.forName(name, false, XposedHook.class.getClassLoader());
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
