package com.nihaocloud.sesamedisk.httputils

import android.os.Build
import android.os.Build.MODEL
import android.os.Build.VERSION.RELEASE
import androidx.lifecycle.LiveData
import com.nihaocloud.sesamedisk.BuildConfig
import com.nihaocloud.sesamedisk.BuildConfig.VERSION_NAME
import com.nihaocloud.sesamedisk.account.Account
import com.nihaocloud.sesamedisk.account.AccountInfo
import com.nihaocloud.sesamedisk.api.ApiResponse
import com.nihaocloud.sesamedisk.data.SeafRepo
import com.nihaocloud.sesamedisk.data.ServerInfo
import com.nihaocloud.sesamedisk.model.AuthToken
import com.nihaocloud.sesamedisk.util.PLATFORM
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    companion object {
        //    both are... https://app.nihaoconsult.com and https://app.nihaocloud.com
        //
        //    official is the one you sent https://app.nihaoconsult.com/
        //https://www.nihaocloud.com/   for china prod
        //https://app.nihaoconsult.com  fro prod
        // https://www.test.nihaocloud.com/  for test
        const val AUTH_TOKEN = "api2/auth-token/"
        const val ACCOUNT_INFO = "api2/account/info/"
        const val SERVER_INFO = "api2/server-info/"
        const val REPOS = "api2/repos/"
        const val ACTIVITIES = "api/v2.1/activities/"
        const val EVENT = "api2/events/"
        const val REPO_HISTORY_CHANGE = "api2/repo_history_changes/%s/"
        const val STARRED_ITEM = "api/v2.1/starred-items/"
    }

    @POST("api2/auth-token/")
    fun authToken(
        @Header("X-Seafile-OTP") authToken: String? = null,
        @Header("X-SEAFILE-S2FA") sessionKey: String? = null,
        @Header("X-SEAFILE-2FA-TRUST-DEVICE") rememberDevice: Int,
        @Part("username") email: RequestBody,
        @Part("password") password: RequestBody,
        @Part("device_id") deviceId: RequestBody, // String deviceId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
        @Part("client_version") clientVersion: RequestBody = VERSION_NAME.toRequestBody(VERSION_NAME.toMediaType()),
        @Part("platform") platform: RequestBody = PLATFORM.toRequestBody(PLATFORM.toMediaType()),
        @Part("device_name") device_name: RequestBody = MODEL.toRequestBody(MODEL.toMediaType()),
        @Part("platform_version") platformVersion: RequestBody = RELEASE.toRequestBody(RELEASE.toMediaType()),
    ): LiveData<ApiResponse<AuthToken>>
//  .header("Authorization", "Token " + account.token);

    @GET("api2/account/info/")
    fun  accountInfo() : LiveData<ApiResponse<AccountInfo>>

    @GET("api2/server-info/")
    fun  serverInfo() : LiveData<ApiResponse<ServerInfo>>

    @GET("api2/repos/")
    fun  repos() : LiveData<ApiResponse<List<SeafRepo>>>


}