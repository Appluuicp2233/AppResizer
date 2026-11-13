# 屏幕密度调节器 · AppResizer

[中文](README.md) · [English](README.en.md) · [日本語](README.ja.md)

> 说明：本项目部分内容由 AI 完成，介绍与更改日志可能存在问题，请谨慎鉴别；应用开发也由 AI 辅助完成。

> 一款运行在 Xposed / EdXposed / LSPosed 框架上的系统级显示调节工具，可为设备上的**单个应用**独立设置显示密度（DPI）与分辨率，不修改系统全局参数。

| 项目 | 信息 |
| --- | --- |
| 包名 | `com.appresizer` |
| 当前版本 | 2.0.5 |
| 作者 | Appluuicp2233 |
| 所属 | AllToolBox |
| 开源协议 | MIT License |
| minSdk / targetSdk | 24（Android 7.0）/ 25 |

---

## 一、简介（快速了解）

屏幕密度调节器是一个 Xposed 模块。它把配置注入到你选中的目标应用进程中，单独覆盖该应用的 DPI 与分辨率；系统本身和其他应用不受影响。

- 按应用独立配置 DPI 与分辨率，互不干扰。
- 提供 5 种注入方式，可按应用单独切换。
- 支持系统接口 / root 接口两种应用列表获取模式。
- 内置全局防崩溃机制，异常时可复制或导出日志反馈。
- 界面支持简体中文、English、日本語，可在设置中切换。

## 二、功能详情

### 2.1 单应用显示调节

| 功能 | 说明 |
| --- | --- |
| DPI 调节 | 为任意应用单独设置显示密度（范围 72 – 960） |
| 分辨率覆盖 | 单独设置目标应用的宽 × 高（范围 320 – 4320） |
| 注入方式 | 配置注入 / 指标注入 / 资源管理 / 界面注入 / 五种同时尝试 |
| 进程终止 | 强制终止 / 普通终止 / 不终止 |
| 系统应用 | 开关控制是否在列表中显示内置系统应用 |
| 搜索 | 按应用名称或包名实时过滤 |

### 2.2 注入方式

| 值 | 名称 | 说明 |
| --- | --- | --- |
| 0 | 配置注入 | 改写目标应用的 `Configuration` 与显示指标，兼容性最好（默认） |
| 1 | 指标注入 | 仅改写显示指标，适合大部分界面类应用 |
| 2 | 资源管理 | 在系统资源管理层统一下发，适合多进程应用 |
| 3 | 界面注入 | 逐个界面改写资源，适合注入较晚的场景 |
| 4 | 五种同时尝试 | 依次尝试以上方式，提高生效概率 |

### 2.3 应用列表获取模式

| 模式 | 说明 |
| --- | --- |
| 系统接口 | 通过系统 `PackageManager` 获取应用列表（默认，无需 root） |
| root 接口 | 通过 `su -c pm list packages -u` 获取全部应用，可覆盖隐藏 / 保护应用；root 不可用时自动回退系统接口 |

## 三、环境要求

1. Android 7.0（API 24）及以上。
2. 已安装 Xposed / EdXposed / LSPosed 框架。
3. 在框架中勾选启用本模块，并重启设备后生效。
4. root 权限（用于配置文件全局可读、终止目标应用进程、root 接口枚举应用）。

## 四、安装与使用

1. 安装 APK，在框架管理器的模块列表中勾选本应用，重启设备。
2. 首次打开：阅读并同意用户协议 → 浏览向导（欢迎 / 权限 / 安全 / 使用）→ 点击「进入」。
3. 首次进入主界面依次选择：注入方式（默认配置注入）→ 应用列表获取模式。
4. 在列表点击目标应用，进入详情页设置 DPI 与分辨率。
5. 点击右上角「保存」，目标应用进程被终止；下次打开该应用时新配置生效。
6. 第二次启动起，APP 直接进入主界面，不再重复显示向导。

> 若某种注入方式对某个应用不生效，可在该应用详情页切换为其他方式。

## 五、构建

### 5.1 环境

| 项目 | 版本 / 要求 |
| --- | --- |
| Gradle | 7.0.2（已附 `gradlew`） |
| Android Gradle Plugin | 7.0.4 |
| JDK | 11 |
| compileSdkVersion | 25 |
| Build-Tools | 30.0.3 |

### 5.2 命令

```bash
# 编译未签名 release
./gradlew assembleRelease

# 编译 debug
./gradlew assembleDebug
```

Xposed API 以 `compileOnly` 方式引入：

```gradle
compileOnly 'de.robv.android.xposed:api:82'
```

若网络拉取不到，可将 `api-82.jar` 放入 `app/libs/`，并改为：

```gradle
compileOnly files('libs/api-82.jar')
```

### 5.3 签名

所有历史版本使用同一密钥签名，证书 SHA-256：

```
9d629fe2503e3678916373d9f87ec36d55cf935164f963c78d9cec6265011d4d
```

可直接覆盖安装升级，不会因签名不一致导致安装失败。

## 六、项目结构

```
AppResizer/
├── build.gradle / settings.gradle / gradle.properties
├── gradlew / gradlew.bat / gradle/wrapper/
├── LICENSE
├── README.md / README.en.md / README.ja.md
├── CHANGELOG.md
├── icon/                         上架用图标
└── app/
    ├── build.gradle / proguard-rules.pro
    └── src/main/
        ├── AndroidManifest.xml
        ├── assets/xposed_init
        ├── java/com/appresizer/
        │   ├── AppResizerApp.java        Application（叠加 200 DPI 与语言）
        │   ├── BaseActivity.java          基础 Activity
        │   ├── LocaleUtils.java           语言 / 密度上下文工具
        │   ├── MainActivity.java          主界面
        │   ├── WizardActivity.java        向导 / 首启协议
        │   ├── AppSettingsActivity.java   应用详情
        │   ├── SettingsActivity.java      设置（含语言切换）
        │   ├── SearchActivity.java        搜索
        │   ├── AppListAdapter.java / AppInfo.java
        │   ├── DialogUtils.java           统一弹窗
        │   ├── ConfigManager.java         配置存储
        │   ├── EnvChecker.java / ShellUtils.java
        │   ├── CrashHandler.java / CrashActivity.java  全局防崩溃
        │   └── XposedHook.java            注入入口
        └── res/
            ├── values/                    简体中文（默认）
            ├── values-en/                 English
            ├── values-ja/                 日本語
            ├── layout / menu / drawable / mipmap-*
```

## 七、配置如何被 Xposed 读取（Android 7+ 关键点）

Android 7.0 起 `MODE_WORLD_READABLE` 被禁用，Xposed 的 `XSharedPreferences` 无法读取应用的私有 SharedPreferences。因此每次保存配置后，本应用通过 root 执行：

```sh
chmod 755 <dataDir>
chmod 755 <dataDir>/shared_prefs
chmod 644 <dataDir>/shared_prefs/appresizer_config.xml
```

若配置未生效，可在 Xposed 日志中过滤 `AppResizer`：

- `AppResizer apply <包名> dpi=...`：配置读取成功，注入已执行。
- `AppResizer prefs unreadable`：权限未改成功，需检查 root 授权。

## 八、隐私与合规

- 本应用不申请任何联网权限，不收集、不上传、不分享个人信息。
- 所有配置仅保存在本机 SharedPreferences，卸载后一并清除。
- root 权限仅在主动执行保存配置、终止进程、root 模式枚举应用时调用。
- 本工具用于调整本机已安装应用的显示参数，请勿用于规避平台风控、篡改他人设备或任何违法用途。

## 九、许可

本项目基于 **MIT License** 开源，详见 [LICENSE](LICENSE)。版本改动记录见 [CHANGELOG.md](CHANGELOG.md)。
