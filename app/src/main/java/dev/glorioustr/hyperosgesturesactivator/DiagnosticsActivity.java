package dev.glorioustr.hyperosgesturesactivator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Insets;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class DiagnosticsActivity extends Activity {
    private static final long LIVE_REFRESH_INTERVAL_MS = 750L;
    private static final int REQUEST_EXPORT_REPORT = 4101;

    private final Handler liveHandler = new Handler(Looper.getMainLooper());
    private final EventAdapter eventAdapter = new EventAdapter();
    private final Runnable liveRefresh = new Runnable() {
        @Override
        public void run() {
            reloadEvents();
            liveHandler.postDelayed(this, LIVE_REFRESH_INTERVAL_MS);
        }
    };

    private DiagnosticDatabase database;
    private TextView liveStateView;
    private TextView summaryView;
    private TextView filterView;
    private ListView eventList;
    private String activeFilter;
    private long renderedTotal = -1L;
    private String renderedFilter;
    private boolean darkMode;
    private boolean amoledMode;
    private boolean aeroGlass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = DiagnosticDatabase.get(this);
        SharedPreferences preferences = getSharedPreferences("ui_preferences", MODE_PRIVATE);
        String colorMode = preferences.getString("color_mode", "SYSTEM");
        boolean systemDark = (getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        amoledMode = "AMOLED".equals(colorMode);
        darkMode = amoledMode || "DARK".equals(colorMode)
                || ("SYSTEM".equals(colorMode) && systemDark);
        aeroGlass = "AERO_GLASS".equals(preferences.getString("content_style", "DEFAULT"));
        applySystemChrome();
        setTitle(R.string.screen_title);
        setContentView(buildScreen());
        recordAppEvent(
                DiagnosticEvent.STATUS_SUCCESS,
                "ui",
                "live-diagnostics-opened",
                buildDeviceSummary());
        captureAppSnapshot("screen-opened");
    }

    @Override
    protected void onStart() {
        super.onStart();
        liveStateView.setText(R.string.live_ready);
        liveStateView.setTextColor(Color.rgb(20, 125, 70));
        liveHandler.removeCallbacks(liveRefresh);
        liveHandler.post(liveRefresh);
    }

    @Override
    protected void onStop() {
        liveHandler.removeCallbacks(liveRefresh);
        liveStateView.setText(R.string.live_paused);
        liveStateView.setTextColor(Color.rgb(121, 116, 126));
        super.onStop();
    }

    private View buildScreen() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackground(screenBackground());
        root.setOnApplyWindowInsetsListener((view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsets.Type.systemBars());
            view.setPadding(
                    dp(16) + bars.left,
                    dp(20) + bars.top,
                    dp(16) + bars.right,
                    dp(12) + bars.bottom);
            return windowInsets;
        });

        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);

        TextView back = new TextView(this);
        back.setText("‹");
        back.setTextColor(primaryTextColor());
        back.setTextSize(34);
        back.setGravity(Gravity.CENTER);
        back.setContentDescription(getString(R.string.back_cd));
        back.setClickable(true);
        back.setFocusable(true);
        back.setOnClickListener(view -> finish());
        topBar.addView(back, new LinearLayout.LayoutParams(dp(44), dp(48)));

        TextView title = new TextView(this);
        title.setText(R.string.diagnostics_title);
        title.setTextColor(primaryTextColor());
        title.setTextSize(23);
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titlesParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        titlesParams.setMarginStart(dp(8));
        topBar.addView(title, titlesParams);
        topBar.addView(new View(this), new LinearLayout.LayoutParams(dp(44), dp(48)));
        root.addView(topBar, matchWrap());

        LinearLayout liveCard = new LinearLayout(this);
        liveCard.setOrientation(LinearLayout.VERTICAL);
        liveCard.setPadding(dp(16), dp(16), dp(16), dp(16));
        liveCard.setBackground(surfaceDrawable(16));
        LinearLayout liveHeader = new LinearLayout(this);
        liveHeader.setOrientation(LinearLayout.HORIZONTAL);
        liveHeader.setGravity(Gravity.CENTER_VERTICAL);
        TextView pulse = new TextView(this);
        pulse.setText("▰");
        pulse.setTextSize(22);
        pulse.setTextColor(Color.rgb(0, 214, 230));
        pulse.setGravity(Gravity.CENTER);
        pulse.setBackground(rounded(Color.rgb(19, 78, 101), 15,
                Color.argb(100, 98, 216, 255), 1));
        liveHeader.addView(pulse, new LinearLayout.LayoutParams(dp(52), dp(52)));
        LinearLayout liveTitles = new LinearLayout(this);
        liveTitles.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams liveTitleParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        liveTitleParams.setMarginStart(dp(14));
        liveTitles.addView(label(R.string.live_system_diagnostics_title, 20,
                primaryTextColor(), Typeface.BOLD), matchWrap());
        liveTitles.addView(label(R.string.live_system_diagnostics_desc, 12,
                secondaryTextColor(), Typeface.NORMAL), matchWrap());
        liveHeader.addView(liveTitles, liveTitleParams);
        liveStateView = label(R.string.live_monitoring, 12,
                Color.rgb(0, 203, 219), Typeface.BOLD);
        liveStateView.setGravity(Gravity.CENTER);
        liveStateView.setPadding(dp(12), dp(6), dp(12), dp(6));
        liveStateView.setBackground(rounded(Color.rgb(27, 76, 99), 24,
                Color.TRANSPARENT, 0));
        liveHeader.addView(liveStateView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, dp(38)));
        liveCard.addView(liveHeader, matchWrap());

        summaryView = new TextView(this);
        summaryView.setTextColor(secondaryTextColor());
        summaryView.setTextSize(12);
        summaryView.setPadding(0, dp(10), 0, dp(5));
        liveCard.addView(summaryView, matchWrap());

        filterView = new TextView(this);
        filterView.setText(getString(
                R.string.diagnostics_showing, getString(R.string.filter_all)));
        filterView.setTextColor(secondaryTextColor());
        filterView.setTextSize(10);
        liveCard.addView(filterView, matchWrap());

        LinearLayout controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.VERTICAL);
        controls.setPadding(0, dp(5), 0, dp(8));

        LinearLayout filters = new LinearLayout(this);
        filters.setOrientation(LinearLayout.HORIZONTAL);
        addWeightedButton(filters, filterButton(
                getString(R.string.filter_all), null));
        addWeightedButton(filters, filterButton(
                getString(R.string.filter_success), DiagnosticEvent.STATUS_SUCCESS));
        addWeightedButton(filters, filterButton(
                getString(R.string.filter_failure), DiagnosticEvent.STATUS_FAILURE));
        addWeightedButton(filters, filterButton(
                getString(R.string.filter_info), DiagnosticEvent.STATUS_INFO));
        controls.addView(filters, matchWrap());

        liveCard.addView(controls, matchWrap());

        eventList = new ListView(this);
        eventList.setAdapter(eventAdapter);
        eventList.setDividerHeight(dp(8));
        eventList.setClipToPadding(false);
        eventList.setPadding(0, dp(4), 0, dp(8));
        eventList.setBackgroundColor(Color.TRANSPARENT);
        eventList.setBackground(rounded(Color.rgb(8, 13, 24), 12,
                Color.rgb(35, 45, 63), 1));
        liveCard.addView(eventList, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(300)));
        LinearLayout.LayoutParams liveParams = matchWrap();
        liveParams.topMargin = dp(14);
        root.addView(liveCard, liveParams);

        TextView toolsTitle = label(R.string.diagnostics_tools_title, 18,
                primaryTextColor(), Typeface.BOLD);
        toolsTitle.setPadding(0, dp(16), 0, dp(8));
        root.addView(toolsTitle, matchWrap());
        root.addView(toolRow("▣", Color.rgb(45, 112, 224),
                R.string.action_export, R.string.diagnostics_export_tool_desc,
                view -> chooseReportDestination()), matchWrap());
        root.addView(toolRow("◎", Color.rgb(116, 82, 225),
                R.string.action_snapshot, R.string.diagnostics_snapshot_tool_desc,
                view -> captureAppSnapshot("manual-refresh")), matchWrap());
        root.addView(toolRow("⌫", Color.rgb(204, 72, 92),
                R.string.action_clear, R.string.diagnostics_clear_tool_desc,
                view -> confirmClear()), matchWrap());
        root.addView(toolRow("➤", Color.rgb(0, 171, 193),
                R.string.telegram_report_title, R.string.telegram_report_desc,
                view -> openTelegram()), matchWrap());
        return root;
    }

    private View toolRow(String symbol, int accent, int titleId, int descriptionId,
            View.OnClickListener listener) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(14), dp(11), dp(10), dp(11));
        row.setBackground(surfaceDrawable(14));
        TextView icon = label(symbol, 23, Color.WHITE, Typeface.BOLD);
        icon.setGravity(Gravity.CENTER);
        icon.setBackground(rounded(darken(accent, 0.28f), 14,
                Color.argb(95, 210, 235, 255), 1));
        row.addView(icon, new LinearLayout.LayoutParams(dp(48), dp(48)));
        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams copyParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        copyParams.setMarginStart(dp(13));
        copy.addView(label(titleId, 15, primaryTextColor(), Typeface.BOLD), matchWrap());
        copy.addView(label(descriptionId, 11, secondaryTextColor(), Typeface.NORMAL), matchWrap());
        row.addView(copy, copyParams);
        Button open = actionButton(getString(R.string.action_open), listener);
        open.setTextColor(darkMode ? Color.rgb(29, 47, 71) : Color.rgb(35, 61, 91));
        open.setBackground(rounded(Color.rgb(174, 211, 255), 22,
                Color.TRANSPARENT, 0));
        row.addView(open, new LinearLayout.LayoutParams(dp(72), dp(42)));
        row.setClickable(true);
        row.setFocusable(true);
        row.setOnClickListener(listener);
        LinearLayout.LayoutParams params = matchWrap();
        params.bottomMargin = dp(8);
        row.setLayoutParams(params);
        return row;
    }

    private TextView label(int stringId, int size, int color, int style) {
        return label(getString(stringId), size, color, style);
    }

    private TextView label(String value, int size, int color, int style) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setTypeface(Typeface.DEFAULT, style);
        return view;
    }

    private void openTelegram() {
        Intent telegram = new Intent(Intent.ACTION_VIEW,
                Uri.parse("tg://resolve?domain=glorioustr"));
        try {
            startActivity(telegram);
        } catch (Throwable ignored) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://t.me/glorioustr")));
            } catch (Throwable unavailable) {
                Toast.makeText(this, R.string.telegram_open_failed, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void applySystemChrome() {
        int background = amoledMode ? Color.BLACK
                : darkMode ? Color.rgb(7, 17, 32) : Color.rgb(240, 246, 253);
        getWindow().setStatusBarColor(background);
        getWindow().setNavigationBarColor(background);
        int flags = getWindow().getDecorView().getSystemUiVisibility();
        if (darkMode) flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        else flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
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

    private int darken(int color, float amount) {
        return Color.rgb(Math.round(Color.red(color) * (1f - amount)),
                Math.round(Color.green(color) * (1f - amount)),
                Math.round(Color.blue(color) * (1f - amount)));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_EXPORT_REPORT
                || resultCode != RESULT_OK
                || data == null
                || data.getData() == null) {
            return;
        }
        exportReport(data.getData());
    }

    private Button filterButton(String label, String filter) {
        return actionButton(label, view -> {
            activeFilter = filter;
            filterView.setText(getString(R.string.diagnostics_showing, label));
            recordAppEvent(
                    DiagnosticEvent.STATUS_SUCCESS,
                    "ui",
                    "filter-changed",
                    filter == null ? "ALL" : filter);
            reloadEvents();
        });
    }

    private Button actionButton(String label, View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextSize(12);
        button.setTextColor(primaryTextColor());
        button.setMinHeight(dp(44));
        button.setBackground(rounded(darkMode ? Color.rgb(43, 48, 61) : Color.WHITE,
                12, darkMode ? Color.rgb(82, 92, 112) : Color.rgb(221, 226, 236), 1));
        button.setOnClickListener(listener);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMarginEnd(dp(6));
        button.setLayoutParams(params);
        return button;
    }

    private void addWeightedButton(LinearLayout row, Button button) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f);
        params.setMarginEnd(dp(6));
        button.setLayoutParams(params);
        row.addView(button);
    }

    private void reloadEvents() {
        try {
            DiagnosticDatabase.Counts counts = database.counts();
            boolean sameFilter = activeFilter == null
                    ? renderedFilter == null
                    : activeFilter.equals(renderedFilter);
            if (counts.total == renderedTotal && sameFilter) {
                return;
            }
            List<DiagnosticEvent> events = database.latest(activeFilter);
            summaryView.setText(getString(R.string.diagnostics_count_summary,
                    counts.total, counts.success, counts.failure, counts.info));
            eventAdapter.replace(events);
            eventList.setSelection(0);
            renderedTotal = counts.total;
            renderedFilter = activeFilter;
        } catch (Throwable throwable) {
            liveStateView.setText(R.string.live_database_error);
            liveStateView.setTextColor(Color.rgb(186, 26, 26));
        }
    }

    private void captureAppSnapshot(String reason) {
        try {
            String detail = "reason=" + reason
                    + " | " + buildDeviceSummary()
                    + " | defaultHome=" + resolveDefaultHome()
                    + " | activation=" + GestureActivation.isEnabled(this)
                    + " | hooks={systemUi=" + GestureActivation.isSystemUiReady(this)
                    + ",launcher=" + GestureActivation.isLauncherReady(this) + "}"
                    + " | global/force_fsg_nav_bar="
                    + GestureActivation.readGlobalInt(
                            this, GestureActivation.KEY_FORCE_FSG_NAV_BAR, -1)
                    + " | secure/force_fsg_nav_bar=" + readSecure("force_fsg_nav_bar")
                    + " | secure/navigation_mode=" + readSecure("navigation_mode")
                    + " | secure/navigation_bar_mode=" + readSecure("navigation_bar_mode")
                    + " | secure/miui_fullscreen_gesture="
                    + readSecure("miui_fullscreen_gesture");
            recordAppEvent(
                    DiagnosticEvent.STATUS_SUCCESS,
                    "snapshot",
                    "capture-app-state",
                    detail);
        } catch (Throwable throwable) {
            recordAppEvent(
                    DiagnosticEvent.STATUS_FAILURE,
                    "snapshot",
                    "capture-app-state",
                    throwable.getClass().getName() + ": " + throwable.getMessage());
        }
        reloadEvents();
    }

    private void chooseReportDestination() {
        SimpleDateFormat filenameFormat =
                new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US);
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType("text/plain")
                .putExtra(
                        Intent.EXTRA_TITLE,
                        "hga-diagnostics-"
                                + filenameFormat.format(new Date())
                                + ".txt");
        try {
            startActivityForResult(intent, REQUEST_EXPORT_REPORT);
        } catch (Throwable throwable) {
            recordAppEvent(
                    DiagnosticEvent.STATUS_FAILURE,
                    "export",
                    "choose-diagnostics-report-destination",
                    throwable.getClass().getName() + ": " + throwable.getMessage());
            Toast.makeText(this,
                    getString(R.string.export_picker_failed),
                    Toast.LENGTH_LONG).show();
            renderedTotal = -1L;
            reloadEvents();
        }
    }

    private void exportReport(Uri destination) {
        liveStateView.setText(R.string.live_exporting);
        liveStateView.setTextColor(Color.rgb(71, 83, 160));
        new Thread(() -> {
            try (OutputStream output = getContentResolver().openOutputStream(
                    destination, "wt")) {
                if (output == null) {
                    throw new IllegalStateException("Document provider returned no output stream");
                }
                List<DiagnosticEvent> events = database.all();
                DiagnosticDatabase.Counts counts = database.counts();
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                        output, StandardCharsets.UTF_8))) {
                    writeReport(writer, counts, events);
                }
                recordAppEvent(
                        DiagnosticEvent.STATUS_SUCCESS,
                        "export",
                        "write-diagnostics-report",
                        "uri=" + destination + " | exportedEvents=" + events.size());
                runOnUiThread(() -> {
                    renderedTotal = -1L;
                    reloadEvents();
                    liveStateView.setText(R.string.live_exported);
                    liveStateView.setTextColor(Color.rgb(20, 125, 70));
                    Toast.makeText(this,
                            getString(R.string.export_saved_toast),
                            Toast.LENGTH_LONG).show();
                });
            } catch (Throwable throwable) {
                recordAppEvent(
                        DiagnosticEvent.STATUS_FAILURE,
                        "export",
                        "write-diagnostics-report",
                        throwable.getClass().getName() + ": " + throwable.getMessage());
                runOnUiThread(() -> {
                    renderedTotal = -1L;
                    reloadEvents();
                    liveStateView.setText(R.string.live_export_failed);
                    liveStateView.setTextColor(Color.rgb(186, 26, 26));
                    Toast.makeText(this,
                            getString(R.string.export_failed_toast),
                            Toast.LENGTH_LONG).show();
                });
            }
        }, "diagnostics-export").start();
    }

    private void writeReport(
            BufferedWriter writer,
            DiagnosticDatabase.Counts counts,
            List<DiagnosticEvent> events) throws Exception {
        SimpleDateFormat reportTime =
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z", Locale.US);
        writer.write("HyperOS Gestures Activator — Tanılama Raporu\n");
        writer.write("Oluşturulma: " + reportTime.format(new Date()) + "\n");
        writer.write(buildDeviceSummary() + "\n");
        writer.write("defaultHome=" + resolveDefaultHome() + "\n");
        writer.write("activation=" + GestureActivation.isEnabled(this) + "\n");
        writer.write("hooks={systemUi=" + GestureActivation.isSystemUiReady(this)
                + ",launcher=" + GestureActivation.isLauncherReady(this) + "}\n");
        writer.write("global/force_fsg_nav_bar="
                + GestureActivation.readGlobalInt(
                        this, GestureActivation.KEY_FORCE_FSG_NAV_BAR, -1) + "\n");
        writer.write("secure/navigation_mode=" + readSecure("navigation_mode") + "\n");
        writer.write("counts={total=" + counts.total
                + ",success=" + counts.success
                + ",failure=" + counts.failure
                + ",info=" + counts.info + "}\n");
        writer.write("\n=== OLAYLAR (EN YENİDEN ESKİYE) ===\n");
        for (DiagnosticEvent event : events) {
            writer.write("\n[#" + event.id + "] "
                    + reportTime.format(new Date(event.timestamp))
                    + "  " + event.status
                    + "  " + event.category + '/' + event.operation + "\n");
            writer.write("Kaynak: " + event.processName
                    + " · " + event.threadName + "\n");
            writer.write(event.detail.isEmpty() ? "—\n" : event.detail + "\n");
        }
    }

    private void confirmClear() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.clear_title)
                .setMessage(R.string.clear_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    database.clear();
                    renderedTotal = -1L;
                    recordAppEvent(
                            DiagnosticEvent.STATUS_SUCCESS,
                            "storage",
                            "diagnostic-events-cleared",
                            "User cleared the diagnostics database");
                    reloadEvents();
                })
                .show();
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
                return "unresolved";
            }
            return new ComponentName(
                    resolved.activityInfo.packageName,
                    resolved.activityInfo.name).flattenToShortString();
        } catch (Throwable throwable) {
            return "error:" + throwable.getClass().getSimpleName();
        }
    }

    private String readSecure(String key) {
        try {
            String value = Settings.Secure.getString(getContentResolver(), key);
            return value == null ? "<null>" : value;
        } catch (Throwable throwable) {
            return "error:" + throwable.getClass().getSimpleName();
        }
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
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

    private final class EventAdapter extends BaseAdapter {
        private final List<DiagnosticEvent> events = new ArrayList<>();
        private final SimpleDateFormat timeFormat =
                new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault());

        void replace(List<DiagnosticEvent> replacement) {
            events.clear();
            events.addAll(replacement);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return events.size();
        }

        @Override
        public DiagnosticEvent getItem(int position) {
            return events.get(position);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).id;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            EventRow row;
            if (convertView instanceof LinearLayout && convertView.getTag() instanceof EventRow) {
                row = (EventRow) convertView.getTag();
            } else {
                row = createEventRow();
                convertView = row.container;
                convertView.setTag(row);
            }
            bindEventRow(row, getItem(position));
            return convertView;
        }

        private EventRow createEventRow() {
            LinearLayout container = new LinearLayout(DiagnosticsActivity.this);
            container.setOrientation(LinearLayout.VERTICAL);
            container.setPadding(dp(12), dp(10), dp(12), dp(10));

            TextView header = new TextView(DiagnosticsActivity.this);
            header.setTextSize(13);
            header.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            container.addView(header, matchWrap());

            TextView detail = new TextView(DiagnosticsActivity.this);
            detail.setTextColor(Color.rgb(194, 202, 218));
            detail.setTextSize(12);
            detail.setTypeface(Typeface.MONOSPACE);
            detail.setTextIsSelectable(true);
            detail.setPadding(0, dp(5), 0, 0);
            container.addView(detail, matchWrap());

            TextView source = new TextView(DiagnosticsActivity.this);
            source.setTextColor(Color.rgb(124, 137, 159));
            source.setTextSize(10);
            source.setGravity(Gravity.END);
            source.setPadding(0, dp(5), 0, 0);
            container.addView(source, matchWrap());
            return new EventRow(container, header, detail, source);
        }

        private void bindEventRow(EventRow row, DiagnosticEvent event) {
            int accent;
            if (DiagnosticEvent.STATUS_SUCCESS.equals(event.status)) {
                accent = Color.rgb(61, 224, 161);
            } else if (DiagnosticEvent.STATUS_FAILURE.equals(event.status)) {
                accent = Color.rgb(255, 111, 121);
            } else {
                accent = Color.rgb(89, 190, 255);
            }
            row.container.setBackground(rounded(Color.rgb(8, 13, 24), 0,
                    Color.TRANSPARENT, 0));
            row.header.setTextColor(accent);
            row.header.setText(timeFormat.format(new Date(event.timestamp))
                    + "  " + event.status
                    + "  " + event.category + '/' + event.operation);
            row.detail.setText(event.detail.isEmpty() ? "—" : event.detail);
            row.source.setText(event.processName + " · " + event.threadName + " · #" + event.id);
        }
    }

    private static final class EventRow {
        final LinearLayout container;
        final TextView header;
        final TextView detail;
        final TextView source;

        EventRow(
                LinearLayout container,
                TextView header,
                TextView detail,
                TextView source) {
            this.container = container;
            this.header = header;
            this.detail = detail;
            this.source = source;
        }
    }
}
