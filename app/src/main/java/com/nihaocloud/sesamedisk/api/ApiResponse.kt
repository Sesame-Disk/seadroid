package com.nihaocloud.sesamedisk.api

import com.squareup.moshi.Moshi
import retrofit2.Response
import timber.log.Timber
import java.io.IOException

/**
 * Common class used by API responses.
 * @param <T> the type of the response object
</T> */
sealed class ApiResponse<T> {
    companion object {
        fun <T> create(throwable: Throwable): ApiErrorResponse<T> {
            Timber.e("Throwable [${throwable.message}]")
            return ApiErrorResponse(Exception(throwable))
        }

        fun <T> create(response: Response<T>): ApiResponse<T> {
            return when {
                response.isSuccessful -> {
                    val body = response.body()
                    if (body == null || response.code() == 204)
                        ApiErrorResponse(NoSuchElementException("Can't fetch data from server"))
                    else ApiSuccessResponse(body)
                }
                else -> {
                    val msg = response.errorBody()?.string()
                    val errorMsg = if (msg.isNullOrEmpty()) {
                        response.message()
                    } else {
                        try {
                            val moshi = Moshi.Builder().build()
                            val adapter = moshi.adapter(ApiError::class.java)
                            val apiError = adapter.fromJson(msg)
                            when {
                                apiError != null && apiError.message.isNotEmpty() -> apiError.message
                                else -> msg
                            }
                        } catch (exception: IOException) {
                            msg
                        }
                    }
                    ApiErrorResponse(RuntimeException(errorMsg))
                }
            }
        }
    }
}

data class ApiSuccessResponse<T>(val body: T) : ApiResponse<T>()
data class ApiErrorResponse<T>(val exception: Exception) : ApiResponse<T>()