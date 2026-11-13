package com.appresizer;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class AppSettingsActivity extends BaseActivity {

    public static final String EXTRA_PACKAGE = "package_name";

    private ConfigManager configManager;
    private String packageName;
    private String appLabel;
    private Drawable appIcon;
    private int dpi;
    private int width;
    private int height;
    private int method;

    private TextView dpiValue;
    private TextView sizeValue;
    private TextView methodValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configManager = ConfigManager.get(this);
        packageName = getIntent().getStringExtra(EXTRA_PACKAGE);
        if (packageName == null) {
            finish();
            return;
        }
        setContentView(R.layout.activity_app_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        loadAppInfo();

        ImageView icon = (ImageView) findViewById(R.id.detail_icon);
        TextView name = (TextView) findViewById(R.id.detail_name);
        TextView pkg = (TextView) findViewById(R.id.detail_package);
        dpiValue = (TextView) findViewById(R.id.dpi_value);
        sizeValue = (TextView) findViewById(R.id.size_value);
        methodValue = (TextView) findViewById(R.id.method_value);

        icon.setImageDrawable(appIcon);
        name.setText(appLabel);
        pkg.setText(packageName);
        if (actionBar != null) {
            actionBar.setTitle(appLabel);
        }

        findViewById(R.id.detail_open).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTarget();
            }
        });
        findViewById(R.id.dpi_row).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogUtils.showDpiDialog(AppSettingsActivity.this, dpi > 0 ? dpi : configManager.getDefaultDpi(), new DialogUtils.OnNumberResult() {
                    @Override
                    public void onResult(int value) {
                        dpi = value;
                        updateValues();
                    }
                });
            }
        });
        findViewById(R.id.size_row).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int defaultWidth = configManager.getDefaultWidth();
                int defaultHeight = configManager.getDefaultHeight();
                DialogUtils.showSizeDialog(AppSettingsActivity.this,
                        width > 0 ? width : defaultWidth,
                        height > 0 ? height : defaultHeight,
                        new DialogUtils.OnSizeResult() {
                            @Override
                            public void onResult(int w, int h) {
                                width = w;
                                height = h;
                                updateValues();
                            }
                        });
            }
        });
        findViewById(R.id.method_row).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMethodDialog();
            }
        });

        loadValues();
    }

    private void loadAppInfo() {
        PackageManager packageManager = getPackageManager();
        try {
            ApplicationInfo info = packageManager.getApplicationInfo(packageName, 0);
            appLabel = String.valueOf(info.loadLabel(packageManager));
            appIcon = info.loadIcon(packageManager);
        } catch (Throwable throwable) {
            appLabel = packageName;
            appIcon = null;
        }
    }

    private void loadValues() {
        dpi = configManager.getDpi(packageName);
        width = configManager.getWidth(packageName);
        height = configManager.getHeight(packageName);
        method = configManager.getMethod(packageName);
        if (method < 0) {
            method = configManager.getDpiMethod();
        }
        updateValues();
    }

    private void updateValues() {
        if (dpi > 0) {
            dpiValue.setText(String.valueOf(dpi));
        } else {
            dpiValue.setText(R.string.unconfigured);
        }
        if (width > 0 && height > 0) {
            sizeValue.setText(width + " × " + height);
        } else {
            sizeValue.setText(R.string.unconfigured);
        }
        methodValue.setText(methodName(method));
    }

    private String methodName(int value) {
        String[] names = getResources().getStringArray(R.array.dpi_method_names);
        for (int i = 0; i < ConfigManager.METHOD_VALUES.length; i++) {
            if (ConfigManager.METHOD_VALUES[i] == value) {
                return names[i];
            }
        }
        return names[0];
    }

    private void showMethodDialog() {
        String[] names = getResources().getStringArray(R.array.dpi_method_names);
        int checked = 0;
        for (int i = 0; i < ConfigManager.METHOD_VALUES.length; i++) {
            if (ConfigManager.METHOD_VALUES[i] == method) {
                checked = i;
            }
        }
        DialogUtils.showChoice(this, R.string.select_mode_title, names, checked, new DialogUtils.OnNumberResult() {
            @Override
            public void onResult(int value) {
                method = ConfigManager.METHOD_VALUES[value];
                updateValues();
            }
        });
    }

    private void openTarget() {
        Intent launch = getPackageManager().getLaunchIntentForPackage(packageName);
        if (launch != null) {
            startActivity(launch);
        } else {
            Snackbar.make(findViewById(R.id.coordinator), R.string.open + " " + packageName, Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_app_settings, menu);
        // 右上角操作图标统一为白色。
        try {
            for (int i = 0; i < menu.size(); i++) {
                MenuItem item = menu.getItem(i);
                if (item.getIcon() != null) {
                    item.getIcon().setTint(android.graphics.Color.WHITE);
                }
            }
        } catch (Throwable ignored) {
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        if (id == R.id.action_save) {
            DialogUtils.confirm(this, R.string.save_confirm_title, R.string.save_confirm_body, new Runnable() {
                @Override
                public void run() {
                    saveAndApply();
                }
            });
            return true;
        }
        if (id == R.id.action_reset) {
            DialogUtils.confirm(this, R.string.reset_confirm_title, R.string.reset_confirm_body, new Runnable() {
                @Override
                public void run() {
                    doReset();
                }
            });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveAndApply() {
        if (dpi <= 0 || dpi < ConfigManager.DPI_MIN || dpi > ConfigManager.DPI_MAX) {
            dpi = configManager.getDefaultDpi();
        }
        final int finalDpi = dpi;
        final int finalWidth = width;
        final int finalHeight = height;
        final int finalMethod = method;
        DialogUtils.showProgress(this, getString(R.string.saving_apply));
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                configManager.saveConfig(packageName, finalDpi, finalWidth, finalHeight, finalMethod);
                killTarget();
                return Boolean.TRUE;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                DialogUtils.dismissProgress();
                if (isFinishing()) {
                    return;
                }
                Snackbar.make(findViewById(R.id.coordinator), R.string.save_done, Snackbar.LENGTH_SHORT).show();
            }
        }.execute();
    }

    private void doReset() {
        DialogUtils.showProgress(this, getString(R.string.resetting));
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                configManager.reset(packageName);
                killTarget();
                return Boolean.TRUE;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                DialogUtils.dismissProgress();
                if (isFinishing()) {
                    return;
                }
                loadValues();
                Snackbar.make(findViewById(R.id.coordinator), R.string.reset_done, Snackbar.LENGTH_SHORT).show();
            }
        }.execute();
    }

    private void killTarget() {
        int killMethod = configManager.getKillMethod();
        if (killMethod == ConfigManager.KILL_METHOD_NONE) {
            return;
        }
        if (killMethod == ConfigManager.KILL_METHOD_AM) {
            ShellUtils.execRoot("am force-stop " + packageName);
        } else {
            ShellUtils.execRoot("am force-stop " + packageName);
        }
    }
}
