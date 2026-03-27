package com.example.barangayapp.storage;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.barangayapp.model.User;
import com.google.gson.Gson;

public class SessionManager {
    private static final String PREF_NAME = "barangay_session";
    private static final String KEY_USER = "user";
    private static final String KEY_SERVER_URL = "server_url";
    private static final String DEFAULT_SERVER_URL = "http://192.168.1.71:3000/";
    private final SharedPreferences preferences;
    private final Gson gson = new Gson();

    public SessionManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveUser(User user) {
        preferences.edit().putString(KEY_USER, gson.toJson(user)).apply();
    }

    public User getUser() {
        String json = preferences.getString(KEY_USER, null);
        return json == null ? null : gson.fromJson(json, User.class);
    }

    public boolean isLoggedIn() {
        return getUser() != null;
    }

    public void saveServerUrl(String serverUrl) {
        preferences.edit().putString(KEY_SERVER_URL, normalizeServerUrl(serverUrl)).apply();
    }

    public String getServerUrl() {
        return normalizeServerUrl(preferences.getString(KEY_SERVER_URL, DEFAULT_SERVER_URL));
    }

    public static String normalizeServerUrl(String value) {
        if (value == null) {
            return DEFAULT_SERVER_URL;
        }

        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return DEFAULT_SERVER_URL;
        }

        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            trimmed = "http://" + trimmed;
        }

        if (!trimmed.endsWith("/")) {
            trimmed = trimmed + "/";
        }

        return trimmed;
    }

    public void clear() {
        preferences.edit().remove(KEY_USER).apply();
    }
}
