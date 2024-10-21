package com.aakash.aadhan.api;

import com.aakash.aadhan.model.LoginRequest;
import com.aakash.aadhan.model.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AadhanApi {
    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);
}
