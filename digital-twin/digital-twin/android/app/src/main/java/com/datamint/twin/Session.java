package com.datamint.twin;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;

/** Stores the JWT token, the signed-in user and the server URL. */
public final class Session {
    private Session() {}

    private static SharedPreferences sp() {
        return TwinApp.ctx().getSharedPreferences("twin", Context.MODE_PRIVATE);
    }

    public static String token() {
        String t = sp().getString("token", null);
        if (t != null && isTokenExpired(t)) {
            clear();
            return null;
        }
        return t;
    }

    public static boolean isLoggedIn() {
        return token() != null;
    }

    public static String fullName() { return sp().getString("name", ""); }
    public static String role() { return sp().getString("role", ""); }
    public static boolean isAdmin() { return "ADMIN".equals(role()); }

    public static String baseUrl() {
        String u = sp().getString("url", BuildConfig.DEFAULT_API_URL);
        return normalizeUrl(u);
    }

    public static void setBaseUrl(String url) {
        String normalized = normalizeUrl(url);
        sp().edit().putString("url", normalized).apply();
    }

    public static String normalizeUrl(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return BuildConfig.DEFAULT_API_URL;
        }
        String u = raw.trim();
        if (!u.startsWith("http://") && !u.startsWith("https://")) {
            u = "http://" + u;
        }
        if (!u.contains("/api")) {
            if (u.endsWith("/")) {
                u = u + "api/";
            } else {
                u = u + "/api/";
            }
        } else if (!u.endsWith("/")) {
            u = u + "/";
        }
        return u;
    }

    public static void save(Models.LoginResponse r) {
        if (r == null || r.token == null) return;
        SharedPreferences.Editor edit = sp().edit().putString("token", r.token);
        if (r.user != null) {
            String name = r.user.fullName != null && !r.user.fullName.isEmpty() ? r.user.fullName : r.user.username;
            edit.putString("name", name != null ? name : "")
                .putString("role", r.user.role != null ? r.user.role : "");
        }
        edit.apply();
    }

    public static void clear() {
        sp().edit().remove("token").remove("name").remove("role").apply();
    }

    private static boolean isTokenExpired(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return true;
            byte[] bytes = Base64.decode(parts[1], Base64.URL_SAFE);
            String json = new String(bytes, StandardCharsets.UTF_8);
            JSONObject payload = new JSONObject(json);
            if (payload.has("exp")) {
                long expSeconds = payload.getLong("exp");
                long nowSeconds = System.currentTimeMillis() / 1000L;
                return nowSeconds >= expSeconds;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
}
