package dev.glorioustr.hyperosgesturesactivator;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Insets;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public final class AboutActivity extends Activity {
    private static final String PROJECT_URL =
            "https://github.com/GloriousTR/HyperOS-Gestures-Activator";
    private static final String TELEGRAM_URL = "https://t.me/glorioustr";
    private boolean darkMode;
    private boolean amoledMode;
    private boolean aeroGlass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = getSharedPreferences("ui_preferences", MODE_PRIVATE);
        String colorMode = preferences.getString("color_mode", "SYSTEM");
        boolean systemDark = (getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        amoledMode = "AMOLED".equals(colorMode);
        darkMode = amoledMode || "DARK".equals(colorMode)
                || ("SYSTEM".equals(colorMode) && systemDark);
        aeroGlass = "AERO_GLASS".equals(preferences.getString("content_style", "DEFAULT"));
        applySystemChrome();
        setTitle(R.string.about_title);
        setContentView(buildScreen());
        DiagnosticDatabase.get(this).insert(new DiagnosticEvent(
                0L,
                System.currentTimeMillis(),
                DiagnosticEvent.STATUS_SUCCESS,
                "ui",
                "about-opened",
                "version=" + BuildConfig.VERSION_NAME,
                getPackageName(),
                Thread.currentThread().getName()));
    }

    private View buildScreen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackground(screenBackground());

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(16), dp(20), dp(32));
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            Insets bars = insets.getInsets(WindowInsets.Type.systemBars());
            view.setPadding(dp(20) + bars.left, dp(16) + bars.top,
                    dp(20) + bars.right, dp(32) + bars.bottom);
            return insets;
        });
        scroll.addView(root, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        root.addView(buildTopBar(), matchWrap());
        root.addView(space(18));
        root.addView(buildHero(), matchWrap());
        root.addView(space(16));
        root.addView(buildFeatureCard(), matchWrap());
        root.addView(space(16));
        root.addView(buildRepositoryCard(), matchWrap());
        root.addView(space(16));
        root.addView(buildContactCard(), matchWrap());
        return scroll;
    }

    private View buildTopBar() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        TextView back = text("‹", 34, primaryTextColor(), Typeface.NORMAL);
        back.setGravity(Gravity.CENTER);
        back.setContentDescription(getString(R.string.back_cd));
        back.setClickable(true);
        back.setFocusable(true);
        back.setOnClickListener(view -> finish());
        row.addView(back, new LinearLayout.LayoutParams(dp(44), dp(48)));
        TextView title = text(getString(R.string.about_title), 24,
                primaryTextColor(), Typeface.BOLD);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        params.setMarginStart(dp(8));
        row.addView(title, params);
        return row;
    }

    private View buildHero() {
        LinearLayout card = card();
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        card.setPadding(dp(20), dp(22), dp(20), dp(24));
        card.setBackground(rounded(Color.rgb(2, 3, 17), 18,
                Color.rgb(30, 68, 150), 1));

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.ic_launcher_art);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        logo.setAdjustViewBounds(true);
        logo.setContentDescription(getString(R.string.about_logo_cd));
        card.addView(logo, new LinearLayout.LayoutParams(dp(220), dp(220)));

        TextView eyebrow = text(getString(R.string.about_eyebrow), 11,
                Color.rgb(123, 196, 255), Typeface.BOLD);
        eyebrow.setLetterSpacing(0.12f);
        eyebrow.setGravity(Gravity.CENTER);
        eyebrow.setPadding(0, dp(14), 0, 0);
        card.addView(eyebrow, matchWrap());

        TextView appName = text(getString(R.string.app_name), 23,
                Color.WHITE, Typeface.BOLD);
        appName.setGravity(Gravity.CENTER);
        appName.setPadding(0, dp(7), 0, 0);
        card.addView(appName, matchWrap());

        TextView version = text(getString(
                R.string.about_version, BuildConfig.VERSION_NAME), 13,
                Color.rgb(190, 202, 230), Typeface.NORMAL);
        version.setGravity(Gravity.CENTER);
        version.setPadding(0, dp(5), 0, 0);
        card.addView(version, matchWrap());

        TextView description = text(getString(R.string.about_description), 14,
                Color.rgb(221, 228, 245), Typeface.NORMAL);
        description.setGravity(Gravity.CENTER);
        description.setLineSpacing(0f, 1.15f);
        description.setPadding(0, dp(16), 0, 0);
        card.addView(description, matchWrap());
        return card;
    }

    private View buildFeatureCard() {
        LinearLayout card = card();
        addFeature(card, "⌁", R.string.about_capabilities_title,
                R.string.about_capabilities_desc, false);
        addFeature(card, "✓", R.string.about_safe_title,
                R.string.about_safe_desc, true);
        addFeature(card, "◉", R.string.about_diagnostics_title,
                R.string.about_diagnostics_desc, true);
        return card;
    }

    private void addFeature(
            LinearLayout parent,
            String symbol,
            int titleId,
            int descriptionId,
            boolean divider) {
        if (divider) {
            parent.addView(divider(), new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(1)));
        }
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.TOP);
        row.setPadding(0, dp(14), 0, dp(14));
        TextView icon = text(symbol, 20,
                darkMode ? Color.rgb(167, 181, 255) : Color.rgb(67, 79, 146), Typeface.BOLD);
        icon.setGravity(Gravity.CENTER);
        icon.setBackground(rounded(
                darkMode ? Color.rgb(47, 50, 72) : Color.rgb(239, 241, 255),
                11, Color.TRANSPARENT, 0));
        row.addView(icon, new LinearLayout.LayoutParams(dp(44), dp(44)));

        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams copyParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        copyParams.setMarginStart(dp(14));
        row.addView(copy, copyParams);
        copy.addView(text(getString(titleId), 15,
                primaryTextColor(), Typeface.BOLD), matchWrap());
        TextView description = text(getString(descriptionId), 12,
                secondaryTextColor(), Typeface.NORMAL);
        description.setPadding(0, dp(4), 0, 0);
        copy.addView(description, matchWrap());
        parent.addView(row, matchWrap());
    }

    private View buildRepositoryCard() {
        LinearLayout card = card();
        TextView title = text(getString(R.string.about_repository_title), 17,
                primaryTextColor(), Typeface.BOLD);
        card.addView(title, matchWrap());
        TextView description = text(getString(R.string.about_repository_desc), 13,
                secondaryTextColor(), Typeface.NORMAL);
        description.setPadding(0, dp(6), 0, 0);
        card.addView(description, matchWrap());
        TextView url = text(getString(R.string.about_repository_url), 11,
                darkMode ? Color.rgb(139, 177, 255) : Color.rgb(71, 83, 160), Typeface.NORMAL);
        url.setPadding(0, dp(12), 0, dp(12));
        url.setTextIsSelectable(true);
        card.addView(url, matchWrap());

        Button open = new Button(this);
        open.setAllCaps(false);
        open.setText(R.string.about_open_github);
        open.setTextColor(Color.WHITE);
        open.setTextSize(14);
        open.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        open.setBackground(rounded(
                Color.rgb(68, 82, 160), 14, Color.TRANSPARENT, 0));
        open.setOnClickListener(view -> openProject());
        card.addView(open, matchWrap());
        return card;
    }

    private View buildContactCard() {
        LinearLayout card = card();
        LinearLayout heading = new LinearLayout(this);
        heading.setOrientation(LinearLayout.HORIZONTAL);
        heading.setGravity(Gravity.CENTER_VERTICAL);
        TextView icon = text("➤", 24, Color.WHITE, Typeface.BOLD);
        icon.setGravity(Gravity.CENTER);
        icon.setBackground(rounded(Color.rgb(0, 151, 178), 15,
                Color.rgb(93, 213, 230), 1));
        heading.addView(icon, new LinearLayout.LayoutParams(dp(50), dp(50)));
        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams copyParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        copyParams.setMarginStart(dp(14));
        copy.addView(text(getString(R.string.contact_telegram_title), 17,
                primaryTextColor(), Typeface.BOLD), matchWrap());
        copy.addView(text(getString(R.string.contact_telegram_handle), 14,
                darkMode ? Color.rgb(73, 218, 235) : Color.rgb(0, 130, 164), Typeface.BOLD), matchWrap());
        TextView description = text(getString(R.string.contact_telegram_desc), 12,
                secondaryTextColor(), Typeface.NORMAL);
        description.setPadding(0, dp(3), 0, 0);
        copy.addView(description, matchWrap());
        heading.addView(copy, copyParams);
        card.addView(heading, matchWrap());
        Button open = new Button(this);
        open.setAllCaps(false);
        open.setText(R.string.open_telegram);
        open.setTextColor(Color.WHITE);
        open.setTextSize(14);
        open.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        open.setBackground(rounded(Color.rgb(0, 151, 178), 14,
                Color.TRANSPARENT, 0));
        open.setOnClickListener(view -> openTelegram());
        LinearLayout.LayoutParams openParams = matchWrap();
        openParams.topMargin = dp(14);
        card.addView(open, openParams);
        return card;
    }

    private void openProject() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(PROJECT_URL)));
        } catch (ActivityNotFoundException exception) {
            Toast.makeText(this, R.string.about_open_failed, Toast.LENGTH_LONG).show();
        }
    }

    private void openTelegram() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("tg://resolve?domain=glorioustr")));
        } catch (ActivityNotFoundException ignored) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(TELEGRAM_URL)));
            } catch (ActivityNotFoundException unavailable) {
                Toast.makeText(this, R.string.telegram_open_failed, Toast.LENGTH_LONG).show();
            }
        }
    }

    private LinearLayout card() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(8), dp(18), dp(8));
        card.setBackground(surfaceDrawable(15));
        card.setElevation(dp(1));
        return card;
    }

    private View divider() {
        View divider = new View(this);
        divider.setBackgroundColor(darkMode
                ? Color.rgb(68, 70, 82) : Color.rgb(235, 238, 244));
        return divider;
    }

    private View space(int height) {
        return new View(this) {{
            setLayoutParams(new LinearLayout.LayoutParams(1, dp(height)));
        }};
    }

    private TextView text(String value, int size, int color, int style) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setTypeface(Typeface.DEFAULT, style);
        return view;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void applySystemChrome() {
        int background = amoledMode ? Color.BLACK
                : darkMode ? Color.rgb(7, 17, 32) : Color.rgb(240, 246, 253);
        getWindow().setStatusBarColor(background);
        getWindow().setNavigationBarColor(background);
        int flags = getWindow().getDecorView().getSystemUiVisibility();
        if (darkMode) {
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        } else {
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        }
        getWindow().getDecorView().setSystemUiVisibility(flags);
    }

    private GradientDrawable screenBackground() {
        if (amoledMode) return rounded(Color.BLACK, 0, Color.TRANSPARENT, 0);
        if (aeroGlass) return darkMode
                ? gradient(new int[]{Color.rgb(7, 18, 34), Color.rgb(22, 40, 69),
                        Color.rgb(42, 29, 65)}, 0, Color.TRANSPARENT, 0)
                : gradient(new int[]{Color.rgb(231, 246, 255), Color.rgb(244, 237, 255),
                        Color.rgb(222, 235, 255)}, 0, Color.TRANSPARENT, 0);
        return rounded(darkMode ? Color.rgb(15, 14, 20) : Color.rgb(246, 248, 252),
                0, Color.TRANSPARENT, 0);
    }

    private GradientDrawable surfaceDrawable(int radius) {
        if (amoledMode) return rounded(Color.rgb(8, 9, 13), radius,
                Color.rgb(73, 81, 99), 1);
        if (aeroGlass) return darkMode
                ? gradient(new int[]{Color.rgb(29, 55, 78), Color.rgb(54, 48, 70),
                        Color.rgb(37, 48, 67)}, radius, Color.rgb(148, 169, 199), 1)
                : gradient(new int[]{Color.rgb(214, 229, 240), Color.rgb(229, 222, 239)},
                        radius, Color.rgb(142, 164, 194), 1);
        return rounded(darkMode ? Color.rgb(43, 43, 52) : Color.WHITE, radius,
                darkMode ? Color.rgb(93, 99, 115) : Color.rgb(220, 226, 237), 1);
    }

    private GradientDrawable gradient(int[] colors, int radius, int stroke, int width) {
        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR, colors);
        drawable.setCornerRadius(dp(radius));
        if (width > 0) drawable.setStroke(dp(width), stroke);
        return drawable;
    }

    private int primaryTextColor() {
        return darkMode ? Color.rgb(245, 241, 250) : Color.rgb(26, 35, 52);
    }

    private int secondaryTextColor() {
        return darkMode ? Color.rgb(199, 196, 211) : Color.rgb(100, 109, 126);
    }

    private GradientDrawable rounded(
            int color,
            int radius,
            int strokeColor,
            int strokeWidth) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));
        if (strokeWidth > 0) {
            drawable.setStroke(dp(strokeWidth), strokeColor);
        }
        return drawable;
    }
}
