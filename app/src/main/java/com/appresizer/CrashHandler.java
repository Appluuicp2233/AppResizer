package com.appresizer;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 全局未捕获异常处理器：捕获崩溃 → 保存日志 → 启动崩溃界面，避免直接闪退白屏。
 */
public final class CrashHandler implements Thread.UncaughtExceptionHandler {

    private static final String CRASH_FILE = "crash_log.txt";

    private final Context context;

    public CrashHandler(Context context) {
        this.context = context;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        final String log = buildLog(thread, ex);
        saveLog(log);
        try {
            Intent intent = new Intent(context, CrashActivity.class);
            intent.putExtra(CrashActivity.EXTRA_LOG, log);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        } catch (Throwable ignored) {
        }
        // 保持进程存活，让崩溃界面得以绘制与停留；退出由崩溃界面的“退出”按钮完成。
        for (; ; ) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private String buildLog(Thread thread, Throwable ex) {
        StringBuilder sb = new StringBuilder();
        sb.append("AppResizer Crash Log\n");
        sb.append("Time: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append('\n');
        sb.append("Android: ").append(Build.VERSION.RELEASE).append(" (API ").append(Build.VERSION.SDK_INT).append(")\n");
        sb.append("Device: ").append(Build.MANUFACTURER).append(' ').append(Build.MODEL).append('\n');
        sb.append("Thread: ").append(thread == null ? "?" : thread.getName()).append('\n');
        sb.append('\n');
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        if (ex != null) {
            ex.printStackTrace(pw);
        }
        pw.flush();
        sb.append(sw.toString());
        return sb.toString();
    }

    private void saveLog(String log) {
        try {
            File dir = context.getFilesDir();
            File file = new File(dir, CRASH_FILE);
            FileWriter fw = new FileWriter(file);
            fw.write(log);
            fw.close();
        } catch (Throwable ignored) {
        }
    }

    public static String readLastLog(Context context) {
        try {
            File file = new File(context.getFilesDir(), CRASH_FILE);
            if (file.exists()) {
                StringBuilder sb = new StringBuilder();
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }
                reader.close();
                return sb.toString();
            }
        } catch (Throwable ignored) {
        }
        return null;
    }
}
