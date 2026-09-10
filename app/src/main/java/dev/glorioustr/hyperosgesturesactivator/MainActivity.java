package dev.glorioustr.hyperosgesturesactivator;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Insets;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public final class MainActivity extends Activity {
    private static final long STATUS_REFRESH_INTERVAL_MS = 1000L;
    private static final String UI_PREFERENCES = "ui_preferences";
    private static final String KEY_CONTENT_STYLE = "content_style";
    private static final String STYLE_DEFAULT = "DEFAULT";
    private static final String STYLE_AERO_GLASS = "AERO_GLASS";

    private final Handler statusHandler = new Handler(Looper.getMainLooper());
    private final Runnable statusRefresh = new Runnable() {
        @Override
        public void run() {
            updateDashboard();
            statusHandler.postDelayed(this, STATUS_REFRESH_INTERVAL_MS);
        }
    };

    private DiagnosticDatabase database;
    private LinearLayout statusCard;
    private TextView statusBadge;
    private TextView statusTitle;
    private TextView statusDetail;
    private Button primaryButton;
    private TextView systemUiHealth;
    private TextView launcherHealth;
    private TextView navigationHealth;
    private TextView defaultHomeView;
    private GestureHeroView gestureHero;
    private boolean aeroGlass;
    private boolean darkMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = DiagnosticDatabase.get(this);
        darkMode = (getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        aeroGlass = STYLE_AERO_GLASS.equals(getSharedPreferences(
                UI_PREFERENCES, MODE_PRIVATE).getString(KEY_CONTENT_STYLE, STYLE_DEFAULT));
        applySystemChrome();
        setTitle(R.string.app_name);
        setContentView(buildScreen());
        recordAppEvent(
                DiagnosticEvent.STATUS_SUCCESS,
                "ui",
                "dashboard-opened",
                buildDeviceSummary());
    }

    @Override
    protected void onStart() {
        super.onStart();
        statusHandler.removeCallbacks(statusRefresh);
        statusHandler.post(statusRefresh);
    }

    @Override
    protected void onStop() {
        statusHandler.removeCallbacks(statusRefresh);
        super.onStop();
    }

    private View buildScreen() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackground(screenBackground());

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(18), dp(20), dp(28));
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            Insets bars = insets.getInsets(WindowInsets.Type.systemBars());
            view.setPadding(
                    dp(20) + bars.left,
                    dp(18) + bars.top,
                    dp(20) + bars.right,
                    dp(28) + bars.bottom);
            return insets;
        });
        scrollView.addView(root, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        root.addView(buildTopBar(), matchWrap());
        root.addView(space(18));
        root.addView(buildStatusCard(), matchWrap());
        root.addView(space(22));
        root.addView(sectionTitle(getString(R.string.section_system_health)), matchWrap());
        root.addView(space(10));
        root.addView(buildHealthCard(), matchWrap());
        root.addView(space(22));
        root.addView(sectionTitle(getString(R.string.section_gestures)), matchWrap());
        root.addView(space(10));
        root.addView(buildGestureCard(), matchWrap());
        root.addView(space(22));

        TextView footer = text(
                getString(R.string.footer_version_device,
                        BuildConfig.VERSION_NAME, Build.MANUFACTURER, Build.MODEL),
                12,
                secondaryTextColor(),
                Typeface.NORMAL);
        footer.setGravity(Gravity.CENTER);
        root.addView(footer, matchWrap());
        return scrollView;
    }

    private View buildTopBar() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(10), 0, dp(10));

        TextView menu = text("☰", 25, primaryTextColor(), Typeface.BOLD);
        menu.setGravity(Gravity.CENTER);
        menu.setContentDescription(getString(R.string.menu_cd));
        menu.setClickable(true);
        menu.setFocusable(true);
        menu.setBackgroundColor(Color.TRANSPARENT);
        menu.setOnClickListener(this::showMainMenu);
        row.addView(menu, new LinearLayout.LayoutParams(dp(48), dp(48)));

        LinearLayout brand = new LinearLayout(this);
        brand.setOrientation(LinearLayout.HORIZONTAL);
        brand.setGravity(Gravity.CENTER);
        brand.setPadding(dp(10), dp(6), dp(12), dp(6));
        brand.setBackground(gradient(new int[]{
                Color.rgb(10, 21, 38), Color.rgb(18, 20, 31)}, 17,
                aeroGlass ? Color.argb(145, 111, 172, 255) : Color.TRANSPARENT,
                aeroGlass ? 1 : 0));
        brand.setElevation(dp(2));

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.ic_launcher_foreground);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        logo.setBackground(rounded(Color.rgb(2, 4, 17), 9,
                aeroGlass ? Color.argb(150, 92, 158, 255) : Color.TRANSPARENT,
                aeroGlass ? 1 : 0));
        logo.setClipToOutline(true);
        brand.addView(logo, new LinearLayout.LayoutParams(dp(36), dp(36)));

        LinearLayout titles = new LinearLayout(this);
        titles.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams copyParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        copyParams.setMarginStart(dp(7));
        TextView title = text(getString(R.string.dashboard_title), 16,
                Color.WHITE, Typeface.BOLD);
        TextView subtitle = text(getString(R.string.dashboard_subtitle), 9,
                Color.rgb(91, 174, 255), Typeface.BOLD);
        subtitle.setLetterSpacing(0.08f);
        titles.addView(title, matchWrap());
        titles.addView(subtitle, matchWrap());
        brand.addView(titles, copyParams);

        LinearLayout.LayoutParams brandParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        brandParams.setMarginStart(dp(4));
        brandParams.setMarginEnd(dp(8));
        row.addView(brand, brandParams);

        TextView style = text("✦", 18,
                aeroGlass ? Color.rgb(101, 174, 255) : secondaryTextColor(), Typeface.BOLD);
        style.setGravity(Gravity.CENTER);
        style.setContentDescription(getString(R.string.menu_appearance));
        style.setClickable(true);
        style.setFocusable(true);
        style.setBackground(surfaceDrawable(14));
        style.setOnClickListener(view -> showAppearanceDialog());
        row.addView(style, new LinearLayout.LayoutParams(dp(44), dp(44)));
        return row;
    }

    private View buildStatusCard() {
        statusCard = new LinearLayout(this);
        statusCard.setOrientation(LinearLayout.VERTICAL);
        statusCard.setPadding(dp(20), dp(20), dp(20), dp(20));
        statusCard.setElevation(dp(aeroGlass ? 4 : 2));

        gestureHero = new GestureHeroView(this, aeroGlass, darkMode);
        statusCard.addView(gestureHero, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(138)));

        statusBadge = text(getString(R.string.status_label), 11,
                Color.rgb(35, 85, 62), Typeface.BOLD);
        statusBadge.setLetterSpacing(0.12f);
        statusBadge.setGravity(Gravity.CENTER);
        statusBadge.setPadding(0, dp(2), 0, 0);
        statusCard.addView(statusBadge, matchWrap());

        statusTitle = text(getString(R.string.status_checking_title), 26,
                Color.rgb(18, 46, 32), Typeface.BOLD);
        statusTitle.setPadding(0, dp(8), 0, 0);
        statusTitle.setGravity(Gravity.CENTER);
        statusCard.addView(statusTitle, matchWrap());

        statusDetail = text(getString(R.string.status_checking_desc), 14,
                Color.rgb(62, 82, 70), Typeface.NORMAL);
        statusDetail.setPadding(0, dp(6), 0, dp(18));
        statusDetail.setGravity(Gravity.CENTER);
        statusCard.addView(statusDetail, matchWrap());

        primaryButton = new Button(this);
        primaryButton.setAllCaps(false);
        primaryButton.setTextSize(15);
        primaryButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        primaryButton.setMinHeight(dp(52));
        primaryButton.setOnClickListener(view ->
                setGestureActivation(!GestureActivation.isEnabled(this)));
        statusCard.addView(primaryButton, matchWrap());

        TextView safety = text(
                getString(R.string.safety_hint),
                11,
                secondaryTextColor(),
                Typeface.NORMAL);
        safety.setGravity(Gravity.CENTER);
        safety.setPadding(0, dp(12), 0, 0);
        statusCard.addView(safety, matchWrap());
        return statusCard;
    }

    private View buildHealthCard() {
        LinearLayout group = new LinearLayout(this);
        group.setOrientation(LinearLayout.VERTICAL);
        LinearLayout top = tileRow();
        systemUiHealth = addHealthTile(top, "S", Color.rgb(27, 174, 194),
                getString(R.string.health_systemui), getString(R.string.status_checking_title));
        launcherHealth = addHealthTile(top, "L", Color.rgb(125, 87, 217),
                getString(R.string.health_launcher), getString(R.string.status_checking_title));
        group.addView(top, matchWrap());
        group.addView(space(10));
        LinearLayout bottom = tileRow();
        navigationHealth = addHealthTile(bottom, "⌁", Color.rgb(54, 111, 203),
                getString(R.string.health_navigation), getString(R.string.status_checking_title));
        defaultHomeView = addHealthTile(bottom, "⌂", Color.rgb(235, 132, 51),
                getString(R.string.health_default_home), getString(R.string.status_checking_title));
        group.addView(bottom, matchWrap());
        return group;
    }

    private View buildGestureCard() {
        LinearLayout group = new LinearLayout(this);
        group.setOrientation(LinearLayout.VERTICAL);
        LinearLayout top = tileRow();
        addGestureTile(top, "‹", Color.rgb(55, 179, 219),
                getString(R.string.gesture_back_title), getString(R.string.gesture_back_desc));
        addGestureTile(top, "↑", Color.rgb(116, 87, 224),
                getString(R.string.gesture_home_title), getString(R.string.gesture_home_desc));
        group.addView(top, matchWrap());
        group.addView(space(10));
        LinearLayout bottom = tileRow();
        addGestureTile(bottom, "▤", Color.rgb(236, 92, 148),
                getString(R.string.gesture_recents_title), getString(R.string.gesture_recents_desc));
        addGestureTile(bottom, "↔", Color.rgb(242, 145, 54),
                getString(R.string.gesture_quick_switch_title),
                getString(R.string.gesture_quick_switch_desc));
        group.addView(bottom, matchWrap());
        return group;
    }

    private LinearLayout tileRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setBaselineAligned(false);
        return row;
    }

    private TextView addHealthTile(
            LinearLayout parent,
            String symbol,
            int accent,
            String label,
            String value) {
        LinearLayout tile = themedCard(dp(14));
        tile.setGravity(Gravity.START);
        tile.setMinimumHeight(dp(126));

        TextView icon = iconBadge(symbol, accent, 42);
        tile.addView(icon, new LinearLayout.LayoutParams(dp(42), dp(42)));

        TextView labelView = text(label, 12, secondaryTextColor(), Typeface.BOLD);
        labelView.setPadding(0, dp(10), 0, 0);
        labelView.setMaxLines(2);
        tile.addView(labelView, matchWrap());

        TextView valueView = text(value, 13, secondaryTextColor(), Typeface.BOLD);
        valueView.setPadding(0, dp(4), 0, 0);
        valueView.setMaxLines(2);
        tile.addView(valueView, matchWrap());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
        if (parent.getChildCount() > 0) {
            params.setMarginStart(dp(10));
        }
        parent.addView(tile, params);
        return valueView;
    }

    private void addGestureTile(
            LinearLayout parent,
            String symbol,
            int accent,
            String title,
            String detail) {
        LinearLayout tile = themedCard(dp(14));
        tile.setMinimumHeight(dp(142));
        tile.addView(iconBadge(symbol, accent, 46),
                new LinearLayout.LayoutParams(dp(46), dp(46)));

        TextView titleView = text(title, 14, primaryTextColor(), Typeface.BOLD);
        titleView.setPadding(0, dp(10), 0, 0);
        titleView.setMaxLines(2);
        tile.addView(titleView, matchWrap());

        TextView detailView = text(detail, 11, secondaryTextColor(), Typeface.NORMAL);
        detailView.setPadding(0, dp(4), 0, 0);
        detailView.setMaxLines(3);
        tile.addView(detailView, matchWrap());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
        if (parent.getChildCount() > 0) {
            params.setMarginStart(dp(10));
        }
        parent.addView(tile, params);
    }

    private TextView iconBadge(String symbol, int accent, int size) {
        TextView icon = text(symbol, 22, Color.WHITE, Typeface.BOLD);
        icon.setGravity(Gravity.CENTER);
        icon.setBackground(gradient(new int[]{
                lighten(accent, 0.12f), accent, darken(accent, 0.15f)},
                15, aeroGlass ? Color.argb(150, 225, 240, 255) : Color.TRANSPARENT,
                aeroGlass ? 1 : 0));
        icon.setElevation(dp(2));
        return icon;
    }

    private void showMainMenu(View anchor) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        FrameLayout overlay = new FrameLayout(this);
        overlay.setPadding(dp(12), dp(48), dp(24), dp(36));
        overlay.setOnClickListener(view -> dialog.dismiss());

        LinearLayout panel = card();
        panel.setPadding(dp(14), dp(16), dp(14), dp(14));
        panel.setOnClickListener(view -> { });
        panel.addView(text(getString(R.string.menu_cd), 22,
                primaryTextColor(), Typeface.BOLD), matchWrap());
        TextView subtitle = text(getString(R.string.menu_overlay_subtitle), 13,
                secondaryTextColor(), Typeface.NORMAL);
        subtitle.setPadding(0, dp(3), 0, dp(10));
        panel.addView(subtitle, matchWrap());

        addOverlayMenuItem(panel, "◉", Color.rgb(71, 83, 160),
                getString(R.string.menu_live_diagnostics),
                getString(R.string.diagnostics_subtitle), () -> {
                    dialog.dismiss();
                    openDiagnostics();
                });
        addOverlayMenuItem(panel, "▣", Color.rgb(52, 111, 175),
                getString(R.string.menu_snapshot),
                getString(R.string.menu_snapshot_desc), () -> {
                    dialog.dismiss();
                    captureSnapshot("dashboard-menu");
                    Toast.makeText(this, R.string.toast_snapshot_saved,
                            Toast.LENGTH_SHORT).show();
                });
        addOverlayMenuItem(panel, "✦", Color.rgb(68, 137, 210),
                getString(R.string.menu_appearance),
                getString(aeroGlass
                        ? R.string.style_aero_glass_desc
                        : R.string.style_default_desc), () -> {
                    dialog.dismiss();
                    showAppearanceDialog();
                });
        addOverlayMenuItem(panel, "ⓘ", Color.rgb(104, 75, 165),
                getString(R.string.menu_about),
                getString(R.string.menu_about_desc), () -> {
                    dialog.dismiss();
                    openAbout();
                });

        Button close = new Button(this);
        close.setAllCaps(false);
        close.setText(R.string.menu_close);
        close.setTextSize(14);
        close.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        close.setTextColor(aeroGlass ? (darkMode
                ? Color.rgb(187, 217, 255) : Color.rgb(49, 94, 145))
                : (darkMode ? Color.rgb(215, 205, 232) : Color.rgb(67, 79, 146)));
        close.setBackground(surfaceDrawable(14));
        close.setOnClickListener(view -> dialog.dismiss());
        LinearLayout.LayoutParams closeParams = matchWrap();
        closeParams.topMargin = dp(8);
        panel.addView(close, closeParams);

        FrameLayout.LayoutParams panelParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.TOP | Gravity.START);
        overlay.addView(panel, panelParams);
        dialog.setContentView(overlay);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            WindowManager.LayoutParams attributes = window.getAttributes();
            attributes.dimAmount = 0.5f;
            window.setAttributes(attributes);
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }
    }

    private void addOverlayMenuItem(
            LinearLayout parent,
            String symbol,
            int accent,
            String title,
            String detail,
            Runnable action) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(14), dp(10), dp(12), dp(10));
        row.setMinimumHeight(dp(76));
        row.setClickable(true);
        row.setFocusable(true);
        row.setBackground(surfaceDrawable(17));
        row.setOnClickListener(view -> action.run());

        TextView icon = text(symbol, 21, accent, Typeface.BOLD);
        icon.setGravity(Gravity.CENTER);
        icon.setBackground(rounded(Color.argb(20,
                Color.red(accent), Color.green(accent), Color.blue(accent)),
                13, Color.TRANSPARENT, 0));
        row.addView(icon, new LinearLayout.LayoutParams(dp(44), dp(44)));

        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams copyParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        copyParams.setMarginStart(dp(14));
        copy.addView(text(title, 15, primaryTextColor(), Typeface.BOLD), matchWrap());
        TextView description = text(detail, 12,
                secondaryTextColor(), Typeface.NORMAL);
        description.setPadding(0, dp(2), 0, 0);
        copy.addView(description, matchWrap());
        row.addView(copy, copyParams);
        row.addView(text("›", 26, secondaryTextColor(), Typeface.NORMAL));

        LinearLayout.LayoutParams rowParams = matchWrap();
        rowParams.bottomMargin = dp(8);
        parent.addView(row, rowParams);
    }

    private void showAppearanceDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        FrameLayout overlay = new FrameLayout(this);
        overlay.setPadding(dp(18), dp(68), dp(18), dp(36));
        overlay.setOnClickListener(view -> dialog.dismiss());

        LinearLayout panel = card();
        panel.setPadding(dp(18), dp(20), dp(18), dp(18));
        panel.setOnClickListener(view -> { });
        panel.addView(text(getString(R.string.appearance_title), 23,
                primaryTextColor(), Typeface.BOLD), matchWrap());
        TextView subtitle = text(getString(R.string.appearance_subtitle), 13,
                secondaryTextColor(), Typeface.NORMAL);
        subtitle.setPadding(0, dp(4), 0, dp(16));
        panel.addView(subtitle, matchWrap());

        addThemeChoice(panel, dialog, false,
                getString(R.string.style_default_title),
                getString(R.string.style_default_desc));
        addThemeChoice(panel, dialog, true,
                getString(R.string.style_aero_glass_title),
                getString(R.string.style_aero_glass_desc));

        FrameLayout.LayoutParams panelParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.TOP);
        overlay.addView(panel, panelParams);
        dialog.setContentView(overlay);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            WindowManager.LayoutParams attributes = window.getAttributes();
            attributes.dimAmount = 0.55f;
            window.setAttributes(attributes);
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }
    }

    private void addThemeChoice(
            LinearLayout parent,
            Dialog dialog,
            boolean aero,
            String title,
            String detail) {
        boolean selected = aeroGlass == aero;
        LinearLayout choice = new LinearLayout(this);
        choice.setOrientation(LinearLayout.HORIZONTAL);
        choice.setGravity(Gravity.CENTER_VERTICAL);
        choice.setPadding(dp(12), dp(12), dp(14), dp(12));
        choice.setMinimumHeight(dp(104));
        choice.setClickable(true);
        choice.setFocusable(true);
        int selectedBorder = aero
                ? Color.rgb(92, 181, 255) : Color.rgb(93, 78, 180);
        choice.setBackground(aero
                ? gradient(new int[]{Color.rgb(8, 26, 43), Color.rgb(22, 59, 85),
                        Color.rgb(77, 39, 94)}, 18,
                        selected ? selectedBorder : Color.argb(110, 220, 232, 255),
                        selected ? 2 : 1)
                : rounded(Color.rgb(249, 249, 253), 18,
                        selected ? selectedBorder : Color.rgb(219, 223, 234),
                        selected ? 2 : 1));

        LinearLayout preview = new LinearLayout(this);
        preview.setOrientation(LinearLayout.VERTICAL);
        preview.setPadding(dp(9), dp(9), dp(9), dp(9));
        preview.setBackground(aero
                ? gradient(new int[]{Color.rgb(17, 43, 65), Color.rgb(63, 41, 78)},
                        14, Color.argb(140, 218, 237, 255), 1)
                : rounded(Color.WHITE, 14, Color.rgb(222, 226, 237), 1));
        View lineOne = new View(this);
        lineOne.setBackground(rounded(aero ? Color.rgb(79, 184, 255)
                : Color.rgb(112, 88, 205), 6, Color.TRANSPARENT, 0));
        preview.addView(lineOne, new LinearLayout.LayoutParams(dp(48), dp(8)));
        View lineTwo = new View(this);
        lineTwo.setBackground(rounded(aero ? Color.rgb(232, 114, 196)
                : Color.rgb(50, 183, 193), 6, Color.TRANSPARENT, 0));
        LinearLayout.LayoutParams lineTwoParams = new LinearLayout.LayoutParams(dp(32), dp(6));
        lineTwoParams.topMargin = dp(8);
        preview.addView(lineTwo, lineTwoParams);
        choice.addView(preview, new LinearLayout.LayoutParams(dp(76), dp(68)));

        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams copyParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        copyParams.setMarginStart(dp(14));
        int previewPrimary = aero ? Color.WHITE : Color.rgb(31, 39, 55);
        int previewSecondary = aero ? Color.rgb(204, 218, 238) : Color.rgb(103, 111, 128);
        copy.addView(text(title, 15, previewPrimary, Typeface.BOLD), matchWrap());
        TextView detailView = text(detail, 11, previewSecondary, Typeface.NORMAL);
        detailView.setPadding(0, dp(3), 0, 0);
        copy.addView(detailView, matchWrap());
        choice.addView(copy, copyParams);

        TextView check = text(selected ? "✓" : "○", 22,
                selected ? selectedBorder : previewSecondary, Typeface.BOLD);
        check.setGravity(Gravity.CENTER);
        choice.addView(check, new LinearLayout.LayoutParams(dp(34), dp(42)));
        choice.setOnClickListener(view -> {
            dialog.dismiss();
            selectContentStyle(aero);
        });

        LinearLayout.LayoutParams params = matchWrap();
        params.bottomMargin = dp(10);
        parent.addView(choice, params);
    }

    private void selectContentStyle(boolean aero) {
        if (aeroGlass == aero) {
            return;
        }
        aeroGlass = aero;
        getSharedPreferences(UI_PREFERENCES, MODE_PRIVATE).edit()
                .putString(KEY_CONTENT_STYLE, aero ? STYLE_AERO_GLASS : STYLE_DEFAULT)
                .apply();
        applySystemChrome();
        setContentView(buildScreen());
        updateDashboard();
        recordAppEvent(DiagnosticEvent.STATUS_SUCCESS, "ui", "content-style-changed",
                "style=" + (aero ? STYLE_AERO_GLASS : STYLE_DEFAULT));
        Toast.makeText(this, aero
                ? R.string.style_aero_glass_title
                : R.string.style_default_title, Toast.LENGTH_SHORT).show();
    }

    private void openDiagnostics() {
        startActivity(new Intent(this, DiagnosticsActivity.class));
    }

    private void openAbout() {
        startActivity(new Intent(this, AboutActivity.class));
    }

    private void updateDashboard() {
        boolean permissionGranted = checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS)
                == PackageManager.PERMISSION_GRANTED;
        boolean enabled = GestureActivation.isEnabled(this);
        boolean systemUiReady = GestureActivation.isSystemUiReady(this);
        boolean launcherReady = GestureActivation.isLauncherReady(this);
        int forceFsg = GestureActivation.readGlobalInt(
                this, GestureActivation.KEY_FORCE_FSG_NAV_BAR, 0);
        int navigationMode = readSecureInt(GestureActivation.KEY_NAVIGATION_MODE, -1);
        boolean fullyActive = enabled && forceFsg == 1 && navigationMode == 2;
        if (gestureHero != null) {
            gestureHero.setActive(fullyActive);
        }

        if (!permissionGranted) {
            applyStatus(
                    getString(R.string.status_permission_label),
                    getString(R.string.status_permission_title),
                    getString(R.string.status_permission_desc),
                    Color.rgb(255, 239, 237),
                    Color.rgb(145, 28, 28));
            primaryButton.setText(R.string.action_check_permission);
            primaryButton.setTextColor(Color.WHITE);
            primaryButton.setBackground(gradient(
                    new int[]{Color.rgb(174, 39, 39), Color.rgb(211, 66, 91)},
                    14, aeroGlass ? Color.argb(130, 255, 220, 228) : Color.TRANSPARENT,
                    aeroGlass ? 1 : 0));
        } else if (fullyActive) {
            applyStatus(
                    getString(R.string.status_active_label),
                    getString(R.string.status_active_title),
                    getString(R.string.status_active_desc),
                    Color.rgb(232, 247, 239),
                    Color.rgb(20, 105, 60));
            primaryButton.setText(R.string.action_safe_disable);
            primaryButton.setTextColor(darkMode ? Color.rgb(255, 220, 225)
                    : Color.rgb(136, 31, 31));
            primaryButton.setBackground(darkMode
                    ? gradient(new int[]{Color.rgb(83, 37, 54), Color.rgb(63, 42, 68)},
                            14, aeroGlass ? Color.argb(150, 255, 188, 203)
                                    : Color.rgb(125, 70, 84), 1)
                    : rounded(Color.rgb(255, 246, 245), 14,
                            Color.rgb(231, 188, 184), 1));
        } else if (enabled) {
            applyStatus(
                    getString(R.string.status_starting_label),
                    getString(R.string.status_starting_title),
                    getString(R.string.status_starting_desc),
                    Color.rgb(255, 246, 226),
                    Color.rgb(137, 81, 0));
            primaryButton.setText(R.string.action_safe_disable);
            primaryButton.setTextColor(darkMode ? Color.rgb(255, 224, 170)
                    : Color.rgb(120, 67, 0));
            primaryButton.setBackground(darkMode
                    ? gradient(new int[]{Color.rgb(75, 55, 34), Color.rgb(61, 47, 66)},
                            14, aeroGlass ? Color.argb(150, 255, 211, 128)
                                    : Color.rgb(127, 101, 61), 1)
                    : rounded(Color.rgb(255, 250, 240), 14,
                            Color.rgb(231, 205, 149), 1));
        } else {
            applyStatus(
                    getString(R.string.status_off_label),
                    getString(R.string.status_off_title),
                    getString(R.string.status_off_desc),
                    Color.WHITE,
                    Color.rgb(50, 61, 80));
            primaryButton.setText(R.string.action_enable_gestures);
            primaryButton.setTextColor(Color.WHITE);
            primaryButton.setBackground(gradient(new int[]{
                    Color.rgb(52, 114, 214), Color.rgb(111, 75, 209), Color.rgb(219, 70, 150)},
                    14, aeroGlass ? Color.argb(145, 225, 238, 255) : Color.TRANSPARENT,
                    aeroGlass ? 1 : 0));
        }

        bindHealth(systemUiHealth, systemUiReady,
                getString(R.string.health_ready), getString(R.string.health_waiting));
        bindHealth(launcherHealth, launcherReady,
                getString(R.string.health_ready), getString(R.string.health_waiting));
        boolean navigationReady = forceFsg == 1 && navigationMode == 2;
        bindHealth(navigationHealth, navigationReady,
                getString(R.string.health_gesture_mode), enabled
                        ? getString(R.string.health_transitioning)
                        : getString(R.string.health_off));
        defaultHomeView.setText(shortHome(resolveDefaultHome()));
        defaultHomeView.setTextColor(darkMode
                ? Color.rgb(215, 225, 241) : Color.rgb(62, 72, 90));

    }

    private void applyStatus(
            String badge,
            String title,
            String detail,
            int background,
            int accent) {
        if (aeroGlass) {
            int[] colors = darkMode
                    ? new int[]{Color.rgb(18, 43, 63), Color.rgb(48, 45, 66),
                            Color.rgb(34, 44, 61)}
                    : new int[]{Color.rgb(208, 220, 232), Color.rgb(216, 216, 229),
                            Color.rgb(210, 218, 231)};
            statusCard.setBackground(gradient(colors, 24,
                    Color.argb(darkMode ? 150 : 120, Color.red(accent),
                            Color.green(accent), Color.blue(accent)), 1));
        } else if (darkMode) {
            statusCard.setBackground(rounded(Color.rgb(56, 54, 62), 22,
                    Color.argb(100, Color.red(accent), Color.green(accent),
                            Color.blue(accent)), 1));
        } else {
            statusCard.setBackground(rounded(background, 22,
                    Color.argb(35, Color.red(accent), Color.green(accent),
                            Color.blue(accent)), 1));
        }
        statusBadge.setText(badge);
        statusBadge.setTextColor(darkMode ? lighten(accent, 0.38f) : accent);
        statusTitle.setText(title);
        statusTitle.setTextColor(darkMode ? Color.rgb(245, 247, 255) : accent);
        statusDetail.setText(detail);
        statusDetail.setTextColor(darkMode
                ? Color.rgb(205, 218, 236) : Color.rgb(71, 82, 75));
    }

    private void bindHealth(
            TextView view,
            boolean healthy,
            String healthyText,
            String waitingText) {
        view.setText((healthy ? "● " : "○ ") + (healthy ? healthyText : waitingText));
        view.setTextColor(healthy
                ? (darkMode ? Color.rgb(104, 232, 158) : Color.rgb(20, 112, 65))
                : (darkMode ? Color.rgb(255, 205, 112) : Color.rgb(145, 94, 15)));
    }

    private void setGestureActivation(boolean enabled) {
        if (checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS)
                != PackageManager.PERMISSION_GRANTED) {
            recordAppEvent(
                    DiagnosticEvent.STATUS_FAILURE,
                    "activation",
                    "change-gesture-activation",
                    "WRITE_SECURE_SETTINGS permission missing");
            new AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_permission_title)
                    .setMessage(R.string.dialog_permission_message)
                    .setPositiveButton(R.string.action_ok, null)
                    .show();
            return;
        }
        if (enabled
                && (!GestureActivation.isSystemUiReady(this)
                || !GestureActivation.isLauncherReady(this))) {
            recordAppEvent(
                    DiagnosticEvent.STATUS_FAILURE,
                    "activation",
                    "enable-gesture-navigation",
                    "Hooks not ready: systemUi=" + GestureActivation.isSystemUiReady(this)
                            + ", launcher=" + GestureActivation.isLauncherReady(this));
            new AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_module_title)
                    .setMessage(R.string.dialog_module_message)
                    .setPositiveButton(R.string.action_ok, null)
                    .show();
            return;
        }

        SharedPreferences preferences = createDeviceProtectedStorageContext()
                .getSharedPreferences("gesture_activation", MODE_PRIVATE);
        boolean wasEnabled = GestureActivation.isEnabled(this);
        if (enabled && !wasEnabled) {
            preferences.edit().putInt(
                    "previous_force_fsg_nav_bar",
                    GestureActivation.readGlobalInt(
                            this, GestureActivation.KEY_FORCE_FSG_NAV_BAR, 0)).apply();
        }
        int forceValue = enabled
                ? 1 : preferences.getInt("previous_force_fsg_nav_bar", 0);
        boolean activationWritten = GestureActivation.writeGlobalInt(
                this, GestureActivation.KEY_ENABLED, enabled ? 1 : 0);
        boolean forceWritten = GestureActivation.writeGlobalInt(
                this, GestureActivation.KEY_FORCE_FSG_NAV_BAR, forceValue);
        String operation = enabled
                ? "enable-gesture-navigation" : "disable-gesture-navigation";
        if (activationWritten && forceWritten) {
            recordAppEvent(
                    DiagnosticEvent.STATUS_SUCCESS,
                    "activation",
                    operation,
                    "enabled=" + enabled + " | force_fsg_nav_bar=" + forceValue);
        } else {
            recordAppEvent(
                    DiagnosticEvent.STATUS_FAILURE,
                    "activation",
                    operation,
                    "activationWritten=" + activationWritten
                            + " | forceWritten=" + forceWritten);
        }
        updateDashboard();
        statusHandler.postDelayed(() -> captureSnapshot(
                "activation-settled:" + enabled), 1500L);
    }

    private void captureSnapshot(String reason) {
        String detail = "reason=" + reason
                + " | " + buildDeviceSummary()
                + " | defaultHome=" + resolveDefaultHome()
                + " | activation=" + GestureActivation.isEnabled(this)
                + " | hooks={systemUi=" + GestureActivation.isSystemUiReady(this)
                + ",launcher=" + GestureActivation.isLauncherReady(this) + "}"
                + " | global/force_fsg_nav_bar="
                + GestureActivation.readGlobalInt(
                        this, GestureActivation.KEY_FORCE_FSG_NAV_BAR, -1)
                + " | secure/navigation_mode=" + readSecureInt("navigation_mode", -1);
        recordAppEvent(
                DiagnosticEvent.STATUS_SUCCESS,
                "snapshot",
                "capture-dashboard-state",
                detail);
        updateDashboard();
    }

    private void recordAppEvent(
            String status,
            String category,
            String operation,
            String detail) {
        database.insert(new DiagnosticEvent(
                0L,
                System.currentTimeMillis(),
                status,
                category,
                operation,
                detail,
                getPackageName(),
                Thread.currentThread().getName()));
    }

    private String buildDeviceSummary() {
        return "HGA=" + BuildConfig.VERSION_NAME
                + " | device=" + Build.MANUFACTURER + ' ' + Build.MODEL
                + " | Android=" + Build.VERSION.RELEASE
                + " | API=" + Build.VERSION.SDK_INT
                + " | build=" + Build.DISPLAY;
    }

    private String resolveDefaultHome() {
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
            ResolveInfo resolved = getPackageManager().resolveActivity(
                    intent, PackageManager.MATCH_DEFAULT_ONLY);
            if (resolved == null || resolved.activityInfo == null) {
                return getString(R.string.home_unresolved);
            }
            return new ComponentName(
                    resolved.activityInfo.packageName,
                    resolved.activityInfo.name).flattenToShortString();
        } catch (Throwable throwable) {
            return getString(R.string.home_unavailable);
        }
    }

    private String shortHome(String home) {
        if (home.startsWith("ginlemon.flowerfree")) {
            return "Smart Launcher";
        }
        if (home.startsWith("com.mi.android.globallauncher")
                || home.startsWith("com.miui.home")) {
            return "Xiaomi Launcher";
        }
        int slash = home.indexOf('/');
        return slash > 0 ? home.substring(0, slash) : home;
    }

    private int readSecureInt(String key, int fallback) {
        try {
            return Settings.Secure.getInt(getContentResolver(), key, fallback);
        } catch (Throwable throwable) {
            return fallback;
        }
    }

    private LinearLayout card() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(4), dp(16), dp(4));
        card.setBackground(surfaceDrawable(18));
        card.setElevation(dp(aeroGlass ? 3 : 1));
        return card;
    }

    private LinearLayout themedCard(int padding) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(padding, padding, padding, padding);
        card.setBackground(surfaceDrawable(20));
        card.setElevation(dp(aeroGlass ? 3 : 1));
        return card;
    }

    private View sectionTitle(String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        View marker = new View(this);
        marker.setBackground(gradient(new int[]{
                Color.rgb(32, 187, 232), Color.rgb(126, 83, 225), Color.rgb(236, 85, 151)},
                8, Color.TRANSPARENT, 0));
        row.addView(marker, new LinearLayout.LayoutParams(dp(5), dp(22)));
        TextView view = text(value, 15, primaryTextColor(), Typeface.BOLD);
        view.setLetterSpacing(0.025f);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        params.setMarginStart(dp(10));
        row.addView(view, params);
        return row;
    }

    private TextView text(String value, int size, int color, int style) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setTypeface(Typeface.DEFAULT, style);
        return view;
    }

    private View divider() {
        View divider = new View(this);
        divider.setBackgroundColor(aeroGlass
                ? Color.argb(70, 220, 233, 255) : Color.rgb(235, 238, 244));
        return divider;
    }

    private View space(int height) {
        View space = new View(this);
        space.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(height)));
        return space;
    }

    private GradientDrawable rounded(int color, int radius, int strokeColor, int strokeWidth) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));
        if (strokeWidth > 0) {
            drawable.setStroke(dp(strokeWidth), strokeColor);
        }
        return drawable;
    }

    private GradientDrawable gradient(
            int[] colors,
            int radius,
            int strokeColor,
            int strokeWidth) {
        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR, colors);
        drawable.setCornerRadius(dp(radius));
        if (strokeWidth > 0) {
            drawable.setStroke(dp(strokeWidth), strokeColor);
        }
        return drawable;
    }

    private GradientDrawable surfaceDrawable(int radius) {
        if (aeroGlass) {
            return darkMode
                    ? gradient(new int[]{
                            Color.rgb(24, 43, 63),
                            Color.rgb(48, 45, 66),
                            Color.rgb(34, 44, 61)}, radius,
                            Color.argb(125, 215, 231, 255), 1)
                    : gradient(new int[]{
                            Color.rgb(208, 220, 232),
                            Color.rgb(216, 216, 229),
                            Color.rgb(210, 218, 231)}, radius,
                            Color.argb(150, 84, 115, 151), 1);
        }
        return darkMode
                ? rounded(Color.rgb(56, 54, 62), radius, Color.rgb(56, 54, 62), 1)
                : rounded(Color.WHITE, radius, Color.rgb(228, 232, 240), 1);
    }

    private int primaryTextColor() {
        return darkMode ? Color.rgb(245, 241, 250) : Color.rgb(26, 35, 52);
    }

    private int secondaryTextColor() {
        return darkMode ? Color.rgb(203, 195, 211) : Color.rgb(105, 113, 128);
    }

    private int lighten(int color, float amount) {
        int red = Math.round(Color.red(color) + (255 - Color.red(color)) * amount);
        int green = Math.round(Color.green(color) + (255 - Color.green(color)) * amount);
        int blue = Math.round(Color.blue(color) + (255 - Color.blue(color)) * amount);
        return Color.rgb(red, green, blue);
    }

    private int darken(int color, float amount) {
        return Color.rgb(
                Math.round(Color.red(color) * (1f - amount)),
                Math.round(Color.green(color) * (1f - amount)),
                Math.round(Color.blue(color) * (1f - amount)));
    }

    private void applySystemChrome() {
        int background;
        if (darkMode) {
            background = aeroGlass ? Color.rgb(6, 17, 33) : Color.rgb(15, 14, 20);
        } else {
            background = aeroGlass ? Color.rgb(231, 246, 255) : Color.rgb(246, 248, 252);
        }
        Window window = getWindow();
        window.setStatusBarColor(background);
        window.setNavigationBarColor(background);
        window.setNavigationBarDividerColor(background);
        int flags = window.getDecorView().getSystemUiVisibility();
        if (darkMode) {
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        } else {
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        }
        window.getDecorView().setSystemUiVisibility(flags);
    }

    private GradientDrawable screenBackground() {
        if (aeroGlass) {
            return darkMode
                    ? gradient(new int[]{Color.rgb(6, 17, 33), Color.rgb(19, 37, 63),
                            Color.rgb(48, 27, 62)}, 0, Color.TRANSPARENT, 0)
                    : gradient(new int[]{Color.rgb(231, 246, 255), Color.rgb(245, 236, 255),
                            Color.rgb(221, 235, 255)}, 0, Color.TRANSPARENT, 0);
        }
        return rounded(darkMode ? Color.rgb(15, 14, 20) : Color.rgb(246, 248, 252),
                0, Color.TRANSPARENT, 0);
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
