package com.example.barangayapp.data;

import com.example.barangayapp.model.Announcement;
import com.example.barangayapp.model.ChangePasswordBody;
import com.example.barangayapp.model.ComplaintBody;
import com.example.barangayapp.model.DocumentRequestBody;
import com.example.barangayapp.model.LoginRequest;
import com.example.barangayapp.model.LoginResponse;
import com.example.barangayapp.model.MessageResponse;
import com.example.barangayapp.model.RegisterRequest;
import com.example.barangayapp.model.UpdateProfileBody;
import com.example.barangayapp.model.User;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    @POST("api/login")
    Call<LoginResponse> login(@Body LoginRequest body);

    @POST("api/register")
    Call<MessageResponse> register(@Body RegisterRequest body);

    @GET("api/me")
    Call<User> getCurrentUser();

    @POST("api/request")
    Call<MessageResponse> submitRequest(@Body DocumentRequestBody body);

    @POST("api/complaint")
    Call<MessageResponse> submitComplaint(@Body ComplaintBody body);

    @GET("api/announcements")
    Call<List<Announcement>> getAnnouncements();

    @POST("api/updateProfile")
    Call<User> updateProfile(@Body UpdateProfileBody body);

    @POST("api/changePassword")
    Call<MessageResponse> changePassword(@Body ChangePasswordBody body);

    @POST("api/logout")
    Call<MessageResponse> logout();
}
