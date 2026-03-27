package com.example.barangayapp.ui;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import com.example.barangayapp.R;
import com.example.barangayapp.databinding.ActivityAnnouncementsBinding;
import com.example.barangayapp.model.Announcement;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnnouncementsActivity extends BaseActivity {
    private ActivityAnnouncementsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAnnouncementsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.buttonBack.setOnClickListener(v -> finish());
        loadAnnouncements();
    }

    private void loadAnnouncements() {
        api().getAnnouncements().enqueue(new Callback<List<Announcement>>() {
            @Override
            public void onResponse(Call<List<Announcement>> call, Response<List<Announcement>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    renderAnnouncements(response.body());
                } else {
                    toast("Failed to load announcements");
                }
            }

            @Override
            public void onFailure(Call<List<Announcement>> call, Throwable t) {
                toast("Cannot connect to server");
            }
        });
    }

    private void renderAnnouncements(List<Announcement> items) {
        binding.announcementsContainer.removeAllViews();
        if (items.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No announcements yet");
            empty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            empty.setTextColor(getColor(R.color.text_secondary));
            binding.announcementsContainer.addView(empty);
            return;
        }

        for (Announcement announcement : items) {
            CardView card = new CardView(this);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            cardParams.bottomMargin = dp(14);
            card.setLayoutParams(cardParams);
            card.setRadius(dp(20));
            card.setCardElevation(dp(4));
            card.setCardBackgroundColor(getColor(R.color.surface_card));

            LinearLayout content = new LinearLayout(this);
            content.setOrientation(LinearLayout.VERTICAL);
            content.setPadding(dp(18), dp(18), dp(18), dp(18));

            TextView title = new TextView(this);
            title.setText(announcement.title);
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            title.setTypeface(Typeface.DEFAULT_BOLD);
            title.setTextColor(getColor(R.color.primary));

            TextView label = new TextView(this);
            LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            labelParams.topMargin = dp(10);
            label.setLayoutParams(labelParams);
            label.setText("Description");
            label.setTypeface(Typeface.DEFAULT_BOLD);
            label.setTextColor(getColor(R.color.text_primary));

            TextView body = new TextView(this);
            LinearLayout.LayoutParams bodyParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            bodyParams.topMargin = dp(8);
            body.setLayoutParams(bodyParams);
            body.setText(announcement.body());
            body.setLineSpacing(0f, 1.35f);
            body.setTextColor(getColor(R.color.text_secondary));
            body.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

            content.addView(title);
            content.addView(label);
            content.addView(body);
            card.addView(content);
            binding.announcementsContainer.addView(card);
        }
    }

    private int dp(int value) {
        return Math.round(getResources().getDisplayMetrics().density * value);
    }
}
