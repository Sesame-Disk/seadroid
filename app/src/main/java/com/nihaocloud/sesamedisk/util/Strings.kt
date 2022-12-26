package com.nihaocloud.sesamedisk.util

import android.text.TextUtils
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

val PLATFORM = "android"


val String.Companion.BACK_SLASH: String
    get() = "/"

val String.Companion.SPACE_USAGE_SEPERATOR: String
    get() = " / "

val String.Companion.EMPTY: String
    get() = ""

val String.Companion.NEW_LINE: String
    get() = "\n"

val String.Companion.CLONE: String
    get() = ":"

val String.Companion.SPACE: String
    get() = " "

val String.Companion.COMMA: String
    get() = ", "

val String.BEARER_TOKEN: String
    get() = "Bearer $this"

fun isNotEmptyOrNull(data: String?): Boolean = !data.isNullOrEmpty()

fun String?.isValidEmailAddress(): Boolean =
    !this.isNullOrEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun Int.toPercentageValue(value: Int): Int {
    if (this == 0) return 0
    if (value == 0) return 0
    return this.times(100).div(value)
}

fun String.stripSlashes(): String {
    return this.replace("^[/]*|[/]*$".toRegex(), "")
}

fun assembleUserName(name: String, email: String, server: String): String {
    var serverUrl = server
    if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(server)) return ""
    // strip port, like :8000 in 192.168.1.116:8000
    if (server.indexOf(":") != -1) serverUrl = server.substring(0, server.indexOf(':'))
    // String info = String.format("%s (%s)", email, server);//settingFragmeng set account name
    var info = String.format("%s (%s)", name, serverUrl)
    info = info.replace("[^\\w\\d\\.@\\(\\) ]".toRegex(), "_")
    return info
}


fun Long.readableFileSize() =
    if (this <= 0) "0 KB"
    else {
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(this.toDouble()) / log10(1000.0)).toInt()
        DecimalFormat("#,##0.#").format(this / 1000.0.pow(digitGroups.toDouble())) + " " + units[digitGroups]
    }
