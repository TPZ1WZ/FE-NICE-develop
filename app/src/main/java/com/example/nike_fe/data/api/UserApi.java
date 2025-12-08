package com.example.nike_fe.data.api;

import com.example.nike_fe.data.model.UpdateProfileRequest;
import com.example.nike_fe.data.model.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;

public interface UserApi {
    
    @GET("/api/v1/users/me")
    Call<User> getProfile(@Header("Authorization") String token);
    
    @PATCH("/api/v1/users/me")
    Call<User> updateProfile(
            @Header("Authorization") String token,
            @Body UpdateProfileRequest request
    );
}
