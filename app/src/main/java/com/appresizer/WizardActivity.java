package com.appresizer;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class WizardActivity extends BaseActivity {

    public static final String EXTRA_FORCE = "force_show";

    private ViewPager pager;
    private TabLayout tabs;
    private View root;
    private ConfigManager configManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configManager = ConfigManager.get(this);
        setContentView(R.layout.activity_wizard);
        root = findViewById(R.id.wizard_root);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title_wizard);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        pager = (ViewPager) findViewById(R.id.wizard_pager);
        tabs = (TabLayout) findViewById(R.id.wizard_tabs);

        boolean force = getIntent().getBooleanExtra(EXTRA_FORCE, false);
        // 已同意协议且已完成引导：再次启动直接进入主界面，不再重复显示向导。
        if (!force && configManager.isAgreed() && !configManager.isFirstRun()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // 先显示向导界面，再在其上弹协议弹窗。
        setupWizard();
        if (!configManager.isAgreed()) {
            showAgreementDialog();
        }
    }

    private void setupWizard() {
        root.setVisibility(View.VISIBLE);
        pager.setAdapter(new WizardAdapter());
        tabs.setupWithViewPager(pager);
    }

    private void showAgreementDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_body, null);
        ((TextView) view.findViewById(R.id.dialog_body)).setText(R.string.agreement_body);
        new AlertDialog.Builder(this)
                .setCustomTitle(DialogUtils.title(this, R.string.agreement_title))
                .setView(view)
                .setCancelable(false)
                .setNegativeButton(R.string.reject, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 拒绝 → 协议弹窗与向导界面一起退出。
                        finish();
                    }
                })
                .setPositiveButton(R.string.agree, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        configManager.setAgreed(true);
                        // 弹窗消失，留在向导界面，不跳转。
                    }
                })
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onEnterClicked(View view) {
        configManager.setFirstRun(false);
        // 如果是从主界面抽屉再次打开向导，点"进入"应直接返回主界面，而不是新开一个 MainActivity。
        Intent intent = getIntent();
        boolean fromMain = intent != null && intent.getBooleanExtra(EXTRA_FORCE, false);
        if (fromMain) {
            finish();
            return;
        }
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private class WizardAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.wizard_tab_welcome);
                case 1:
                    return getString(R.string.wizard_tab_permission);
                case 2:
                    return getString(R.string.wizard_tab_security);
                default:
                    return getString(R.string.wizard_tab_usage);
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = LayoutInflater.from(WizardActivity.this).inflate(R.layout.page_wizard, container, false);
            TextView title = (TextView) view.findViewById(R.id.wizard_page_title);
            TextView body = (TextView) view.findViewById(R.id.wizard_page_body);
            TextView note = (TextView) view.findViewById(R.id.wizard_page_note);
            Button enter = (Button) view.findViewById(R.id.wizard_enter);
            switch (position) {
                case 0:
                    title.setText(R.string.wizard_welcome_title);
                    body.setText(R.string.wizard_welcome_body);
                    note.setText(R.string.wizard_welcome_note);
                    break;
                case 1:
                    title.setText(R.string.wizard_permission_title);
                    body.setText(R.string.wizard_permission_body);
                    note.setText(R.string.wizard_permission_note);
                    break;
                case 2:
                    title.setText(R.string.wizard_security_title);
                    body.setText(R.string.wizard_security_body);
                    note.setText(R.string.wizard_security_note);
                    break;
                default:
                    title.setText(R.string.wizard_usage_title);
                    body.setText(R.string.wizard_usage_body);
                    note.setText(R.string.wizard_usage_note);
                    break;
            }
            if (position == 3) {
                enter.setVisibility(View.VISIBLE);
                enter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onEnterClicked(v);
                    }
                });
            } else {
                enter.setVisibility(View.GONE);
            }
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}
