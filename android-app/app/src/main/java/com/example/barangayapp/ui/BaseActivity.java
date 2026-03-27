package com.example.barangayapp.ui;

import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.barangayapp.data.ApiClient;
import com.example.barangayapp.data.ApiService;
import java.io.IOException;
import okhttp3.ResponseBody;

public abstract class BaseActivity extends AppCompatActivity {
    protected ApiService api() {
        return ApiClient.getInstance(this).create(ApiService.class);
    }

    protected void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    protected String errorMessage(ResponseBody body, String fallback) {
        if (body == null) {
            return fallback;
        }
        try {
            String raw = body.string();
            return raw == null || raw.trim().isEmpty() ? fallback : raw;
        } catch (IOException e) {
            return fallback;
        }
    }
}
