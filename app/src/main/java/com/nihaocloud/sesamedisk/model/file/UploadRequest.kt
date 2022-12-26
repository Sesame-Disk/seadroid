package com.nihaocloud.sesamedisk.model.file

import android.os.Parcelable
import com.nihaocloud.sesamedisk.account.Account
import com.nihaocloud.sesamedisk.database.table.UploadFile
import kotlinx.parcelize.Parcelize

@Parcelize
data class UploadRequest(
    val account: Account,
    val repoId: String,
    val repoName: String,
    val targetDir: String,
    val relativePath: String?,
    val localFilePath: String,
    val isCopyToLocal: Boolean,
    val isEncrypted: Boolean,
    val fileUploadType: UploadFile.FileUploadType
) : Parcelable
