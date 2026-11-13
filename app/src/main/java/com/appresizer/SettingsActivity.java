package com.appresizer;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

public class SettingsActivity extends BaseActivity {

    private ConfigManager configManager;
    private TextView defaultDpiValue;
    private TextView defaultWidthValue;
    private TextView defaultHeightValue;
    private TextView dpiMethodValue;
    private TextView killMethodValue;
    private TextView listModeValue;
    private TextView languageValue;
    private SwitchCompat showSystemSwitch;

    private static final String[] LANG_VALUES = {
            ConfigManager.LANG_SYSTEM,
            ConfigManager.LANG_ZH,
            ConfigManager.LANG_EN,
            ConfigManager.LANG_JA
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configManager = ConfigManager.get(this);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title_settings);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        defaultDpiValue = (TextView) findViewById(R.id.settings_default_dpi_value);
        defaultWidthValue = (TextView) findViewById(R.id.settings_default_width_value);
        defaultHeightValue = (TextView) findViewById(R.id.settings_default_height_value);
        dpiMethodValue = (TextView) findViewById(R.id.settings_dpi_method_value);
        killMethodValue = (TextView) findViewById(R.id.settings_kill_method_value);
        listModeValue = (TextView) findViewById(R.id.settings_list_mode_value);
        languageValue = (TextView) findViewById(R.id.settings_language_value);
        showSystemSwitch = (SwitchCompat) findViewById(R.id.settings_show_system_switch);

        findViewById(R.id.settings_default_dpi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogUtils.showNumberDialog(SettingsActivity.this, R.string.default_dpi_reference,
                        configManager.getDefaultDpi(), ConfigManager.DPI_MIN, ConfigManager.DPI_MAX,
                        new DialogUtils.OnNumberResult() {
                            @Override
                            public void onResult(int value) {
                                configManager.setDefaultDpi(value);
                                refresh();
                            }
                        });
            }
        });

        findViewById(R.id.settings_default_width).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogUtils.showNumberDialog(SettingsActivity.this, R.string.default_width_reference,
                        configManager.getDefaultWidth(), ConfigManager.SIZE_MIN, ConfigManager.SIZE_MAX,
                        new DialogUtils.OnNumberResult() {
                            @Override
                            public void onResult(int value) {
                                configManager.setDefaultWidth(value);
                                refresh();
                            }
                        });
            }
        });

        findViewById(R.id.settings_default_height).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogUtils.showNumberDialog(SettingsActivity.this, R.string.default_height_reference,
                        configManager.getDefaultHeight(), ConfigManager.SIZE_MIN, ConfigManager.SIZE_MAX,
                        new DialogUtils.OnNumberResult() {
                            @Override
                            public void onResult(int value) {
                                configManager.setDefaultHeight(value);
                                refresh();
                            }
                        });
            }
        });

        findViewById(R.id.settings_dpi_method).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMethodDialog();
            }
        });

        findViewById(R.id.settings_kill_method).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showKillDialog();
            }
        });

        findViewById(R.id.settings_list_mode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showListModeDialog();
            }
        });

        findViewById(R.id.settings_language).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLanguageDialog();
            }
        });

        showSystemSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                configManager.setShowSystem(isChecked);
            }
        });
        findViewById(R.id.settings_show_system).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSystemSwitch.setChecked(!showSystemSwitch.isChecked());
            }
        });

        refresh();
    }

    private void refresh() {
        int dpi = configManager.getDefaultDpi();
        defaultDpiValue.setText(dpi > 0 ? String.valueOf(dpi) : getString(R.string.unconfigured));
        int width = configManager.getDefaultWidth();
        defaultWidthValue.setText(width > 0 ? String.valueOf(width) : getString(R.string.unconfigured));
        int height = configManager.getDefaultHeight();
        defaultHeightValue.setText(height > 0 ? String.valueOf(height) : getString(R.string.unconfigured));
        dpiMethodValue.setText(methodName(configManager.getDpiMethod()));
        killMethodValue.setText(killName(configManager.getKillMethod()));
        listModeValue.setText(configManager.getListMode() == ConfigManager.LIST_MODE_ROOT
                ? getString(R.string.list_mode_root) : getString(R.string.list_mode_system));
        languageValue.setText(langName(configManager.getLang()));
        showSystemSwitch.setChecked(configManager.isShowSystem());
    }

    private String[] langNames() {
        return new String[]{
                getString(R.string.language_system),
                getString(R.string.language_zh),
                getString(R.string.language_en),
                getString(R.string.language_ja)
        };
    }

    private int langIndex(String lang) {
        for (int i = 0; i < LANG_VALUES.length; i++) {
            if (LANG_VALUES[i].equals(lang)) {
                return i;
            }
        }
        return 0;
    }

    private String langName(String lang) {
        return langNames()[langIndex(lang)];
    }

    private void showLanguageDialog() {
        final String[] names = langNames();
        int checked = langIndex(configManager.getLang());
        DialogUtils.showChoice(this, R.string.language, names, checked, new DialogUtils.OnNumberResult() {
            @Override
            public void onResult(int value) {
                configManager.setLang(LANG_VALUES[value]);
                // 切换语言后重建界面以立即应用新语言。
                recreate();
            }
        });
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

    private String killName(int value) {
        String[] names = getResources().getStringArray(R.array.kill_method_names);
        if (value >= 0 && value < names.length) {
            return names[value];
        }
        return names[0];
    }

    private void showMethodDialog() {
        String[] names = getResources().getStringArray(R.array.dpi_method_names);
        int checked = 0;
        int current = configManager.getDpiMethod();
        for (int i = 0; i < ConfigManager.METHOD_VALUES.length; i++) {
            if (ConfigManager.METHOD_VALUES[i] == current) {
                checked = i;
            }
        }
        DialogUtils.showChoice(this, R.string.select_mode_title, names, checked, new DialogUtils.OnNumberResult() {
            @Override
            public void onResult(int value) {
                configManager.setDpiMethod(ConfigManager.METHOD_VALUES[value]);
                refresh();
            }
        });
    }

    private void showKillDialog() {
        String[] names = getResources().getStringArray(R.array.kill_method_names);
        int checked = configManager.getKillMethod();
        if (checked < 0 || checked >= names.length) {
            checked = 0;
        }
        DialogUtils.showChoice(this, R.string.kill_method, names, checked, new DialogUtils.OnNumberResult() {
            @Override
            public void onResult(int value) {
                configManager.setKillMethod(value);
                refresh();
            }
        });
    }

    private void showListModeDialog() {
        final String[] names = {
                getString(R.string.list_mode_system),
                getString(R.string.list_mode_root)
        };
        int checked = configManager.getListMode() == ConfigManager.LIST_MODE_ROOT ? 1 : 0;
        DialogUtils.showChoice(this, R.string.select_list_mode_title, names, checked, new DialogUtils.OnNumberResult() {
            @Override
            public void onResult(int value) {
                configManager.setListMode(value == 1 ? ConfigManager.LIST_MODE_ROOT : ConfigManager.LIST_MODE_SYSTEM);
                refresh();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
