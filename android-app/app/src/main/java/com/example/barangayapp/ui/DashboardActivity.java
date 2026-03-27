package com.example.barangayapp.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import com.example.barangayapp.R;
import com.example.barangayapp.databinding.ActivityDashboardBinding;
import com.example.barangayapp.model.Complaint;
import com.example.barangayapp.model.ComplaintBody;
import com.example.barangayapp.model.DocumentRequest;
import com.example.barangayapp.model.DocumentRequestBody;
import com.example.barangayapp.model.MessageResponse;
import com.example.barangayapp.model.NotificationItem;
import com.example.barangayapp.model.User;
import com.example.barangayapp.storage.SessionManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends BaseActivity {
    private static final String[] DOCUMENTS = {
            "Select Document",
            "Barangay Clearance",
            "Certificate of Residency",
            "Certificate of Indigency"
    };

    private static final String[] CATEGORIES = {
            "Select Category",
            "Noise Disturbance",
            "Neighborhood Issue",
            "Sanitation Concern",
            "Other"
    };

    private ActivityDashboardBinding binding;
    private SessionManager sessionManager;
    private Button[] slotButtons;
    private String selectedSlot;
    private final Calendar selectedDate = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sessionManager = new SessionManager(this);

        binding.spinnerDocument.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, DOCUMENTS));
        binding.spinnerCategory.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, CATEGORIES));

        slotButtons = new Button[]{
                binding.buttonSlot1,
                binding.buttonSlot2,
                binding.buttonSlot3,
                binding.buttonSlot4,
                binding.buttonSlot5
        };
        for (Button button : slotButtons) {
            button.setOnClickListener(v -> selectSlot(((Button) v).getText().toString(), (Button) v));
        }

        binding.inputDate.setOnClickListener(v -> openDatePicker());
        binding.buttonSubmitRequest.setOnClickListener(v -> submitRequest());
        binding.buttonSubmitComplaint.setOnClickListener(v -> submitComplaint());
        binding.buttonAnnouncementsTab.setOnClickListener(v -> startActivity(new Intent(this, AnnouncementsActivity.class)));
        binding.buttonProfileTab.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        binding.buttonDashboardTab.setOnClickListener(v -> { });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboard();
    }

    private void openDatePicker() {
        Calendar now = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar picked = Calendar.getInstance();
                    picked.set(year, month, dayOfMonth, 0, 0, 0);
                    picked.set(Calendar.MILLISECOND, 0);
                    int day = picked.get(Calendar.DAY_OF_WEEK);
                    if (day == Calendar.SATURDAY || day == Calendar.SUNDAY) {
                        toast("Weekdays only");
                        return;
                    }
                    selectedDate.setTimeInMillis(picked.getTimeInMillis());
                    binding.inputDate.setText(dateFormat.format(selectedDate.getTime()));
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );
        dialog.getDatePicker().setMinDate(now.getTimeInMillis());
        dialog.show();
    }

    private void selectSlot(String slot, Button selectedButton) {
        selectedSlot = slot;
        for (Button button : slotButtons) {
            boolean active = button == selectedButton;
            button.setBackgroundResource(active ? R.drawable.bg_slot_selected : R.drawable.bg_slot_idle);
            button.setTextColor(getColor(active ? android.R.color.white : R.color.primary));
        }
    }

    private void loadDashboard() {
        api().getCurrentUser().enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    sessionManager.saveUser(user);
                    binding.textWelcome.setText("Welcome, " + user.fullName);
                    bindRequests(user.requests);
                    bindComplaints(user.complaints);
                    bindUnreadBadge(user.notifications);
                } else {
                    toast("Failed to load dashboard");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                toast("Cannot connect to server");
            }
        });
    }

    private void bindRequests(List<DocumentRequest> requests) {
        List<String> items = new ArrayList<>();
        if (requests != null) {
            for (DocumentRequest request : requests) {
                if ("Pending".equalsIgnoreCase(request.status)) {
                    items.add("• " + request.doc + "\n  " + request.date + " • " + request.time + " • Pending");
                }
            }
        }
        binding.textPendingRequests.setText(items.isEmpty() ? "No pending requests" : join(items));
    }

    private void bindComplaints(List<Complaint> complaints) {
        List<String> items = new ArrayList<>();
        if (complaints != null) {
            for (Complaint complaint : complaints) {
                if ("Pending".equalsIgnoreCase(complaint.status)) {
                    items.add("• " + complaint.cat + "\n  " + complaint.subj + " • Pending");
                }
            }
        }
        binding.textPendingComplaints.setText(items.isEmpty() ? "No pending complaints" : join(items));
    }

    private void bindUnreadBadge(List<NotificationItem> notifications) {
        int unread = 0;
        if (notifications != null) {
            for (NotificationItem item : notifications) {
                if (item.isRead == 0) {
                    unread++;
                }
            }
        }
        binding.textUnreadBadge.setText(String.valueOf(unread));
        binding.textUnreadBadge.setVisibility(unread > 0 ? View.VISIBLE : View.GONE);
    }

    private void submitRequest() {
        if (binding.spinnerDocument.getSelectedItemPosition() == 0) {
            toast("Select a document type");
            return;
        }
        String date = binding.inputDate.getText() == null ? "" : binding.inputDate.getText().toString().trim();
        if (date.isEmpty()) {
            toast("Select a pickup date");
            return;
        }
        if (selectedSlot == null || selectedSlot.isEmpty()) {
            toast("Select a time slot");
            return;
        }

        api().submitRequest(new DocumentRequestBody(
                binding.spinnerDocument.getSelectedItem().toString(),
                date,
                selectedSlot
        )).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful()) {
                    toast("Request submitted");
                    resetRequestForm();
                    loadDashboard();
                } else {
                    toast("Request failed");
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                toast("Cannot connect to server");
            }
        });
    }

    private void submitComplaint() {
        if (binding.spinnerCategory.getSelectedItemPosition() == 0) {
            toast("Select a category");
            return;
        }

        String subject = value(binding.inputComplaintSubject);
        String description = value(binding.inputComplaintDescription);
        if (subject.isEmpty() || description.isEmpty()) {
            toast("Complete the complaint form");
            return;
        }

        api().submitComplaint(new ComplaintBody(
                binding.spinnerCategory.getSelectedItem().toString(),
                subject,
                description
        )).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful()) {
                    toast("Complaint submitted");
                    resetComplaintForm();
                    loadDashboard();
                } else {
                    toast("Complaint failed");
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                toast("Cannot connect to server");
            }
        });
    }

    private void resetRequestForm() {
        binding.spinnerDocument.setSelection(0);
        binding.inputDate.setText("");
        selectedSlot = null;
        for (Button button : slotButtons) {
            button.setBackgroundResource(R.drawable.bg_slot_idle);
            button.setTextColor(getColor(R.color.primary));
        }
    }

    private void resetComplaintForm() {
        binding.spinnerCategory.setSelection(0);
        binding.inputComplaintSubject.setText("");
        binding.inputComplaintDescription.setText("");
    }

    private String join(List<String> items) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) {
                builder.append("\n\n");
            }
            builder.append(items.get(i));
        }
        return builder.toString();
    }

    private String value(android.widget.EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }
}
