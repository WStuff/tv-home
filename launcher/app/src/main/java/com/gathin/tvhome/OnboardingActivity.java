package com.gathin.tvhome;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** First-run, remote-friendly setup. It deliberately reads the apps on this TV instead of shipping a personal list. */
public final class OnboardingActivity extends Activity {
    private static final String PREFS = "launcher_onboarding";
    private static final String COMPLETE = "complete";
    private static final int MAX_DOCK = 4;

    private final List<App> apps = new ArrayList<>();
    private final Set<String> dock = new LinkedHashSet<>();
    private final Set<String> home = new LinkedHashSet<>();
    private final List<FrameLayout> wallpaperRows = new ArrayList<>();
    private final List<TextView> wallpaperLabels = new ArrayList<>();
    private int page;
    private int builtInWallpaper;
    private String language;
    private float density;
    private LinearLayout content;
    private View primary;
    private TextView selectionCount;

    static boolean isComplete(Context context) {
        return context.getSharedPreferences(PREFS, MODE_PRIVATE).getBoolean(COMPLETE, false);
    }

    @Override protected void attachBaseContext(Context base) {
        super.attachBaseContext(LauncherLocale.wrap(base));
    }

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        density = getResources().getDisplayMetrics().density;
        collectApps();
        if (state != null) {
            page = state.getInt("page", 0);
            builtInWallpaper = state.getInt("wallpaper", 0);
            language = state.getString("language", LauncherLocale.language(this));
        }
        if (language == null) language = LauncherLocale.language(this);
        render();
    }

    @Override protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putInt("page", page);
        out.putInt("wallpaper", builtInWallpaper);
        out.putString("language", language);
    }

    @Override public void onBackPressed() {
        if (page == 0) { super.onBackPressed(); return; }
        previous();
    }

    @Override public boolean dispatchKeyEvent(KeyEvent event) {
        // The side arrows are available from every selection screen. This avoids trapping a user
        // at the bottom of a long list just to reach Continue or Back.
        if (page > 0 && page < 6 && event.getAction() == KeyEvent.ACTION_UP) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
                previous();
                return true;
            }
            if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
                next();
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private void collectApps() {
        PackageManager pm = getPackageManager();
        Map<String, ResolveInfo> unique = new LinkedHashMap<>();
        // Prefer the TV activity when an app provides both a regular and leanback entry point.
        for (String category : new String[]{Intent.CATEGORY_LEANBACK_LAUNCHER, Intent.CATEGORY_LAUNCHER}) {
            Intent intent = new Intent(Intent.ACTION_MAIN).addCategory(category);
            for (ResolveInfo info : pm.queryIntentActivities(intent, 0)) {
                if (info.activityInfo == null || !info.activityInfo.exported) continue;
                String pkg = info.activityInfo.packageName;
                if (!pkg.equals(getPackageName())) unique.putIfAbsent(pkg, info);
            }
        }
        for (ResolveInfo info : unique.values()) {
            String label;
            try { label = Cards.cleanLabel(String.valueOf(info.loadLabel(pm)), info.activityInfo.packageName); }
            catch (RuntimeException unavailable) { label = info.activityInfo.packageName; }
            apps.add(new App(info.activityInfo.packageName, label));
        }
        Collator collator = Collator.getInstance();
        Collections.sort(apps, (a, b) -> {
            int byLabel = collator.compare(a.label, b.label);
            return byLabel != 0 ? byLabel : a.pkg.compareTo(b.pkg);
        });
    }

    private void render() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(84), dp(46), dp(84), dp(40));
        root.setBackgroundColor(0xFF2B1B1F);

        TextView brand = text(R.string.onboarding_brand, 16, 0xFFE9A2B5);
        brand.setGravity(Gravity.CENTER);
        brand.setLetterSpacing(0.18f);
        root.addView(brand, new LinearLayout.LayoutParams(-1, dp(28)));
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        root.addView(content, new LinearLayout.LayoutParams(-1, 0, 1));

        if (page == 0) languagePicker();
        else if (page < 3) intro();
        else if (page < 5) picker(page == 3);
        else if (page == 5) wallpaperPicker();
        else finishScreen();

        if (page < 6) {
            LinearLayout controls = new LinearLayout(this);
            controls.setGravity(Gravity.CENTER_VERTICAL);
            View back = navButton(R.drawable.ic_onboarding_back, R.string.onboarding_back);
            back.setVisibility(page == 0 ? View.INVISIBLE : View.VISIBLE);
            back.setOnClickListener(v -> previous());
            controls.addView(back, new LinearLayout.LayoutParams(dp(58), dp(58)));
            View spacer = new View(this);
            controls.addView(spacer, new LinearLayout.LayoutParams(0, 1, 1));
            primary = navButton(R.drawable.ic_onboarding_next, R.string.onboarding_next);
            primary.setOnClickListener(v -> next());
            controls.addView(primary, new LinearLayout.LayoutParams(dp(58), dp(58)));
            root.addView(controls, new LinearLayout.LayoutParams(-1, dp(58)));
        }
        setContentView(root);
        (page > 0 && page < 3 ? primary : content).post(() -> {
            View first = firstFocusable(content);
            (first != null ? first : primary).requestFocus();
        });
    }

    private void intro() {
        content.setGravity(Gravity.CENTER);
        int title = page == 1 ? R.string.onboarding_welcome_title : R.string.onboarding_features_title;
        int body = page == 1 ? R.string.onboarding_welcome_body : R.string.onboarding_features_body;
        TextView heading = text(title, 36, Color.WHITE);
        heading.setGravity(Gravity.CENTER);
        heading.setMaxLines(2);
        content.addView(heading, new LinearLayout.LayoutParams(-1, -2));
        TextView description = text(body, 20, 0xCCFFFFFF);
        description.setGravity(Gravity.CENTER);
        description.setMaxLines(4);
        description.setLineSpacing(dp(5), 1f);
        LinearLayout.LayoutParams descriptionParams = new LinearLayout.LayoutParams(-1, -2);
        descriptionParams.topMargin = dp(22);
        content.addView(description, descriptionParams);
    }

    private void picker(boolean choosingDock) {
        int title = choosingDock ? R.string.onboarding_dock_title : R.string.onboarding_home_title;
        int body = choosingDock ? R.string.onboarding_dock_body : R.string.onboarding_home_body;
        TextView heading = text(title, 30, Color.WHITE);
        content.addView(heading, new LinearLayout.LayoutParams(-1, -2));
        TextView description = text(body, 17, 0xCCFFFFFF);
        description.setMaxLines(2);
        LinearLayout.LayoutParams descriptionParams = new LinearLayout.LayoutParams(-1, -2);
        descriptionParams.topMargin = dp(8);
        content.addView(description, descriptionParams);
        selectionCount = text(getString(R.string.onboarding_selected, visibleSelectionCount(choosingDock)), 15, 0xFFE9A2B5);
        LinearLayout.LayoutParams countParams = new LinearLayout.LayoutParams(-1, -2);
        countParams.topMargin = dp(14);
        content.addView(selectionCount, countParams);
        if (apps.isEmpty()) {
            TextView empty = text(R.string.onboarding_no_apps, 18, 0xCCFFFFFF);
            empty.setGravity(Gravity.CENTER_VERTICAL);
            content.addView(empty, new LinearLayout.LayoutParams(-1, 0, 1));
            return;
        }
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        LinearLayout rows = new LinearLayout(this);
        rows.setOrientation(LinearLayout.VERTICAL);
        rows.setPadding(0, dp(8), 0, dp(8));
        for (App app : apps) {
            if (!choosingDock && dock.contains(app.pkg)) continue;
            rows.addView(appRow(app, choosingDock));
        }
        scroll.addView(rows, new ScrollView.LayoutParams(-1, -2));
        content.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
    }

    private View appRow(App app, boolean choosingDock) {
        boolean selected = (choosingDock ? dock : home).contains(app.pkg);
        TextView row = text((selected ? "✓  " : "     ") + app.label, 18, Color.WHITE);
        row.setSingleLine(true);
        row.setEllipsize(TextUtils.TruncateAt.END);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(18), 0, dp(18), 0);
        row.setFocusable(true);
        row.setClickable(true);
        row.setSelected(selected);
        row.setBackgroundResource(R.drawable.settings_choice_bg);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(52));
        params.bottomMargin = dp(6);
        row.setLayoutParams(params);
        row.setOnClickListener(v -> {
            Set<String> choice = choosingDock ? dock : home;
            if (!choice.contains(app.pkg) && choosingDock && choice.size() == MAX_DOCK) {
                Toast.makeText(this, R.string.onboarding_dock_limit, Toast.LENGTH_SHORT).show();
                return;
            }
            boolean becomingSelected = !choice.contains(app.pkg);
            if (becomingSelected) choice.add(app.pkg); else choice.remove(app.pkg);
            if (choosingDock) {
                if (becomingSelected) home.add(app.pkg); else home.remove(app.pkg);
            }
            setAppRow(row, app, becomingSelected);
            if (selectionCount != null) selectionCount.setText(getString(R.string.onboarding_selected,
                    visibleSelectionCount(choosingDock)));
        });
        return row;
    }

    private void setAppRow(TextView row, App app, boolean selected) {
        row.setText((selected ? "✓  " : "     ") + app.label);
        row.setSelected(selected);
    }

    private int visibleSelectionCount(boolean choosingDock) {
        if (choosingDock) return dock.size();
        int count = 0;
        for (String pkg : home) if (!dock.contains(pkg)) count++;
        return count;
    }

    private void languagePicker() {
        content.setGravity(Gravity.CENTER);
        TextView heading = text(R.string.onboarding_language_title, 36, Color.WHITE);
        heading.setGravity(Gravity.CENTER);
        content.addView(heading, new LinearLayout.LayoutParams(-1, -2));
        TextView description = text(R.string.onboarding_language_body, 20, 0xCCFFFFFF);
        description.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams descriptionParams = new LinearLayout.LayoutParams(-1, -2);
        descriptionParams.topMargin = dp(18);
        content.addView(description, descriptionParams);
        LinearLayout choices = new LinearLayout(this);
        choices.setGravity(Gravity.CENTER);
        TextView english = languageRow(R.string.language_english, "en");
        TextView russian = languageRow(R.string.language_russian, "ru");
        choices.addView(english, new LinearLayout.LayoutParams(dp(230), dp(60)));
        LinearLayout.LayoutParams russianParams = new LinearLayout.LayoutParams(dp(230), dp(60));
        russianParams.setMarginStart(dp(18));
        choices.addView(russian, russianParams);
        LinearLayout.LayoutParams choicesParams = new LinearLayout.LayoutParams(-1, -2);
        choicesParams.topMargin = dp(34);
        content.addView(choices, choicesParams);
    }

    private TextView languageRow(int label, String code) {
        TextView option = text(label, 20, Color.WHITE);
        if (code.equals(language)) option.setText("✓  " + getString(label));
        option.setGravity(Gravity.CENTER);
        option.setFocusable(true);
        option.setClickable(true);
        option.setSelected(code.equals(language));
        option.setBackgroundResource(R.drawable.settings_choice_bg);
        option.setOnClickListener(v -> {
            if (code.equals(language)) return;
            language = code;
            LauncherLocale.save(this, code);
            recreate();
        });
        return option;
    }

    private void wallpaperPicker() {
        TextView heading = text(R.string.onboarding_wallpaper_title, 30, Color.WHITE);
        content.addView(heading, new LinearLayout.LayoutParams(-1, -2));
        TextView description = text(R.string.onboarding_wallpaper_body, 17, 0xCCFFFFFF);
        description.setMaxLines(2);
        LinearLayout.LayoutParams descriptionParams = new LinearLayout.LayoutParams(-1, -2);
        descriptionParams.topMargin = dp(8);
        content.addView(description, descriptionParams);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout rows = new LinearLayout(this);
        rows.setOrientation(LinearLayout.VERTICAL);
        rows.setPadding(0, dp(18), 0, dp(12));
        wallpaperRows.clear();
        wallpaperLabels.clear();
        for (int i = 0; i < BuiltInWallpapers.count(); i++) rows.addView(wallpaperRow(i));
        scroll.addView(rows, new ScrollView.LayoutParams(-1, -2));
        content.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        updateWallpaperSelection();
    }

    private FrameLayout wallpaperRow(int index) {
        FrameLayout row = new FrameLayout(this);
        row.setFocusable(true);
        row.setClickable(true);
        row.setBackgroundResource(R.drawable.settings_choice_bg);
        row.setPadding(dp(4), dp(4), dp(4), dp(4));
        ImageView preview = new ImageView(this);
        preview.setImageResource(BuiltInWallpapers.imageAt(index));
        preview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        preview.setBackgroundResource(R.drawable.wallpaper_preview_clip);
        preview.setClipToOutline(true);
        preview.setContentDescription(getString(BuiltInWallpapers.labelAt(index)));
        row.addView(preview, new FrameLayout.LayoutParams(-1, -1));
        TextView label = text("", 17, Color.WHITE);
        label.setGravity(Gravity.CENTER_VERTICAL);
        label.setPadding(dp(16), 0, dp(16), 0);
        label.setBackgroundColor(0xA6100F14);
        FrameLayout.LayoutParams labelParams = new FrameLayout.LayoutParams(-1, dp(42), Gravity.BOTTOM);
        row.addView(label, labelParams);
        row.setOnClickListener(v -> {
            builtInWallpaper = index;
            updateWallpaperSelection();
        });
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(-1, dp(148));
        rowParams.bottomMargin = dp(12);
        row.setLayoutParams(rowParams);
        wallpaperRows.add(row);
        wallpaperLabels.add(label);
        return row;
    }

    private void updateWallpaperSelection() {
        for (int i = 0; i < wallpaperRows.size(); i++) {
            boolean selected = i == builtInWallpaper;
            wallpaperRows.get(i).setSelected(selected);
            wallpaperLabels.get(i).setText((selected ? "✓  " : "")
                    + getString(BuiltInWallpapers.labelAt(i)));
        }
    }

    private void next() {
        if (page < 6) {
            if (page == 3) home.addAll(dock);
            page++;
            render();
            return;
        }
        finishSetup();
    }

    private void previous() {
        if (page == 0) return;
        page--;
        render();
    }

    private void finishScreen() {
        content.setGravity(Gravity.CENTER);
        TextView heading = text(R.string.onboarding_done_title, 36, Color.WHITE);
        heading.setGravity(Gravity.CENTER);
        heading.setMaxLines(2);
        content.addView(heading, new LinearLayout.LayoutParams(-1, -2));
        TextView description = text(R.string.onboarding_done_body, 20, 0xCCFFFFFF);
        description.setGravity(Gravity.CENTER);
        description.setMaxLines(3);
        LinearLayout.LayoutParams descriptionParams = new LinearLayout.LayoutParams(-1, -2);
        descriptionParams.topMargin = dp(20);
        content.addView(description, descriptionParams);
        TextView start = actionButton(R.string.onboarding_start);
        start.setOnClickListener(v -> finishSetup());
        LinearLayout.LayoutParams startParams = new LinearLayout.LayoutParams(dp(320), dp(58));
        startParams.gravity = Gravity.CENTER_HORIZONTAL;
        startParams.topMargin = dp(34);
        content.addView(start, startParams);
    }

    private void finishSetup() {
        Set<String> packages = new LinkedHashSet<>();
        for (App app : apps) packages.add(app.pkg);
        home.addAll(dock);
        Config.saveUserDockApps(this, new ArrayList<>(dock));
        getSharedPreferences(Config.SETTINGS_NAME, MODE_PRIVATE).edit()
                .putString(Config.WALLPAPER_CHOICE_KEY, BuiltInWallpapers.choiceAt(builtInWallpaper)).apply();
        if (!LibraryApps.replaceHome(this, packages, home)) {
            Toast.makeText(this, R.string.library_save_failed, Toast.LENGTH_LONG).show();
            return;
        }
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putBoolean(COMPLETE, true).apply();
        setResult(RESULT_OK);
        finish();
    }

    /** A centered vector avoids the font-baseline drift that made the old chevrons look low. */
    private View navButton(int icon, int description) {
        ImageView button = new ImageView(this);
        button.setImageResource(icon);
        button.setScaleType(ImageView.ScaleType.CENTER);
        button.setContentDescription(getString(description));
        button.setFocusable(true);
        button.setClickable(true);
        button.setBackgroundResource(R.drawable.onboarding_nav_bg);
        return button;
    }

    private TextView actionButton(int label) {
        TextView button = text(label, 18, Color.WHITE);
        button.setGravity(Gravity.CENTER);
        button.setFocusable(true);
        button.setClickable(true);
        button.setBackgroundResource(R.drawable.onboarding_action_bg);
        return button;
    }

    private TextView text(int value, int size, int color) { return text(getString(value), size, color); }

    private TextView text(String value, int size, int color) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setTypeface(getResources().getFont(R.font.outfit_regular));
        view.setIncludeFontPadding(false);
        return view;
    }

    private View firstFocusable(ViewGroup group) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child.isFocusable()) return child;
            if (child instanceof ViewGroup) {
                View found = firstFocusable((ViewGroup) child);
                if (found != null) return found;
            }
        }
        return null;
    }

    private int dp(int value) { return Math.round(value * density); }

    private static final class App {
        final String pkg, label;
        App(String pkg, String label) { this.pkg = pkg; this.label = label; }
    }
}
