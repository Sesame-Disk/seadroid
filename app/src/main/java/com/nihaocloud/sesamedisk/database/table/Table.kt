package com.nihaocloud.sesamedisk.database.table

import android.accounts.Account
import android.os.Parcelable
import androidx.room.*
import com.nihaocloud.sesamedisk.BuildConfig
import com.nihaocloud.sesamedisk.util.SPACE_USAGE_SEPERATOR
import com.nihaocloud.sesamedisk.util.assembleUserName
import com.nihaocloud.sesamedisk.util.readableFileSize
import com.nihaocloud.sesamedisk.util.stripSlashes
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

//@Entity(
//    tableName = "course_learner_text_table",
//    primaryKeys = ["learnerTextId"],
//    indices = [Index("courseId"), Index("learnerTextId")],
//    foreignKeys = [ForeignKey(
//        entity = CourseEntity::class,
//        parentColumns = ["courseId"],
//        childColumns = ["courseId"],
//        onUpdate = ForeignKey.CASCADE,
//        onDelete = ForeignKey.CASCADE,
//        deferred = false
//    )]
//)

@Parcelize
@Entity(
    tableName = "nihao_account",
    primaryKeys = ["email"],
    indices = [Index("email")]
)
data class NihaoAccount(
    val email: String,
    val usage: Long,
    val total: Long,
    val server: String,
    val name: String,
    val isShib: Boolean,
    val token: String,
    val sessionKey: String,
    val avatarUrl: String,
) : Parcelable {

    @Ignore
    @IgnoredOnParcel
    val spaceUsed =
        "${usage.readableFileSize()}${String.SPACE_USAGE_SEPERATOR} ${total.readableFileSize()}"

    @Ignore
    @IgnoredOnParcel
    val serverHost = server.let {
        val substring = it.substring(server.indexOf("://") + 3)
        substring.substring(0, substring.indexOf('/'))
    }

    @Ignore
    @IgnoredOnParcel
    val serverDomainName =
        if (serverHost.contains(":")) serverHost.substring(0, serverHost.indexOf(':'))
        else serverHost

    @Ignore
    @IgnoredOnParcel
    val serverNoProtocol = server.let {
        var result = it.substring(it.indexOf("://") + 3)
        if (result.endsWith("/")) result = result.substring(0, result.length - 1)
        result
    }

    @Ignore
    @IgnoredOnParcel
    val isHttp = server.startsWith("https")

    @Ignore
    @IgnoredOnParcel
    val signature = String.format("%s (%s)", serverNoProtocol, email)

    @Ignore
    @IgnoredOnParcel
    val displayName = assembleUserName(name, email, server.stripSlashes())

    @Ignore
    @IgnoredOnParcel
    val androidAccount = Account(signature, BuildConfig.ACCOUNT_TYPE)
}

@Entity(
    tableName = "repo_cache_dir",
    primaryKeys = ["repoId"],
    indices = [Index("repoId")]
)
data class RepoCacheDir(
    val repoId: String,
    val repoDir: String,
    val account: String
)

@Entity(
    tableName = "encryption_key",
    primaryKeys = ["repoId"],
    indices = [Index("repoId")]
)
data class EncryptionKey(
    val repoId: String,
    val encKey: String,
    val encIv: String
)

@Entity(
    tableName = "starred_file_cache",
    primaryKeys = ["id"],
    indices = [Index("id")]
)
data class StarredFileCache(
    val id: Int,
    val account: String,
    val content: String
)

@Entity(
    tableName = "file_cache",
    primaryKeys = ["repoId"],
    indices = [Index("repoId")]
)
data class FileCache(
    val repoId: String,
    val fileId: String,
    val path: String,
    val relativePath: String?,
    val repoName: String,
    val account: String
)

@Entity(
    tableName = "dirents_cache",
    primaryKeys = ["dirId"],
    indices = [Index("dirId")]
)
data class DirentsCache(
    val dirId: String,
    val fileId: String,
    val path: String,
)

@Entity(
    tableName = "photo_cache",
    primaryKeys = ["id"],
    indices = [Index("id")]
)
data class PhotoCache(
    val id: Int,
    val file: String,
    val dateAdded: Long
)

@Entity(
    tableName = "library",
    primaryKeys = ["id"],
    indices = [Index("id")],
    foreignKeys = [ForeignKey(
        entity = NihaoAccount::class,
        parentColumns = ["email"],
        childColumns = ["ownerContactEmail"],
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE,
        deferred = false
    )]
)
data class Library(
    val id: String,
    val name: String,
    val owner: String,
    val ownerName: String,
    val ownerContactEmail: String,
    val modifierEmail: String,
    val modifierContactEmail: String,
    val modifierName: String,
    val mtimeRelative: String,
    val type: LibraryType,
    val mtime: Long,
    val size: Long,
    val sizeFormatted: String,
    val encrypted: Boolean,
    val permission: String,
    val virtual: Boolean,
    val root: String,
    val headCommitId: String,
    val version: Int,
    val salt: String,
) {
    @Ignore
    val isGroupRepo: Boolean = LibraryType.GROUP == type

    @Ignore
    val isPersonalRepo: Boolean = LibraryType.REPO == type

    @Ignore
    val isSharedRepo: Boolean = LibraryType.SHARE_REPO == type
}

enum class LibraryType(item: String) {
    GROUP("grepo"),
    REPO("repo"),
    SHARE_REPO("srepo"),
}

@Entity(
    tableName = "encrypted_library_info",
    primaryKeys = ["id"],
    indices = [Index("id")]
)
data class EncryptedLibraryInfo(
    val id: String,
    val magic: String,
    val permission: String,
    val encrypted: Boolean,
    val encVersion: Int,
    val mtime: Int,
    val owner: String,
    val modifierContactEmail: String,
    val modifierName: String,
    val size: Int,
    val modifierEmail: String,
    val name: String,
    val root: String,
    val salt: String,
    val fileCount: Int,
    val randomKey: String,
    val type: String
)

@Entity(
    tableName = "certificate",
    primaryKeys = ["url"],
    indices = [Index("url")]
)
data class Certificate(
    val url: String,
    val cert: String,
    val email: String,
    val server: String
)

@Entity(
    tableName = "auto_update_info",
    primaryKeys = ["repoId"],
    indices = [Index("repoId")]
)

data class AutoUpdateInfo(
    val account: String,
    val repoId: String,
    val repoName: String,
    val parentDir: String,
    val relativePath: String?,
    val localPath: String,
    val version: String,
)

@Entity(
    tableName = "upload_file",
    primaryKeys = ["uploadFileId"],
    indices = [Index(
        value = ["uploadFileId"],
        orders = [Index.Order.ASC]
    )],
)
data class UploadFile(
    val uploadFileId: Long = System.nanoTime(),
    val accountId: Int,
    val repoId: String,
    val repoName: String,
    val targetDir: String,
    val relativePath: String?,
    // android save uri  uri.toString , uir.parse(localFilePath), in KKM see below link
    // https://google.github.io/modernstorage/storage/
    val localFilePath: String,
    val fileUploadType: FileUploadType,
    val isCopyToLocal: Boolean,
    val state: State = State.QUEUED,
    val startTime: Long = System.nanoTime(),
    val updateTime: Long = System.nanoTime(),

    val totalBlock: Int = 0,
    val totalBlockUpload: Int = 0,
    val processTryCount: Int = 0,
    val uploadTryCount: Int = 0,
    val blockFolderPath: String = "",
    val commitUrl: String = "",
    val rawBlockUrl: String = "",
    val fileSourceLocation: FileSourceLocation = FileSourceLocation.CACHE,
    val failureReason: FailureReason = FailureReason.FAILURE_REASON_NONE
) {

    enum class FileUploadType(type: Int) {
        NEW(0),
        REPLACE(1)
    }

    enum class State {
        QUEUED, BLOCK_PROCESSING, BLOCK_PROCESS_FAIL, BLOCK_PROCESS_SUCCESS,
        BLOCK_PROCESS_STOPPED, BLOCK_PROCESS_RESTARTING, BLOCK_PROCESS_PAUSE, BLOCK_PROCESS__RESUME,
        BLOCK_URL_INIT, BLOCK_URL_SUCCESS, BLOCK_URL_FAIL,
        UPLOADING, UPLOAD_SUCCESS, UPLOAD_FAIL, UPLOAD_STOPPED, UPLOAD_RESTARTING, UPLOAD_PAUSE, UPLOAD_RESUME,
        COMMIT_ULT_INIT, COMMIT_URL_SUCCESS, COMMIT_URL_FAIL,
        MERGE_BLOCK_INIT, MERGE_BLOCK_FAIL,
        MERGE_BLOCK_SUCCESS
    }

    enum class FileSourceLocation { CACHE, ORIGINAL }
    enum class FailureReason { FILE_NOT_FOUND, FOLDER_NOT_FOUND, NO_INTERNET_CONNECTION, UNKNOWN, FAILURE_REASON_NONE }
}


@Entity(
    tableName = "upload_file_block",
    primaryKeys = ["id"],
    indices = [Index(
        value = ["id", "uploadFileId"],
        orders = [Index.Order.ASC, Index.Order.ASC]
    )],
    foreignKeys = [ForeignKey(
        entity = UploadFile::class,
        parentColumns = ["uploadFileId"],
        childColumns = ["uploadFileId"],
        onUpdate = ForeignKey.NO_ACTION,
        onDelete = ForeignKey.CASCADE,
        deferred = false
    )]
)

data class UploadFileBlock(
    val id: Long = System.nanoTime(),
    val blockId: String,
    val index: Int,
    val path: String,
    val uploadComplete: Boolean = false,
    val uploadFileId: Long
)