//package com.nihaocloud.sesamedisk.database.table
//
//import androidx.room.Entity
//import androidx.room.PrimaryKey
//import com.nihaocloud.sesamedisk.data.SeafItem
//import com.nihaocloud.sesamedisk.util.Utils
//import java.io.File
//
//@Entity
//data class NihaoCachedFile(
//    @PrimaryKey val id: Int,
//    val fileID: String,
//    val repoName: String,
//    val repoID: String,
//    val path: String,
//    val accountSignature: String,
//    val fileOriginalSize: Long,
//    val relativePath: String? = null
//) : SeafItem {
//
//    val file : File
//
//    get() {
//
//    }
//
//
//    override fun getTitle(): String {
//        return path.substring(path.lastIndexOf('/') + 1)
//    }
//
//    override fun getSubtitle(): String {
//        return Utils.readableFileSize(file.length())
//    }
//
//    override fun getIcon(): Int {
//        return Utils.getFileIcon(file.name)
//    }
//
//}
