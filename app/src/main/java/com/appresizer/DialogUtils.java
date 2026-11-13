package com.appresizer;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public final class DialogUtils {

    public interface OnNumberResult {
        void onResult(int value);
    }

    public interface OnSizeResult {
        void onResult(int width, int height);
    }

    private static AlertDialog progressDialog;

    private DialogUtils() {
    }

    public static Context wrap(Context context) {
        // 只做 200 DPI 密度覆盖，不再套 ContextThemeWrapper。
        // 原因：在部分设备上，给 AlertDialog 套 ContextThemeWrapper(AppTheme_Dialog)+createConfigurationContext
        // 会让弹窗 .show() 时崩溃。传进来的 Activity（this）的 base 已由 BaseActivity 强制为 200 DPI，
        // 其主题也已声明 alertDialogTheme，直接用原 context 即可保持 200 DPI 且正常显示。
        try {
            Configuration configuration = new Configuration(context.getResources().getConfiguration());
            if (configuration.densityDpi == BaseActivity.APP_DENSITY) {
                return context;
            }
            configuration.densityDpi = BaseActivity.APP_DENSITY;
            return context.createConfigurationContext(configuration);
        } catch (Throwable ignored) {
            return context;
        }
    }

    public static String version(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Throwable throwable) {
            return "2.0.0";
        }
    }

    // 纯文字标题（无图标、无应用名版本）。
    public static View title(Context context, int titleRes) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_title_text, null);
        TextView text = (TextView) view.findViewById(R.id.dialog_title_text);
        if (titleRes != 0) {
            text.setText(titleRes);
        } else {
            text.setText(R.string.app_name);
        }
        return view;
    }

    // 选择类弹窗：点选项只切换选中，点“确定”才生效，“取消”取消。
    public static void showChoice(Context context, int titleRes, CharSequence[] items, int checkedIndex,
                                  final OnNumberResult callback) {
        final Context ctx = wrap(context);
        final int[] checked = {checkedIndex};
        final AlertDialog dialog = new AlertDialog.Builder(ctx)
                .setCustomTitle(title(ctx, titleRes))
                .setSingleChoiceItems(items, checkedIndex, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        checked[0] = which;
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        if (callback != null) {
                            callback.onResult(checked[0]);
                        }
                    }
                })
                .create();
        applyFullscreen(ctx, dialog);
        dialog.show();
    }

    // 选择类弹窗（仅确定，无取消）：点选项只切换选中，点“确定”才生效。
    public static void showChoiceNoCancel(Context context, int titleRes, CharSequence[] items, int checkedIndex,
                                          final OnNumberResult callback) {
        final Context ctx = wrap(context);
        final int[] checked = {checkedIndex};
        final AlertDialog dialog = new AlertDialog.Builder(ctx)
                .setCustomTitle(title(ctx, titleRes))
                .setSingleChoiceItems(items, checkedIndex, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        checked[0] = which;
                    }
                })
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        if (callback != null) {
                            callback.onResult(checked[0]);
                        }
                    }
                })
                .create();
        applyFullscreen(ctx, dialog);
        dialog.show();
    }

    public static void showProgress(Context context, String text) {
        dismissProgress();
        Context ctx = wrap(context);
        View view = LayoutInflater.from(ctx).inflate(R.layout.dialog_progress, null);
        ((TextView) view.findViewById(R.id.dialog_progress_text)).setText(text);
        progressDialog = new AlertDialog.Builder(ctx)
                .setView(view)
                .setCancelable(false)
                .create();
        progressDialog.show();
    }

    // 横向进度条弹窗，用于加载应用列表。
    public static void showProgressHorizontal(Context context, String text) {
        dismissProgress();
        Context ctx = wrap(context);
        View view = LayoutInflater.from(ctx).inflate(R.layout.dialog_progress_horizontal, null);
        ((TextView) view.findViewById(R.id.dialog_progress_text)).setText(text);
        progressDialog = new AlertDialog.Builder(ctx)
                .setView(view)
                .setCancelable(false)
                .create();
        progressDialog.show();
    }

    public static void dismissProgress() {
        if (progressDialog != null) {
            try {
                progressDialog.dismiss();
            } catch (Throwable ignored) {
            }
            progressDialog = null;
        }
    }

    public static void showAbout(Context context) {
        Context ctx = wrap(context);
        View view = LayoutInflater.from(ctx).inflate(R.layout.dialog_about, null);
        ((ImageView) view.findViewById(R.id.about_icon)).setImageResource(R.mipmap.ic_launcher);
        ((TextView) view.findViewById(R.id.about_name)).setText(R.string.app_name);
        ((TextView) view.findViewById(R.id.about_version)).setText("(" + version(context) + ")");
        ((TextView) view.findViewById(R.id.about_details)).setText(buildAboutText(context));
        final AlertDialog dialog = new AlertDialog.Builder(ctx)
                .setCustomTitle(title(ctx, R.string.title_about))
                .setView(view)
                .setPositiveButton(R.string.confirm, null)
                .create();
        applyFullscreen(ctx, dialog);
        dialog.show();
    }

    private static String buildAboutText(Context context) {
        StringBuilder builder = new StringBuilder();
        builder.append(context.getString(R.string.about_version)).append("：").append(version(context));
        builder.append("\n").append(context.getString(R.string.about_author)).append("：").append(context.getString(R.string.app_author));
        builder.append("\n").append(context.getString(R.string.about_owner)).append("：").append(context.getString(R.string.app_owner));
        builder.append("\n").append(context.getString(R.string.about_license)).append("：MIT License");
        builder.append("\n").append(context.getString(R.string.about_github)).append("：").append(ConfigManager.GITHUB_REPO_URL);
        builder.append("\n").append(context.getString(R.string.about_update)).append("：").append(context.getString(R.string.app_update_date));
        String framework = MainActivity.getFrameworkName();
        if (framework == null || framework.trim().isEmpty()) {
            framework = context.getString(R.string.about_active_no);
        }
        builder.append("\n").append(context.getString(R.string.about_framework)).append("：").append(framework);
        builder.append("\n").append(context.getString(R.string.about_android)).append("：Android ").append(androidVersion(Build.VERSION.SDK_INT));
        builder.append("\n").append(context.getString(R.string.about_active)).append("：")
                .append(context.getString(MainActivity.isModuleActive() ? R.string.about_active_yes : R.string.about_active_no));
        builder.append("\n\n").append(context.getString(R.string.contact_body));
        return builder.toString();
    }

    public static String androidVersion(int sdk) {
        switch (sdk) {
            case 24:
                return "7.0";
            case 25:
                return "7.1";
            case 26:
                return "8.0";
            case 27:
                return "8.1";
            case 28:
                return "9";
            case 29:
                return "10";
            case 30:
                return "11";
            case 31:
                return "12";
            case 32:
                return "12L";
            case 33:
                return "13";
            case 34:
                return "14";
            default:
                return String.valueOf(sdk);
        }
    }

    public static void showContact(Context context) {
        Context ctx = wrap(context);
        View view = LayoutInflater.from(ctx).inflate(R.layout.dialog_body, null);
        ((TextView) view.findViewById(R.id.dialog_body)).setText(R.string.contact_body);
        final AlertDialog dialog = new AlertDialog.Builder(ctx)
                .setCustomTitle(title(ctx, R.string.title_contact))
                .setView(view)
                .setPositiveButton(R.string.confirm, null)
                .create();
        applyFullscreen(ctx, dialog);
        dialog.show();
    }

    public static void confirm(Context context, int titleRes, int bodyRes, final Runnable onConfirm) {
        Context ctx = wrap(context);
        View view = LayoutInflater.from(ctx).inflate(R.layout.dialog_body, null);
        ((TextView) view.findViewById(R.id.dialog_body)).setText(bodyRes);
        final AlertDialog dialog = new AlertDialog.Builder(ctx)
                .setCustomTitle(title(ctx, titleRes))
                .setView(view)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (onConfirm != null) {
                            onConfirm.run();
                        }
                    }
                })
                .create();
        applyFullscreen(ctx, dialog);
        dialog.show();
    }

    public static void showDpiDialog(Context context, int initial, final OnNumberResult callback) {
        showNumberDialog(context, R.string.dpi_dialog_title, initial, ConfigManager.DPI_MIN, ConfigManager.DPI_MAX, callback);
    }

    public static void showNumberDialog(Context context, int titleRes, int initial, final int min, final int max,
                                        final OnNumberResult callback) {
        final Context ctx = wrap(context);
        final View view = LayoutInflater.from(ctx).inflate(R.layout.dialog_dpi, null);
        final EditText input = (EditText) view.findViewById(R.id.dpi_input);
        final TextView error = (TextView) view.findViewById(R.id.dpi_error);
        if (initial > 0) {
            input.setText(String.valueOf(initial));
            input.setSelection(input.getText().length());
        }
        final AlertDialog dialog = new AlertDialog.Builder(ctx)
                .setCustomTitle(title(ctx, titleRes))
                .setView(view)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.confirm, null)
                .create();
        applyFullscreen(ctx, dialog);
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = parseInt(input);
                if (value < min || value > max) {
                    error.setText(R.string.invalid_range);
                    error.setVisibility(View.VISIBLE);
                    return;
                }
                dialog.dismiss();
                if (callback != null) {
                    callback.onResult(value);
                }
            }
        });
    }

    private static int parseInt(EditText input) {
        try {
            return Integer.parseInt(input.getText().toString().trim());
        } catch (Throwable throwable) {
            return 0;
        }
    }

    public static void showSizeDialog(Context context, int width, int height, final OnSizeResult callback) {
        final Context ctx = wrap(context);
        final View view = LayoutInflater.from(ctx).inflate(R.layout.dialog_size, null);
        final EditText widthInput = (EditText) view.findViewById(R.id.width_input);
        final EditText heightInput = (EditText) view.findViewById(R.id.height_input);
        final TextView error = (TextView) view.findViewById(R.id.size_error);
        if (width > 0) {
            widthInput.setText(String.valueOf(width));
        }
        if (height > 0) {
            heightInput.setText(String.valueOf(height));
        }
        final AlertDialog dialog = new AlertDialog.Builder(ctx)
                .setCustomTitle(title(ctx, R.string.size_dialog_title))
                .setView(view)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.confirm, null)
                .create();
        applyFullscreen(ctx, dialog);
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int w = parseInt(widthInput);
                int h = parseInt(heightInput);
                if (w < ConfigManager.SIZE_MIN || w > ConfigManager.SIZE_MAX
                        || h < ConfigManager.SIZE_MIN || h > ConfigManager.SIZE_MAX) {
                    error.setText(R.string.invalid_size);
                    error.setVisibility(View.VISIBLE);
                    return;
                }
                dialog.dismiss();
                if (callback != null) {
                    callback.onResult(w, h);
                }
            }
        });
    }

    private static void applyFullscreen(Context context, AlertDialog dialog) {
        try {
            if (ConfigManager.get(context).isFullscreenDialog() && dialog != null && dialog.getWindow() != null) {
                WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
                params.width = WindowManager.LayoutParams.MATCH_PARENT;
                params.height = WindowManager.LayoutParams.MATCH_PARENT;
                dialog.getWindow().setAttributes(params);
            }
        } catch (Throwable ignored) {
        }
    }
}
