package com.example.mob_app;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("predict")
    Call<PredictionsResponse> predict(@Body PredictionsRequest request);
}