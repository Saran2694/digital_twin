package com.datamint.twin;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.widget.Toast;

public final class Ui {
    private Ui() {}

    public static int statusColor(String s) {
        if ("RUNNING".equals(s)) return 0xFF22C55E;
        if ("WARNING".equals(s)) return 0xFFF5A623;
        return 0xFFEF4444;
    }

    public static String statusLabel(String s) {
        if ("RUNNING".equals(s)) return "Normal";
        if ("WARNING".equals(s)) return "Warning";
        return "Offline";
    }

    public static int severityColor(String sev, String status) {
        if ("RESOLVED".equals(status)) return 0xFF2F8CFF;
        return "CRITICAL".equals(sev) ? 0xFFEF4444 : 0xFFF5A623;
    }

    public static int maintColor(String s) {
        if ("COMPLETED".equals(s)) return 0xFF17A34A;
        if ("IN_PROGRESS".equals(s)) return 0xFF1B66C9;
        return 0xFF7A5A14;
    }

    public static String maintLabel(String s) {
        if ("COMPLETED".equals(s)) return "Completed";
        if ("IN_PROGRESS".equals(s)) return "In Progress";
        return "Pending";
    }

    /** "2026-10-31T06:12:00" -> "06:12" */
    public static String time(String iso) { return iso != null && iso.length() >= 16 ? iso.substring(11, 16) : "--"; }

    /** "2026-10-31T06:12:00" -> "10-31 06:12" */
    public static String dateTime(String iso) { return iso != null && iso.length() >= 16 ? iso.substring(5, 10) + " " + iso.substring(11, 16) : "--"; }

    public static String f(Double v, int digits) { return v == null ? "--" : String.format(java.util.Locale.US, "%." + digits + "f", v); }

    public static GradientDrawable pill(int color, float radiusPx) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(radiusPx);
        return d;
    }

    public static void toast(Context c, String m) { Toast.makeText(c, m, Toast.LENGTH_SHORT).show(); }
}
