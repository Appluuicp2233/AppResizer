package com.appresizer;

public final class EnvChecker {

    public static final int STATE_ACTIVE = 0;
    public static final int STATE_NOT_HOOKED = 1;
    public static final int STATE_NO_FRAMEWORK = 2;

    private EnvChecker() {
    }

    public static boolean isFrameworkPresent() {
        try {
            Class.forName("de.robv.android.xposed.XposedBridge", false, EnvChecker.class.getClassLoader());
            return true;
        } catch (Throwable throwable) {
            return false;
        }
    }

    public static boolean isRootAvailable() {
        return ShellUtils.hasRoot();
    }

    public static int check() {
        if (MainActivity.isModuleActive()) {
            return STATE_ACTIVE;
        }
        if (isFrameworkPresent()) {
            return STATE_NOT_HOOKED;
        }
        return STATE_NO_FRAMEWORK;
    }
}
