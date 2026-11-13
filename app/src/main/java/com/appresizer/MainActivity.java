package com.appresizer;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int REQ_WIZARD = 1001;

    private static String sFramework = "";

    public static boolean isModuleActive() {
        return false;
    }

    public static String getFrameworkName() {
        return sFramework;
    }

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private View contentRoot;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private TextView banner;
    private AppListAdapter adapter;
    private ConfigManager configManager;
    private boolean ready = false;
    private boolean lastShowSystem = false;
    private AlertDialog progressDialog;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configManager = ConfigManager.get(this);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_name);
        }

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        contentRoot = findViewById(R.id.content_root);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.nav_open, R.string.nav_close);
        toggle.setDrawerIndicatorEnabled(true);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        banner = (TextView) findViewById(R.id.banner_inactive);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppListAdapter(new AppListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(AppInfo info) {
                Intent intent = new Intent(MainActivity.this, AppSettingsActivity.class);
                intent.putExtra(AppSettingsActivity.EXTRA_PACKAGE, info.packageName);
                startActivity(intent);
            }
        });
        adapter.setShowStatus(configManager.isShowStatus());
        recyclerView.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // 真正重新获取列表：清空当前数据 -> 重新扫描 -> 回填。
                loadApps(true, true);
            }
        });

        banner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                banner.setVisibility(View.GONE);
            }
        });

        lastShowSystem = configManager.isShowSystem();

        proceed();
    }

    private void proceed() {
        contentRoot.setVisibility(View.VISIBLE);
        // 流程：先选注入方式（仅确定），再选应用列表获取模式，再检测环境、启动服务、加载列表。
        if (!configManager.isModeChosen()) {
            showModeDialog(new Runnable() {
                @Override
                public void run() {
                    showListModeDialog(new Runnable() {
                        @Override
                        public void run() {
                            startEnvFlow();
                        }
                    });
                }
            });
        } else {
            startEnvFlow();
        }
    }

    private void startEnvFlow() {
        showProgress(getString(R.string.checking_env));
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                return EnvChecker.check();
            }

            @Override
            protected void onPostExecute(Integer state) {
                dismissProgress();
                if (isFinishing()) {
                    return;
                }
                if (state == EnvChecker.STATE_ACTIVE) {
                    onEnvReady();
                } else {
                    showEnvError(state);
                }
            }
        }.execute();
    }

    private void onEnvReady() {
        ready = true;
        updateActiveState();
        showProgress(getString(R.string.starting_service));
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dismissProgress();
                if (isFinishing()) {
                    return;
                }
                showSnack(R.string.service_started);
                loadApps(true);
            }
        }, 700);
    }

    private void showEnvError(final int state) {
        View view = getLayoutInflater().inflate(R.layout.dialog_body, null);
        String reason;
        if (state == EnvChecker.STATE_NOT_HOOKED) {
            reason = getString(R.string.env_error_inactive);
        } else if (!EnvChecker.isRootAvailable()) {
            reason = getString(R.string.env_error_root);
        } else {
            reason = getString(R.string.env_error_xposed);
        }
        ((TextView) view.findViewById(R.id.dialog_body)).setText(getString(R.string.env_error_body) + "\n\n" + reason);
        TextView solution = (TextView) view.findViewById(R.id.dialog_solution);
        solution.setText(R.string.env_solution);
        solution.setVisibility(View.VISIBLE);
        new AlertDialog.Builder(DialogUtils.wrap(this))
                .setCustomTitle(DialogUtils.title(this, 0))
                .setView(view)
                .setCancelable(false)
                .setNegativeButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        exitApp();
                    }
                })
                .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startEnvFlow();
                    }
                })
                .show();
    }

    private void showModeDialog(final Runnable thenRun) {
        final String[] names = getResources().getStringArray(R.array.dpi_method_names);
        int checked = 0;
        int current = configManager.getDpiMethod();
        for (int i = 0; i < ConfigManager.METHOD_VALUES.length; i++) {
            if (ConfigManager.METHOD_VALUES[i] == current) {
                checked = i;
            }
        }
        // 仅确定，无取消；默认选第一个。
        DialogUtils.showChoiceNoCancel(this, R.string.select_mode_title, names, checked, new DialogUtils.OnNumberResult() {
            @Override
            public void onResult(int value) {
                configManager.setDpiMethod(ConfigManager.METHOD_VALUES[value]);
                configManager.setModeChosen(true);
                if (thenRun != null) {
                    thenRun.run();
                }
            }
        });
    }

    private void showListModeDialog(final Runnable thenRun) {
        final String[] names = {
                getString(R.string.list_mode_system),
                getString(R.string.list_mode_root)
        };
        int checked = configManager.getListMode() == ConfigManager.LIST_MODE_ROOT ? 1 : 0;
        DialogUtils.showChoiceNoCancel(this, R.string.select_list_mode_title, names, checked, new DialogUtils.OnNumberResult() {
            @Override
            public void onResult(int value) {
                configManager.setListMode(value == 1 ? ConfigManager.LIST_MODE_ROOT : ConfigManager.LIST_MODE_SYSTEM);
                if (thenRun != null) {
                    thenRun.run();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!ready || isFinishing()) {
            return;
        }
        updateActiveState();
        boolean showSystem = configManager.isShowSystem();
        if (showSystem != lastShowSystem) {
            lastShowSystem = showSystem;
            loadApps(false);
            return;
        }
        adapter.setShowStatus(configManager.isShowStatus());
        if (adapter.getItemCount() > 0) {
            refreshStatus();
        }
    }

    private void updateActiveState() {
        boolean active = isModuleActive();
        if (active) {
            banner.setVisibility(View.GONE);
        } else {
            banner.setVisibility(View.VISIBLE);
        }
    }

    private void showProgress(String text) {
        dismissProgress();
        View view = getLayoutInflater().inflate(R.layout.dialog_progress, null);
        ((TextView) view.findViewById(R.id.dialog_progress_text)).setText(text);
        progressDialog = new AlertDialog.Builder(DialogUtils.wrap(this))
                .setView(view)
                .setCancelable(false)
                .create();
        progressDialog.show();
    }

    private void showProgressHorizontal(String text) {
        dismissProgress();
        View view = getLayoutInflater().inflate(R.layout.dialog_progress_horizontal, null);
        ((TextView) view.findViewById(R.id.dialog_progress_text)).setText(text);
        progressDialog = new AlertDialog.Builder(DialogUtils.wrap(this))
                .setView(view)
                .setCancelable(false)
                .create();
        progressDialog.show();
    }

    private void dismissProgress() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    private void loadApps(final boolean showDialog) {
        loadApps(showDialog, false);
    }

    /**
     * @param showDialog 是否显示"正在加载应用列表"进度弹窗
     * @param clearFirst 是否先清空列表（下拉刷新时使用，确保是真正重新获取而非保留旧数据）
     */
    private void loadApps(final boolean showDialog, final boolean clearFirst) {
        if (showDialog) {
            showProgressHorizontal(getString(R.string.loading_apps));
            swipeRefresh.setRefreshing(true);
        }
        if (clearFirst) {
            adapter.setItems(new ArrayList<AppInfo>());
        }
        final long startTime = System.currentTimeMillis();
        final int minDuration = showDialog ? 700 : 0;
        new AsyncTask<Void, Void, List<AppInfo>>() {
            @Override
            protected List<AppInfo> doInBackground(Void... params) {
                // 每次刷新都重新扫描，不复用任何缓存。
                return queryApps();
            }

            @Override
            protected void onPostExecute(List<AppInfo> result) {
                final Runnable finish = new Runnable() {
                    @Override
                    public void run() {
                        if (showDialog) {
                            dismissProgress();
                        }
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        if (swipeRefresh.isRefreshing()) {
                            swipeRefresh.setRefreshing(false);
                        }
                        adapter.setItems(result);
                        fadeInContent();
                    }
                };
                long elapsed = System.currentTimeMillis() - startTime;
                long remain = minDuration - elapsed;
                if (remain > 0) {
                    handler.postDelayed(finish, remain);
                } else {
                    finish.run();
                }
            }
        }.execute();
    }

    // 刷新/加载完成后，仅内容列表在 0.2 秒内淡入，顶部栏不闪。
    private void fadeInContent() {
        try {
            recyclerView.setAlpha(0f);
            recyclerView.animate().alpha(1f).setDuration(200).start();
        } catch (Throwable ignored) {
            recyclerView.setAlpha(1f);
        }
    }

    private List<AppInfo> queryApps() {
        PackageManager packageManager = getPackageManager();
        List<AppInfo> result = new ArrayList<AppInfo>();
        boolean showSystem = configManager.isShowSystem();
        boolean rootMode = configManager.getListMode() == ConfigManager.LIST_MODE_ROOT;

        List<ApplicationInfo> installed;
        if (rootMode) {
            installed = queryAppsByRoot(packageManager, showSystem);
        } else {
            installed = packageManager.getInstalledApplications(0);
        }

        for (ApplicationInfo info : installed) {
            boolean system = (info.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
            // 无论系统接口还是 root 接口，是否显示系统应用都由"显示系统应用"开关决定。
            if (system && !showSystem) {
                continue;
            }
            AppInfo item = new AppInfo(String.valueOf(info.loadLabel(packageManager)), info.packageName, info.loadIcon(packageManager), system);
            item.dpi = configManager.getDpi(info.packageName);
            item.width = configManager.getWidth(info.packageName);
            item.height = configManager.getHeight(info.packageName);
            item.configured = configManager.isConfigured(info.packageName);
            item.method = configManager.getMethod(info.packageName);
            result.add(item);
        }
        Collections.sort(result, new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo first, AppInfo second) {
                return first.label.compareToIgnoreCase(second.label);
            }
        });
        return result;
    }

    /**
     * root 模式：通过 su 执行 pm list packages 获取完整包名列表（可包含系统/隐藏应用），
     * 再逐个通过 PackageManager 解析应用信息。此方法在后台线程调用。
     */
    private List<ApplicationInfo> queryAppsByRoot(PackageManager packageManager, boolean showSystem) {
        List<ApplicationInfo> list = new ArrayList<ApplicationInfo>();
        java.util.Set<String> seen = new java.util.HashSet<String>();
        // -u 包含已卸载但保留数据的应用；不加 -s 以便同时拿到系统与第三方应用。
        ShellUtils.Result result = ShellUtils.execRoot("pm list packages -u");
        String output = result != null ? result.output : null;
        if (output == null || output.trim().isEmpty()) {
            // root 不可用或命令失败时，回退到系统接口，保证列表可用。
            return packageManager.getInstalledApplications(0);
        }
        String[] lines = output.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || !trimmed.startsWith("package:")) {
                continue;
            }
            String pkg = trimmed.substring("package:".length()).trim();
            if (pkg.isEmpty() || seen.contains(pkg)) {
                continue;
            }
            seen.add(pkg);
            try {
                ApplicationInfo info = packageManager.getApplicationInfo(pkg, 0);
                boolean system = (info.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
                if (system && !showSystem) {
                    continue;
                }
                list.add(info);
            } catch (Throwable ignored) {
                // 包已被删除或无权限读取时跳过。
            }
        }
        if (list.isEmpty()) {
            return packageManager.getInstalledApplications(0);
        }
        return list;
    }

    private void refreshStatus() {
        List<AppInfo> items = adapter.getItems();
        for (AppInfo info : items) {
            info.dpi = configManager.getDpi(info.packageName);
            info.width = configManager.getWidth(info.packageName);
            info.height = configManager.getHeight(info.packageName);
            info.configured = configManager.isConfigured(info.packageName);
            info.method = configManager.getMethod(info.packageName);
        }
        adapter.notifyDataSetChanged();
    }

    private void showSnack(int message) {
        Snackbar.make(findViewById(R.id.coordinator), message, Snackbar.LENGTH_LONG).show();
    }

    private void exitApp() {
        finish();
        System.exit(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (menu instanceof android.support.v7.view.menu.MenuBuilder) {
            ((android.support.v7.view.menu.MenuBuilder) menu).setOptionalIconsVisible(true);
        }
        MenuItem showSystem = menu.findItem(R.id.action_show_system);
        if (showSystem != null) {
            showSystem.setChecked(configManager.isShowSystem());
        }
        // 顶部栏与菜单图标统一为白色（与应用详情页"打开"按钮保持一致）。
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
        if (id == R.id.action_search) {
            startActivity(new Intent(this, SearchActivity.class));
            return true;
        }
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        if (id == R.id.action_about) {
            DialogUtils.showAbout(this);
            return true;
        }
        if (id == R.id.action_show_system) {
            boolean checked = !item.isChecked();
            item.setChecked(checked);
            configManager.setShowSystem(checked);
            lastShowSystem = checked;
            loadApps(true);
            return true;
        }
        if (id == R.id.action_exit) {
            exitApp();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        drawerLayout.closeDrawer(GravityCompat.START);
        if (id == R.id.nav_wizard) {
            Intent wizard = new Intent(this, WizardActivity.class);
            wizard.putExtra(WizardActivity.EXTRA_FORCE, true);
            startActivity(wizard);
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.nav_about) {
            DialogUtils.showAbout(this);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
