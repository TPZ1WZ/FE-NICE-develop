package com.example.nike_fe.data.api;

import com.example.nike_fe.data.model.Coupon;
import com.google.gson.annotations.SerializedName;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.POST;
import retrofit2.http.Path;

import java.util.List;

public interface UserCouponApi {
    @GET("api/v1/coupons/valid")
    Call<List<Coupon>> getValidCoupons(@Header("Authorization") String token);

    @GET("api/v1/coupons/code/{code}")
    Call<Coupon> getCouponByCode(@Header("Authorization") String token, @Path("code") String code);

    // If we need to apply check specifically (though getting by code is often
    // enough for validation)
    // The backend implies logic might be in placeOrder, but we want to
    // pre-validate.
}
