package com.example.barangayapp.ui;

import android.os.Bundle;
import com.example.barangayapp.databinding.ActivityRegisterBinding;
import com.example.barangayapp.model.MessageResponse;
import com.example.barangayapp.model.RegisterRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends BaseActivity {
    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.linkBackLogin.setOnClickListener(v -> finish());
        binding.buttonRegister.setOnClickListener(v -> register());
    }

    private void register() {
        String name = value(binding.inputFullName);
        String address = value(binding.inputAddress);
        String email = value(binding.inputEmail);
        String password = value(binding.inputPassword);
        String confirmPassword = value(binding.inputConfirmPassword);

        if (name.isEmpty() || address.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            toast("Complete all fields");
            return;
        }

        if (!password.equals(confirmPassword)) {
            toast("Passwords do not match");
            return;
        }

        api().register(new RegisterRequest(name, email, password, address)).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful()) {
                    toast("Account created");
                    finish();
                } else {
                    toast("Registration failed");
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                toast("Cannot connect to server");
            }
        });
    }

    private String value(android.widget.EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }
}
