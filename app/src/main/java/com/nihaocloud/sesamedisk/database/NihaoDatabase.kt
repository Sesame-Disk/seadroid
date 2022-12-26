package com.nihaocloud.sesamedisk.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.nihaocloud.sesamedisk.database.dao.*
import com.nihaocloud.sesamedisk.database.table.*
import java.util.concurrent.Executors

@Database(
    entities = [
        NihaoAccount::class,
        RepoCacheDir::class,
        EncryptionKey::class,
        StarredFileCache::class,
        FileCache::class,
        DirentsCache::class,
        PhotoCache::class,
        AutoUpdateInfo::class,
        Certificate::class,
        UploadFile::class,
        UploadFileBlock::class,

],
version = 1,
exportSchema = false
)
abstract class NihaoDatabase : RoomDatabase() {
    abstract fun nihaoAccountDao(): NihaoAccountDao
    abstract fun repoCacheDirDao(): RepoCacheDirDao
    abstract fun encryptionKeyDao(): EncryptionKeyDao
    abstract fun starredFileCacheDao(): StarredFileCacheDao
    abstract fun fileCacheDao(): FileCacheDao
    abstract fun direntsCacheDao(): DirentsCacheDao
    abstract fun photoCacheDao(): PhotoCacheDao
    abstract fun certificateDao(): CertificateDao
    abstract fun uploadFileDao(): UploadFileDao
    abstract fun uploadFileBlockDao(): UploadFileBlockDao

    companion object {
        @Volatile
        private var INSTANCE: NihaoDatabase? = null

        fun getInstance(context: Context): NihaoDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                NihaoDatabase::class.java, "NihaoDatabase.db"
            )
                // prepopulate the database after onCreate was called
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // insert the data on the IO Thread
                        ioThread {
                            // getInstance(context).dataDao().insertData(PREPOPULATE_DATA)
                        }
                    }
                })
                .build()
        // val PREPOPULATE_DATA = listOf(Data("1", "val"), Data("2", "val 2"))
    }
}

private val IO_EXECUTOR = Executors.newSingleThreadExecutor()

/**
 * Utility method to run blocks on a dedicated background thread, used for io/database work.
 */
fun ioThread(f: () -> Unit) {
    IO_EXECUTOR.execute(f)
}
