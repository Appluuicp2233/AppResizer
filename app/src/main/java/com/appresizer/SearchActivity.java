package com.appresizer;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SearchActivity extends BaseActivity {

    private ConfigManager configManager;
    private EditText searchInput;
    private ImageButton searchAction;
    private View emptyView;
    private AppListAdapter adapter;
    private List<AppInfo> allApps = new ArrayList<AppInfo>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configManager = ConfigManager.get(this);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        searchInput = (EditText) findViewById(R.id.search_input);
        searchAction = (ImageButton) findViewById(R.id.search_action);
        emptyView = findViewById(R.id.search_empty);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.search_results);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppListAdapter(new AppListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(AppInfo info) {
                Intent intent = new Intent(SearchActivity.this, AppSettingsActivity.class);
                intent.putExtra(AppSettingsActivity.EXTRA_PACKAGE, info.packageName);
                startActivity(intent);
            }
        });
        adapter.setShowStatus(configManager.isShowStatus());
        recyclerView.setAdapter(adapter);

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean empty = s.length() == 0;
                // 搜索 ↔ 关闭图标加旋转过渡动画。
                try {
                    searchAction.animate().rotation(empty ? 0f : 90f).setDuration(200).start();
                } catch (Throwable ignored) {
                }
                searchAction.setImageResource(empty ? R.drawable.ic_search : R.drawable.ic_clear);
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        searchAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (searchInput.getText().length() > 0) {
                    searchInput.setText("");
                    searchInput.requestFocus();
                }
            }
        });

        loadApps();
        searchInput.requestFocus();
        searchInput.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(searchInput, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        }, 200);
    }

    private void loadApps() {
        new AsyncTask<Void, Void, List<AppInfo>>() {
            @Override
            protected List<AppInfo> doInBackground(Void... params) {
                PackageManager packageManager = getPackageManager();
                List<ApplicationInfo> installed = packageManager.getInstalledApplications(0);
                List<AppInfo> result = new ArrayList<AppInfo>();
                boolean showSystem = configManager.isShowSystem();
                for (ApplicationInfo info : installed) {
                    boolean system = (info.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
                    if (system && !showSystem) {
                        continue;
                    }
                    if (ConfigManager.SELF_PACKAGE.equals(info.packageName)) {
                        continue;
                    }
                    AppInfo item = new AppInfo(String.valueOf(info.loadLabel(packageManager)), info.packageName, info.loadIcon(packageManager), system);
                    item.configured = configManager.isConfigured(info.packageName);
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

            @Override
            protected void onPostExecute(List<AppInfo> result) {
                if (isFinishing()) {
                    return;
                }
                allApps = result;
                filter(searchInput.getText().toString());
            }
        }.execute();
    }

    private void filter(String keyword) {
        String query = keyword == null ? "" : keyword.trim().toLowerCase();
        List<AppInfo> result = new ArrayList<AppInfo>();
        for (AppInfo info : allApps) {
            if (query.length() == 0
                    || info.label.toLowerCase().contains(query)
                    || info.packageName.toLowerCase().contains(query)) {
                result.add(info);
            }
        }
        adapter.setItems(result);
        emptyView.setVisibility(result.isEmpty() && query.length() > 0 ? View.VISIBLE : View.GONE);
    }
}
