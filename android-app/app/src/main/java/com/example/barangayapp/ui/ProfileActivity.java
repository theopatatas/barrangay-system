package com.example.barangayapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import com.example.barangayapp.R;
import com.example.barangayapp.databinding.ActivityProfileBinding;
import com.example.barangayapp.model.ChangePasswordBody;
import com.example.barangayapp.model.MessageResponse;
import com.example.barangayapp.model.UpdateProfileBody;
import com.example.barangayapp.model.User;
import com.example.barangayapp.storage.SessionManager;
import com.google.android.material.textfield.TextInputEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends BaseActivity {
    private ActivityProfileBinding binding;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sessionManager = new SessionManager(this);

        binding.buttonBack.setOnClickListener(v -> finish());
        binding.buttonSaveProfile.setOnClickListener(v -> saveProfile());
        binding.buttonChangePassword.setOnClickListener(v -> openChangePasswordDialog());
        binding.buttonLogout.setOnClickListener(v -> logout());

        bindUser();
    }

    private void bindUser() {
        User user = sessionManager.getUser();
        if (user == null) {
            return;
        }
        binding.inputFullName.setText(user.fullName);
        binding.inputEmail.setText(user.email);
        binding.inputAddress.setText(user.address);
    }

    private void saveProfile() {
        User current = sessionManager.getUser();
        if (current == null) {
            toast("Session expired");
            return;
        }

        UpdateProfileBody body = new UpdateProfileBody(
                current.fullName,
                text(binding.inputEmail),
                text(binding.inputAddress)
        );

        api().updateProfile(body).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User updated = response.body();
                    updated.fullName = current.fullName;
                    sessionManager.saveUser(updated);
                    toast("Profile updated");
                } else {
                    toast("Update failed");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                toast("Cannot connect to server");
            }
        });
    }

    private void openChangePasswordDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null, false);
        TextInputEditText currentPassword = view.findViewById(R.id.inputCurrentPassword);
        TextInputEditText newPassword = view.findViewById(R.id.inputNewPassword);
        TextInputEditText confirmPassword = view.findViewById(R.id.inputConfirmPassword);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Change Password")
                .setView(view)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String current = text(currentPassword);
            String next = text(newPassword);
            String confirm = text(confirmPassword);

            if (current.isEmpty() || next.isEmpty() || confirm.isEmpty()) {
                toast("Complete all password fields");
                return;
            }
            if (!next.equals(confirm)) {
                toast("New password and confirm password must match");
                return;
            }

            api().changePassword(new ChangePasswordBody(current, next)).enqueue(new Callback<MessageResponse>() {
                @Override
                public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                    if (response.isSuccessful()) {
                        toast("Password updated");
                        dialog.dismiss();
                    } else {
                        toast("Password update failed");
                    }
                }

                @Override
                public void onFailure(Call<MessageResponse> call, Throwable t) {
                    toast("Cannot connect to server");
                }
            });
        }));

        dialog.show();
    }

    private void logout() {
        api().logout().enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                clearSessionAndExit();
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                clearSessionAndExit();
            }
        });
    }

    private void clearSessionAndExit() {
        sessionManager.clear();
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private String text(android.widget.EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }
}
