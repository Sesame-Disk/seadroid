package com.nihaocloud.sesamedisk.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AuthToken(
    @Json(name = "token")
    val token: String
)

@JsonClass(generateAdapter = true)
data class AuthError(
    @Json(name = "non_field_errors")
    val nonFieldErrors: List<String>,
    @Json(name = "password")
    val password: List<String>,
    @Json(name = "username")
    val username: List<String>
)