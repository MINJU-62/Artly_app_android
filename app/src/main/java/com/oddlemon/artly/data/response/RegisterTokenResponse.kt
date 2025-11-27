package com.oddlemon.artly.data.response

data class RegisterTokenResponse(
    val `data`: TokenData,
    val message: String,
    val status: String
)

data class TokenData(
    val fcm_token: String,
    val id: Int,
    val update_dtm: String,
    val user_id: Int
)