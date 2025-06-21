package com.example.student;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface GeminiApiService {
    @POST("models/gemini-2.0-flash:generateContent")
    Call<GeminiResponse> getResponse(@Body GeminiRequest request); // Removed @Header apiKey parameter
}