package dev.glorioustr.hyperosgesturesactivator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Insets;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public final class StatusDetailActivity extends Activity {
    private static final String EXTRA_HEALTH = "health";
    private boolean health;
    private boolean dark;
    private boolean amoled;
    private boolean aero;

    static Intent intent(Context context, boolean health) {
        return new Intent(context, StatusDetailActivity.class).putExtra(EXTRA_HEALTH, health);
    }

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        health = getIntent().getBooleanExtra(EXTRA_HEALTH, true);
        SharedPreferences preferences = getSharedPreferences("ui_preferences", MODE_PRIVATE);
        String color = preferences.getString("color_mode", "SYSTEM");
        boolean systemDark = (getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        amoled = "AMOLED".equals(color);
        dark = amoled || "DARK".equals(color) || ("SYSTEM".equals(color) && systemDark);
        aero = "AERO_GLASS".equals(preferences.getString("content_style", "DEFAULT"));
        applyChrome();
        setContentView(buildScreen());
    }

    private View buildScreen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackground(background());
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(18), dp(20), dp(28));
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            Insets bars = insets.getInsets(WindowInsets.Type.systemBars());
            view.setPadding(dp(20) + bars.left, dp(18) + bars.top,
                    dp(20) + bars.right, dp(28) + bars.bottom);
            return insets;
        });
        scroll.addView(root, matchWrap());

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        TextView back = text("‹", 34, primary(), Typeface.NORMAL);
        back.setGravity(Gravity.CENTER);
        back.setOnClickListener(view -> finish());
        top.addView(back, new LinearLayout.LayoutParams(dp(48), dp(48)));
        TextView title = text(getString(health ? R.string.section_system_health
                : R.string.section_gestures), 23, primary(), Typeface.BOLD);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        titleParams.setMarginStart(dp(8));
        top.addView(title, titleParams);
        root.addView(top, matchWrap());

        TextView intro = text(getString(health ? R.string.health_page_desc
                : R.string.gestures_page_desc), 14, secondary(), Typeface.NORMAL);
        intro.setPadding(dp(4), dp(10), dp(4), dp(20));
        root.addView(intro, matchWrap());
        if (health) addHealthContent(root); else addGestureContent(root);
        return scroll;
    }

    private void addHealthContent(LinearLayout root) {
        boolean systemUi = GestureActivation.isSystemUiReady(this);
        boolean launcher = GestureActivation.isLauncherReady(this);
        int fsg = GestureActivation.readGlobalInt(this,
                GestureActivation.KEY_FORCE_FSG_NAV_BAR, 0);
        int navigation;
        try { navigation = Settings.Secure.getInt(getContentResolver(),
                GestureActivation.KEY_NAVIGATION_MODE, -1); }
        catch (Exception ignored) { navigation = -1; }
        addCard(root, "⚙", R.string.health_systemui,
                systemUi ? R.string.health_ready : R.string.health_waiting, systemUi,
                Color.rgb(0, 198, 211), Color.rgb(35, 111, 222));
        addCard(root, "⌂", R.string.health_launcher,
                launcher ? R.string.health_ready : R.string.health_waiting, launcher,
                Color.rgb(111, 78, 232), Color.rgb(205, 74, 208));
        boolean navigationReady = fsg == 1 && navigation == 2;
        addCard(root, "≋", R.string.health_navigation,
                navigationReady ? R.string.health_gesture_mode : R.string.health_off,
                navigationReady, Color.rgb(30, 146, 238), Color.rgb(27, 202, 187));
        addCard(root, "◆", R.string.health_default_home,
                R.string.health_home_detected, true,
                Color.rgb(242, 139, 51), Color.rgb(232, 71, 139));
    }

    private void addGestureContent(LinearLayout root) {
        addCard(root, "‹", R.string.gesture_back_title, R.string.gesture_back_desc, true,
                Color.rgb(0, 190, 218), Color.rgb(46, 106, 224));
        addCard(root, "↑", R.string.gesture_home_title, R.string.gesture_home_desc, true,
                Color.rgb(91, 89, 238), Color.rgb(178, 72, 226));
        addCard(root, "▤", R.string.gesture_recents_title, R.string.gesture_recents_desc, true,
                Color.rgb(230, 71, 154), Color.rgb(247, 112, 83));
        addCard(root, "⇄", R.string.gesture_quick_switch_title,
                R.string.gesture_quick_switch_desc, true,
                Color.rgb(246, 154, 42), Color.rgb(228, 73, 117));
    }

    private void addCard(LinearLayout root, String symbol, int titleId, int detailId,
            boolean ready, int accentStart, int accentEnd) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackground(surface(15));
        TextView icon = text(symbol, 28, Color.WHITE, Typeface.BOLD);
        icon.setGravity(Gravity.CENTER);
        int start = ready ? accentStart : Color.rgb(181, 122, 36);
        int end = ready ? accentEnd : Color.rgb(145, 83, 42);
        icon.setShadowLayer(dp(7), 0, dp(2), Color.argb(150, Color.red(end),
                Color.green(end), Color.blue(end)));
        icon.setBackground(gradient(new int[]{start, end}, 14,
                Color.argb(180, 225, 239, 255), 1));
        icon.setElevation(dp(5));
        card.addView(icon, new LinearLayout.LayoutParams(dp(62), dp(62)));
        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams copyParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        copyParams.setMarginStart(dp(15));
        copy.addView(text(getString(titleId), 17, primary(), Typeface.BOLD), matchWrap());
        TextView detail = text(getString(detailId), 13, secondary(), Typeface.NORMAL);
        detail.setPadding(0, dp(4), 0, 0);
        copy.addView(detail, matchWrap());
        card.addView(copy, copyParams);
        LinearLayout.LayoutParams params = matchWrap();
        params.bottomMargin = dp(12);
        root.addView(card, params);
    }

    private GradientDrawable surface(int radius) {
        if (amoled) return rounded(Color.rgb(7, 7, 9), radius, Color.rgb(58, 64, 77), 1);
        if (aero) return dark
                ? gradient(new int[]{Color.rgb(24, 43, 63), Color.rgb(48, 45, 66),
                        Color.rgb(34, 44, 61)}, radius, Color.argb(125, 215, 231, 255), 1)
                : gradient(new int[]{Color.rgb(208, 220, 232), Color.rgb(216, 216, 229),
                        Color.rgb(210, 218, 231)}, radius, Color.argb(150, 84, 115, 151), 1);
        return rounded(dark ? Color.rgb(56, 54, 62) : Color.WHITE, radius,
                dark ? Color.rgb(91, 96, 111) : Color.rgb(220, 226, 237), 1);
    }

    private GradientDrawable background() {
        if (amoled) return rounded(Color.BLACK, 0, Color.TRANSPARENT, 0);
        if (aero) return dark
                ? gradient(new int[]{Color.rgb(6, 17, 33), Color.rgb(19, 37, 63),
                        Color.rgb(48, 27, 62)}, 0, Color.TRANSPARENT, 0)
                : gradient(new int[]{Color.rgb(231, 246, 255), Color.rgb(245, 236, 255),
                        Color.rgb(221, 235, 255)}, 0, Color.TRANSPARENT, 0);
        return rounded(dark ? Color.rgb(15, 14, 20) : Color.rgb(246, 248, 252),
                0, Color.TRANSPARENT, 0);
    }

    private void applyChrome() {
        int color = amoled ? Color.BLACK : dark ? Color.rgb(15, 14, 20)
                : Color.rgb(246, 248, 252);
        getWindow().setStatusBarColor(color);
        getWindow().setNavigationBarColor(color);
        int flags = getWindow().getDecorView().getSystemUiVisibility();
        if (dark) flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        else flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        getWindow().getDecorView().setSystemUiVisibility(flags);
    }

    private TextView text(String value, int size, int color, int style) {
        TextView view = new TextView(this);
        view.setText(value); view.setTextSize(size); view.setTextColor(color);
        view.setTypeface(Typeface.DEFAULT, style); return view;
    }
    private int primary() { return dark ? Color.rgb(245, 241, 250) : Color.rgb(26, 35, 52); }
    private int secondary() { return dark ? Color.rgb(203, 195, 211) : Color.rgb(105, 113, 128); }
    private GradientDrawable rounded(int color, int radius, int stroke, int width) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius));
        if (width > 0) d.setStroke(dp(width), stroke); return d;
    }
    private GradientDrawable gradient(int[] colors, int radius, int stroke, int width) {
        GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.TL_BR, colors);
        d.setCornerRadius(dp(radius)); if (width > 0) d.setStroke(dp(width), stroke); return d;
    }
    private LinearLayout.LayoutParams matchWrap() { return new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
