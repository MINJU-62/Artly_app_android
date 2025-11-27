package com.oddlemon.artly.data.request

data class RegisterTokenRequest(
    val fcm_token: String,
    val user_id: Int
)