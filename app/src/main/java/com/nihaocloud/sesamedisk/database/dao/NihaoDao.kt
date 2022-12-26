package com.nihaocloud.sesamedisk.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.nihaocloud.sesamedisk.database.table.*

@Dao
abstract class NihaoAccountDao : BaseDao<NihaoAccount> {

//    @Query("SELECT * FROM nihao_account WHERE email = :email")
//    abstract fun findByUserLoginId(email: String): LiveData<PwdUser>
//
//    fun getPwdUserDistinctLiveData(): LiveData<PwdUser> =
//        getPwdUserLiveData().distinctUntilChanged()
//
//    @Query("SELECT * FROM pwd_user_table LIMIT 1")
//    protected abstract fun getPwdUserLiveData(): LiveData<PwdUser>
//
//    @Query("SELECT * FROM pwd_user_table LIMIT 1")
//    abstract fun getPwdUser(): PwdUser
//
//    @Query("DELETE FROM pwd_user_table")
//    abstract fun clear()
//
//    @Query("UPDATE pwd_user_table SET  userProfilePicUrl=:userProfilePicUrl WHERE email=:email")
//    abstract fun updateProfilePicture(email: String, userProfilePicUrl: String)

}

@Dao
abstract class RepoCacheDirDao : BaseDao<RepoCacheDir> {

}

@Dao
abstract class EncryptionKeyDao : BaseDao<EncryptionKey> {
}

@Dao
abstract class StarredFileCacheDao : BaseDao<StarredFileCache> {
}

@Dao
abstract class FileCacheDao : BaseDao<FileCache> {
}

@Dao
abstract class DirentsCacheDao : BaseDao<DirentsCache> {
}

@Dao
abstract class PhotoCacheDao : BaseDao<PhotoCache> {
}

@Dao
abstract class CertificateDao : BaseDao<Certificate> {
}

@Dao
abstract class UploadFileDao : BaseDao<UploadFile> {

    @Query("UPDATE upload_file SET  commitUrl =:commitUrl, rawBlockUrl=:rawBlockUrl WHERE uploadFileId=:uploadFileId")
    abstract fun updateBlockUrl(uploadFileId: Long, commitUrl: String, rawBlockUrl: String): Int

    @Query("UPDATE upload_file SET  state =:state WHERE uploadFileId=:uploadFileId")
    abstract fun updateState(uploadFileId: Long, state: UploadFile.State): Int

    @Query("UPDATE upload_file SET  totalBlock =:totalBlock WHERE uploadFileId=:uploadFileId")
    abstract fun updateTotalBlock(uploadFileId: Long, totalBlock: Int): Int

}


@Dao
abstract class UploadFileBlockDao : BaseDao<UploadFileBlock> {
    @Query("UPDATE upload_file_block SET  uploadComplete =:uploadComplete WHERE blockId=:blockId")
    abstract fun updateState(blockId: Long, uploadComplete: Boolean): Int
}
