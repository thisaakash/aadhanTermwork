package com.aakash.aadhan.api;

import com.aakash.aadhan.model.NotificationRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface NotificationApi {
    @POST("notify")
    Call<Void> sendNotification(@Body NotificationRequest request);
}
