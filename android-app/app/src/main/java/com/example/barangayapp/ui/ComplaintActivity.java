package com.example.barangayapp.ui;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import com.example.barangayapp.databinding.ActivityComplaintBinding;
import com.example.barangayapp.model.ComplaintBody;
import com.example.barangayapp.model.MessageResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ComplaintActivity extends BaseActivity {
    private ActivityComplaintBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityComplaintBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String[] categories = {"Noise Disturbance", "Neighborhood Issue", "Sanitation Concern", "Other"};
        binding.spinnerCategory.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories));
        binding.buttonSubmitComplaint.setOnClickListener(v -> submit());
    }

    private void submit() {
        ComplaintBody body = new ComplaintBody(
                binding.spinnerCategory.getSelectedItem().toString(),
                binding.inputSubject.getText().toString().trim(),
                binding.inputDescription.getText().toString().trim()
        );

        api().submitComplaint(body).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful()) {
                    toast("Complaint submitted");
                    finish();
                } else {
                    toast("Submission failed");
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                toast("Cannot connect to server");
            }
        });
    }
}
