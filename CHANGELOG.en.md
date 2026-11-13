# Changelog
### [2.0.5] - 2026-11-12
**Added**
- Add GitHub repository link on About page, click to jump.
- Source code organized and pushed to open-source repository.
**Changed**
- Version upgraded to 2.0.5.
- Fix error of author information.

### [2.0.3] - 2026-11-11
**Added**
- Add multi-language support: Simplified Chinese, English, Japanese.
- Add language option in Settings, support follow system / Simplified Chinese / English / Japanese, take effect immediately after switching.
- Add `values-en` and `values-ja` resource folders, all UI strings fully mapped.
- Add GitHub open-source files: `LICENSE` (MIT), multi-language README, this changelog, `.gitignore`.
- Reorganize source project structure for GitHub upload.

**Changed**
- Crash log labels use English for cross-language readability.
- Unactivated framework text on About page uses localized string instead of hard-coded Chinese.

### [2.0.2] - 2026-11-11
**Fixed**
- Fix the issue that wizard shows on second launch: go directly to main page after agreeing agreement and finishing guide.
- Fix root mode ignoring "Show system apps" toggle.
- Fix fake refresh: clear list and fully rescan apps on refresh, fade-in after loading finished.
- Fix menu exit icon: replace with native Android exit icon.

**Changed**
- Version set to 2.0.2, no random suffix.
- Reorder settings items: "App fetch mode" above "Show system apps".
- Unify button color: open button, top menu icon and drawer icon to white.

### [2.0.1] - 2026-11-09
**Changed**
- Fix error of author and affiliation information.
- Add "Open Source License: MIT License" on About page, update date.
- Use default Android title size for all dialogs, remove custom large bold title.
- Rewrite user agreement: longer content, wider coverage, state MIT license, privacy and feedback.
- Adjust guide flow: show wizard first then pop agreement dialog; reject closes wizard and dialog, agree only closes dialog.
- Optimize wizard text: remove duplicate welcome text, polish permission/security/usage page.
- Change "Enter" button to default gray secondary button, remove custom blue fill.
- Remove cancel button from injection dialog, keep confirm button only.
- App itself will appear in app list under root mode.
- Return to main page directly when clicking enter from drawer wizard.
- Remove "Fullscreen dialog" switch, add "App fetch mode" in settings, adjust padding.
- Only list content fade on refresh, top bar stays stable.
- Adjust search button touch area and ripple effect.

**Fixed**
- Fix lag after clicking agree: config write and su chmod moved to background thread.

### [2.0.0] - 2026-10-22
**Added**
- Version upgrade to 2.0.0, app name: Screen Density Changer.
- Add 5th injection method: Try all five methods.
- Drawer menu: Wizard / Settings / About, contact info moved into About, add black-theme style header.
- Global crash handler: show crash page, copy or export log.
- Rotate animation for search icon toggle.
- Remove +/- button on DPI dialog, only keep number input.

**Changed**
- Unify dialog style: remove icon, only title / content / button area, scrollable long text.
- Injection / kill / DPI selector: select item first, confirm to apply.
- Horizontal progress bar for app loading.
- Fixed workflow: select injection mode → detect environment → start service → load app list.
- Remove elevation of top bar on main and guide page, seamless connect with tab.
- Reload app list on enter, show loading toast, fade in content after ready.
- Remove divider lines in settings and detail page, remove top shadow.
- Top-right icon on detail page changed to white.

### [1.0.4] - 2026-10-16
**Changed**: Version upgraded to 1.0.4.

**Fixed**
- **v1.0.4-fix**: Fix crash on first open. Remove ContextThemeWrapper from agreement dialog.
- **v1.0.4-fix2**: Fix crash when entering main page. Remove ContextThemeWrapper from DialogUtils, fix top bar shadow.

### [1.0.1] - 2026-10-16
**Changed**
- Hardcode UI density to 200 DPI.
- Fix dialog zoom issue, dialog also render at 200 DPI.
- Version changed to 1.0.1.

### [1.0.0] - 2026-10-06
**Added**
- First release: Xposed module and app management UI.
- Set DPI and resolution separately for each app, kill app to apply changes.
- 4 injection modes: config injection, metric injection, resource injection, view injection.
- User agreement and wizard on first launch.
- Gradle build pipeline for release(signed) and debug APK.
