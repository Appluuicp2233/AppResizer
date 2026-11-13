package com.appresizer;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;

/**
 * 崩溃界面：同 App 风格（深色、顶部栏无阴影），展示应用日志，可复制/导出，并提示联系 QQ。
 */
public class CrashActivity extends BaseActivity {

    public static final String EXTRA_LOG = "crash_log";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.crash_title);
            actionBar.setDisplayHomeAsUpEnabled(false);
        }

        final String log = getIntent().getStringExtra(EXTRA_LOG);
        final String displayLog = (log != null && !log.isEmpty()) ? log : getString(R.string.crash_empty);

        ((TextView) findViewById(R.id.crash_log)).setText(displayLog);

        findViewById(R.id.crash_copy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copy(displayLog);
            }
        });
        findViewById(R.id.crash_export).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                export(displayLog);
            }
        });
        findViewById(R.id.crash_exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                System.exit(0);
            }
        });
    }

    private void copy(String log) {
        try {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("crash", log));
            toast(R.string.crash_copied);
        } catch (Throwable ignored) {
        }
    }

    private void export(String log) {
        try {
            File dir = getExternalFilesDir(null);
            File file = (dir != null) ? new File(dir, "appresizer_crash.txt") : new File(getFilesDir(), "appresizer_crash.txt");
            FileWriter fw = new FileWriter(file);
            fw.write(log);
            fw.close();
            toast(getString(R.string.crash_exported) + "：" + file.getAbsolutePath());
        } catch (Throwable ignored) {
        }
    }

    private void toast(String message) {
        try {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        } catch (Throwable ignored) {
        }
    }

    private void toast(int res) {
        try {
            Toast.makeText(this, res, Toast.LENGTH_SHORT).show();
        } catch (Throwable ignored) {
        }
    }
}
