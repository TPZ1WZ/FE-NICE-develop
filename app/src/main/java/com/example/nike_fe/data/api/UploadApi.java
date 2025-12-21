package com.example.nike_fe.data.api;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface UploadApi {
    @Multipart
    @POST("api/v1/upload")
    Call<String> uploadImage(@Part MultipartBody.Part image);
}
