package com.appresizer;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    public static final int APP_DENSITY = 200;

    @Override
    protected void attachBaseContext(Context newBase) {
        String lang = ConfigManager.get(newBase).getLang();
        super.attachBaseContext(LocaleUtils.wrap(newBase, lang));
    }
}
