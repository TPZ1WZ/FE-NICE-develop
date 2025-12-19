package com.example.nike_fe.data.api;

import com.example.nike_fe.data.model.Category;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;

public interface CategoryApi {
    @GET("/api/v1/categories")
    Call<List<Category>> getCategories();
}
