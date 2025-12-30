package com.oddlemon.artly.data.service

import com.oddlemon.artly.data.request.RegisterTokenRequest
import com.oddlemon.artly.data.response.RegisterTokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ArtlyService {
    @POST("api/notification/registerToken")
    suspend fun registerToken(
        @Body request: RegisterTokenRequest
    ): Response<RegisterTokenResponse>
}