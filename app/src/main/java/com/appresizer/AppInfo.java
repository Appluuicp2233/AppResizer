package com.appresizer;

import android.graphics.drawable.Drawable;

public class AppInfo {

    public String label;
    public String packageName;
    public Drawable icon;
    public boolean system;
    public boolean configured;
    public int dpi;
    public int width;
    public int height;
    public int method;

    public AppInfo(String label, String packageName, Drawable icon, boolean system) {
        this.label = label;
        this.packageName = packageName;
        this.icon = icon;
        this.system = system;
    }

    public boolean hasConfig() {
        return dpi > 0 || width > 0 || height > 0;
    }
}
