package com.example.nike_fe.data.api;

import com.example.nike_fe.data.model.UpdateProfileRequest;
import com.example.nike_fe.data.model.User;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.Part;

public interface UserApi {
    
    @GET("/api/v1/users/me")
    Call<User> getProfile(@Header("Authorization") String token);
    
    @PATCH("/api/v1/users/me")
    Call<User> updateProfile(
            @Header("Authorization") String token,
            @Body UpdateProfileRequest request
    );

    @Multipart
    @PATCH("/api/v1/users/me/avatar")
    Call<User> updateAvatar(
            @Header("Authorization") String token,
            @Part MultipartBody.Part avatar
    );
}
