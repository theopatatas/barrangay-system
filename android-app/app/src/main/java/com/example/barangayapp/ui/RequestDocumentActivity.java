package com.example.barangayapp.ui;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import com.example.barangayapp.databinding.ActivityRequestDocumentBinding;
import com.example.barangayapp.model.DocumentRequestBody;
import com.example.barangayapp.model.MessageResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestDocumentActivity extends BaseActivity {
    private ActivityRequestDocumentBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRequestDocumentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String[] docs = {"Barangay Clearance", "Certificate of Residency", "Certificate of Indigency"};
        String[] slots = {"9AM-10AM", "10AM-11AM", "11AM-12NN", "1PM-2PM", "2PM-3PM"};

        binding.spinnerDocument.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, docs));
        binding.spinnerTime.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, slots));
        binding.buttonSubmitRequest.setOnClickListener(v -> submit());
    }

    private void submit() {
        DocumentRequestBody body = new DocumentRequestBody(
                binding.spinnerDocument.getSelectedItem().toString(),
                binding.inputDate.getText().toString().trim(),
                binding.spinnerTime.getSelectedItem().toString()
        );

        api().submitRequest(body).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful()) {
                    toast("Request submitted");
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
