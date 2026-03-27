package com.example.barangayapp.ui;

import android.content.Intent;
import android.os.Bundle;
import com.example.barangayapp.databinding.ActivityLoginBinding;
import com.example.barangayapp.model.LoginRequest;
import com.example.barangayapp.model.LoginResponse;
import com.example.barangayapp.storage.SessionManager;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends BaseActivity {
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.linkCreateAccount.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        binding.buttonLogin.setOnClickListener(v -> login());
    }

    private void login() {
        String email = binding.inputEmail.getText() == null ? "" : binding.inputEmail.getText().toString().trim();
        String password = binding.inputPassword.getText() == null ? "" : binding.inputPassword.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            toast("Enter your email and password");
            return;
        }

        api().login(new LoginRequest(email, password)).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && Objects.nonNull(response.body()) && Objects.nonNull(response.body().user)) {
                    if ("admin".equalsIgnoreCase(response.body().user.role)) {
                        new SessionManager(LoginActivity.this).clear();
                        toast("Admin accounts are not available in the resident app");
                        return;
                    }
                    new SessionManager(LoginActivity.this).saveUser(response.body().user);
                    startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                    finish();
                } else {
                    toast("Invalid login or unreachable server");
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                toast("Cannot connect to server");
            }
        });
    }
}
