package com.powerletter.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface PowerLetterApi {

    @GET("health")
    suspend fun healthCheck(): Response<Map<String, String>>

    @POST("generateLetter")
    suspend fun generateLetter(
        @Body request: GenerateLetterRequest
    ): Response<GenerateLetterResponse>
}
