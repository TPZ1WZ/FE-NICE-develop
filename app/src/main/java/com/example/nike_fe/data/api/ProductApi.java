package com.example.nike_fe.data.api;

import com.example.nike_fe.data.model.Product;
import com.example.nike_fe.data.model.ProductDetail;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ProductApi {
    
    @GET("/api/v1/products")
    Call<List<Product>> getProducts(
            @Query("name") String name,
            @Query("minPrice") Double minPrice,
            @Query("maxPrice") Double maxPrice
    );
    
    @GET("/api/v1/products/{id}")
    Call<ProductDetail> getProductById(@Path("id") Long id);
}
