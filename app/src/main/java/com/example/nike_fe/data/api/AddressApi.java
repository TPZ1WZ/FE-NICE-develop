package com.example.nike_fe.data.api;

import com.example.nike_fe.data.model.Address;
import com.example.nike_fe.data.model.AddressRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface AddressApi {
    
    @GET("api/user/addresses")
    Call<List<Address>> getUserAddresses(@Header("Authorization") String token);
    
    @GET("api/user/addresses/default")
    Call<Address> getDefaultAddress(@Header("Authorization") String token);
    
    @POST("api/user/addresses")
    Call<Address> createAddress(@Header("Authorization") String token, @Body AddressRequest request);
    
    @PUT("api/user/addresses/{id}")
    Call<Address> updateAddress(@Header("Authorization") String token, @Path("id") Long id, @Body AddressRequest request);
    
    @DELETE("api/user/addresses/{id}")
    Call<Void> deleteAddress(@Header("Authorization") String token, @Path("id") Long id);
    
    @PUT("api/user/addresses/{id}/set-default")
    Call<Address> setDefaultAddress(@Header("Authorization") String token, @Path("id") Long id);
}
