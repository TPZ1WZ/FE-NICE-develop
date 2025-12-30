package com.example.nike_fe.data.api;

import com.example.nike_fe.data.model.AdminProduct;
import com.example.nike_fe.data.model.ProductStats;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AdminProductApi {

        @GET("/api/admin/products")
        Call<List<AdminProduct>> getProducts(
                        @Header("Authorization") String token,
                        @Query("search") String search);

        @GET("/api/admin/products/{id}")
        Call<AdminProduct> getProductById(
                        @Header("Authorization") String token,
                        @Path("id") Long id);

        @POST("/api/admin/products")
        Call<AdminProduct> createProduct(
                        @Header("Authorization") String token,
                        @Body AdminProduct product);

        @PUT("/api/admin/products/{id}")
        Call<AdminProduct> updateProduct(
                        @Header("Authorization") String token,
                        @Path("id") Long id,
                        @Body AdminProduct product);

        @DELETE("/api/admin/products/{id}")
        Call<com.example.nike_fe.data.model.DeleteProductResponse> deleteProduct(
                        @Header("Authorization") String token,
                        @Path("id") Long id);

        @GET("/api/admin/products/stats")
        Call<ProductStats> getStats(
                        @Header("Authorization") String token);
}
