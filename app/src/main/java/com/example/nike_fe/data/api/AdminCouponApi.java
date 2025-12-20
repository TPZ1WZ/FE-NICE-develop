package com.example.nike_fe.data.api;

import com.example.nike_fe.data.model.Coupon;
import com.example.nike_fe.data.model.CouponListResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AdminCouponApi {

    @GET("api/admin/coupons")
    Call<CouponListResponse> getCoupons(
            @Header("Authorization") String token,
            @Query("page") int page,
            @Query("size") int size,
            @Query("sort") String sort);

    @GET("api/admin/coupons/{id}")
    Call<Coupon> getCouponById(
            @Header("Authorization") String token,
            @Path("id") Long id);

    @POST("api/admin/coupons")
    Call<Coupon> createCoupon(
            @Header("Authorization") String token,
            @Body Coupon coupon);

    @PUT("api/admin/coupons/{id}")
    Call<Coupon> updateCoupon(
            @Header("Authorization") String token,
            @Path("id") Long id,
            @Body Coupon coupon);

    @DELETE("api/admin/coupons/{id}")
    Call<Void> deleteCoupon(
            @Header("Authorization") String token,
            @Path("id") Long id);
}
