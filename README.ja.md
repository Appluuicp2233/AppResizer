# 画面密度調整ツール · AppResizer

[中文](README.md) · [English](README.en.md) · [日本語](README.ja.md)

> 注意：本プロジェクトの一部は AI によって作成されました。紹介文と更新履歴には誤りがある可能性がありますので、慎重にご確認ください。アプリも AI の支援を受けて開発されました。

> Xposed / EdXposed / LSPosed フレームワーク上で動作するシステムレベルの表示調整ツールです。システム全体のパラメータを変更せず、**アプリごとに個別の表示密度（DPI）と解像度**を設定できます。

| 項目 | 内容 |
| --- | --- |
| パッケージ名 | `com.appresizer` |
| 最新バージョン | 2.0.5 |
| 作者 | Appluuicp2233 |
| 所属 | AllToolBox |
| ライセンス | MIT License |
| minSdk / targetSdk | 24（Android 7.0）/ 25 |

---

## 1. 概要

画面密度調整ツールは Xposed モジュールです。選択した対象アプリのプロセスに設定を注入し、そのアプリの DPI と解像度だけを上書きします。システム本体や他のアプリには影響しません。

- アプリごとに DPI と解像度を独立して設定でき、互いに干渉しません。
- 5 つの注入方式をアプリごとに切り替えられます。
- アプリリスト取得モードはシステムインターフェースと root インターフェースの 2 種類です。
- グローバルなクラッシュ保護を内蔵。異常時にログをコピーまたはエクスポートしてフィードバックできます。
- 画面表示言語は简体中文 / English / 日本語に対応し、設定で切り替えられます。

## 2. 機能

### 2.1 アプリ別の表示調整

| 機能 | 説明 |
| --- | --- |
| DPI 調整 | 任意のアプリに個別の表示密度を設定（範囲 72 – 960） |
| 解像度上書き | 対象アプリに個別の幅 × 高さを設定（範囲 320 – 4320） |
| 注入方式 | 設定 / メトリクス / リソース管理 / UI / 5 つ同時試行 |
| プロセス終了 | 強制停止 / 通常停止 / 終了しない |
| システムアプリ | 一覧にシステム標準アプリを表示するか切り替え |
| 検索 | アプリ名またはパッケージ名でリアルタイム絞り込み |

### 2.2 注入方式

| 値 | 名称 | 説明 |
| --- | --- | --- |
| 0 | 設定インジェクション | 対象アプリの `Configuration` と表示メトリクスを上書き。互換性が最も高い（既定） |
| 1 | メトリクス注入 | 表示メトリクスのみ上書き。多くの UI 系アプリに適します |
| 2 | リソース管理 | システムリソース管理層から反映。マルチプロセスアプリに適します |
| 3 | UI 注入 | 画面ごとにリソースを上書き。遅い段階での注入に適します |
| 4 | 5 つ同時試行 | 上記を順に試行し、反映確率を高めます |

### 2.3 アプリリスト取得モード

| モード | 説明 |
| --- | --- |
| システムインターフェース | システムの `PackageManager` から取得（既定、root 不要） |
| root インターフェース | `su -c pm list packages -u` で全アプリを取得。非表示 / 保護アプリも含みます。root 不可時はシステムインターフェースに自動フォールバック |

## 3. 要件

1. Android 7.0（API 24）以上。
2. Xposed / EdXposed / LSPosed フレームワークのインストール。
3. フレームワークで本モジュールを有効にし、端末を再起動して有効化します。
4. root 権限（設定ファイルを全体読み取り可能にする、対象アプリのプロセスを終了する、root モードでアプリを列挙するため）。

## 4. インストールと使い方

1. APK をインストールし、フレームワーク管理アプリのモジュール一覧で本アプリを有効にして端末を再起動します。
2. 初回起動: 利用規約を読んで同意 → ガイド（ようこそ / 権限 / セキュリティ / 使い方）を確認 →「入る」をタップ。
3. メイン画面初回では順に: 注入方式（既定は設定インジェクション）→ アプリリスト取得モードを選択します。
4. 一覧から対象アプリをタップして詳細ページを開き、DPI と解像度を設定します。
5. 右上の「保存」をタップすると対象アプリのプロセスが終了し、次回起動時に新しい設定が反映されます。
6. 2 回目以降はメイン画面に直接進み、ガイドは再表示されません。

> あるアプリで注入方式が効かない場合は、そのアプリの詳細ページで他の方式に切り替えてください。

## 5. ビルド

### 5.1 環境

| 項目 | バージョン / 要件 |
| --- | --- |
| Gradle | 7.0.2（`gradlew` 同梱） |
| Android Gradle Plugin | 7.0.4 |
| JDK | 11 |
| compileSdkVersion | 25 |
| Build-Tools | 30.0.3 |

### 5.2 コマンド

```bash
# 署名なし release をビルド
./gradlew assembleRelease

# debug をビルド
./gradlew assembleDebug
```

Xposed API は `compileOnly` で導入します。

```gradle
compileOnly 'de.robv.android.xposed:api:82'
```

ネットワークから取得できない場合は、`api-82.jar` を `app/libs/` に置き、次のように変更します。

```gradle
compileOnly files('libs/api-82.jar')
```

### 5.3 署名

過去バージョンはすべて同じ署名鍵を使用しています。証明書 SHA-256:

```
9d629fe2503e3678916373d9f87ec36d55cf935164f963c78d9cec6265011d4d
```

以前のバージョンの上書きインストールが可能で、署名不一致によるインストール失敗は発生しません。

## 6. プロジェクト構成

```
AppResizer/
├── build.gradle / settings.gradle / gradle.properties
├── gradlew / gradlew.bat / gradle/wrapper/
├── LICENSE
├── README.md / README.en.md / README.ja.md
├── CHANGELOG.md
├── icon/                         ストアアイコン
└── app/
    ├── build.gradle / proguard-rules.pro
    └── src/main/
        ├── AndroidManifest.xml
        ├── assets/xposed_init
        ├── java/com/appresizer/
        │   ├── AppResizerApp.java        Application（200 DPI + 言語）
        │   ├── BaseActivity.java          基底 Activity
        │   ├── LocaleUtils.java           言語 / 密度コンテキスト処理
        │   ├── MainActivity.java          メイン画面
        │   ├── WizardActivity.java        ガイド / 初回規約
        │   ├── AppSettingsActivity.java   アプリ詳細
        │   ├── SettingsActivity.java      設定（言語切り替え含む）
        │   ├── SearchActivity.java        検索
        │   ├── AppListAdapter.java / AppInfo.java
        │   ├── DialogUtils.java           共通ダイアログ
        │   ├── ConfigManager.java         設定保存
        │   ├── EnvChecker.java / ShellUtils.java
        │   ├── CrashHandler.java / CrashActivity.java  グローバルクラッシュ保護
        │   └── XposedHook.java             注入エントリ
        └── res/
            ├── values/                    简体中文（既定）
            ├── values-en/                 English
            ├── values-ja/                 日本語
            ├── layout / menu / drawable / mipmap-*
```

## 7. 設定が Xposed から読まれる仕組み（Android 7+ の注意点）

Android 7.0 以降は `MODE_WORLD_READABLE` が無効になり、Xposed の `XSharedPreferences` からアプリのプライベート SharedPreferences を読めません。そのため保存のたびに本アプリは root 経由で次を実行します。

```sh
chmod 755 <dataDir>
chmod 755 <dataDir>/shared_prefs
chmod 644 <dataDir>/shared_prefs/appresizer_config.xml
```

設定が反映されない場合は Xposed ログで `AppResizer` を絞り込んでください。

- `AppResizer apply <パッケージ> dpi=...`: 設定の読み取り成功、注入実行済み。
- `AppResizer prefs unreadable`: 権限変更に失敗。root 許可を確認してください。

## 8. プライバシーとコンプライアンス

- 本アプリはネットワーク権限を一切申請せず、個人情報を収集・アップロード・共有しません。
- すべての設定は端末内の SharedPreferences にのみ保存され、アンインストール後に消去されます。
- root 権限は設定保存、プロセス終了、root モードでのアプリ列挙を行うときのみ使用します。
- 本ツールは端末にインストールされたアプリの表示パラメータを調整するものです。プラットフォームのリスク制御回避、他人の端末の改ざん、違法用途には使用しないでください。

## 9. ライセンス

本プロジェクトは **MIT License** で公開されています。詳細は [LICENSE](LICENSE) を
