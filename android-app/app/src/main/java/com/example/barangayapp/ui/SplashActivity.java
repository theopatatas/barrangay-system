package com.example.barangayapp.ui;

import android.content.Intent;
import android.os.Bundle;
import com.example.barangayapp.model.User;
import com.example.barangayapp.storage.SessionManager;
import java.util.Objects;

public class SplashActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager sessionManager = new SessionManager(this);
        User user = sessionManager.getUser();
        if (Objects.nonNull(user) && "admin".equalsIgnoreCase(user.role)) {
            sessionManager.clear();
            user = null;
        }
        startActivity(new Intent(this, Objects.nonNull(user) ? DashboardActivity.class : LoginActivity.class));
        finish();
    }
}
