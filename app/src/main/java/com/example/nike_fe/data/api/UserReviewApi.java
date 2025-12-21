package com.example.nike_fe.data.api;

import com.example.nike_fe.data.model.CreateReviewRequest;
import com.example.nike_fe.data.model.Review; // Assuming Review model exists (was used in AdminApi)

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface UserReviewApi {
    @POST("api/v1/reviews")
    Call<Review> createReview(
            @Header("Authorization") String token,
            @Body CreateReviewRequest request);

    @GET("api/v1/reviews/product/{productId}")
    Call<java.util.List<Review>> getProductReviews(@Path("productId") Long productId);

    @GET("api/v1/reviews/product/{productId}/summary")
    Call<com.example.nike_fe.data.model.ReviewSummary> getReviewSummary(@Path("productId") Long productId);
}
