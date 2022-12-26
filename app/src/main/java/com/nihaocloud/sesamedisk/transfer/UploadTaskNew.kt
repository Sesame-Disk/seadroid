package com.nihaocloud.sesamedisk.transfer

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import com.nihaocloud.sesamedisk.NihaoApplication
import com.nihaocloud.sesamedisk.R
import com.nihaocloud.sesamedisk.SeafException
import com.nihaocloud.sesamedisk.SettingsManager
import com.nihaocloud.sesamedisk.data.BlockInfoBean
import com.nihaocloud.sesamedisk.data.DataManager
import com.nihaocloud.sesamedisk.data.ProgressMonitor
import com.nihaocloud.sesamedisk.data.UploadFolder
import com.nihaocloud.sesamedisk.database.table.UploadFile
import com.nihaocloud.sesamedisk.database.table.UploadFileBlock
import com.nihaocloud.sesamedisk.frag.Block
import com.nihaocloud.sesamedisk.frag.ByteSize
import com.nihaocloud.sesamedisk.frag.FileFragmenter
import com.nihaocloud.sesamedisk.model.file.UploadRequest
import com.nihaocloud.sesamedisk.util.Utils
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.security.NoSuchAlgorithmException
import java.util.*

class UploadTaskNew(
    val context: Context,
    taskID: Int,
    private val uploadRequest: UploadRequest,
    private val uploadStateListener: UploadStateListener
) : TransferTask(
    taskID, uploadRequest.account, uploadRequest.repoName, uploadRequest.repoId,
    uploadRequest.relativePath, uploadRequest.localFilePath
) {
    private val dataManager = DataManager(account)
    private val database = NihaoApplication.database
    private val uploadFileDao = database.uploadFileDao()
    private val uploadFileBlockDao = database.uploadFileBlockDao()
    private val fileSystem = NihaoApplication.fileSystem

    public override fun getTaskInfo(): UploadTaskInfo {
        val isUpdate = uploadRequest.fileUploadType === UploadFile.FileUploadType.REPLACE
        return UploadTaskInfo(
            account, taskID, state, repoID,
            repoName, uploadRequest.targetDir, relativePath, path, isUpdate, true,
            finished, totalSize, err
        )
    }

    fun cancelUpload() {
        if (state != TaskState.INIT && state != TaskState.TRANSFERRING) {
            return
        }
        state = TaskState.CANCELLED
        super.cancel(true)
    }

    override fun onPreExecute() {
        state = TaskState.PREPARING
    }

    @Deprecated("Deprecated in Java")
    override fun onProgressUpdate(vararg values: Long?) {
        finished = values[0]!!
        uploadStateListener.onFileUploadProgress(taskID)
    }

    override fun doInBackground(vararg params: Void): File? {
        state = TaskState.PREPARING
        try {
            val process = UploadFile(
                accountId = uploadRequest.account.accountId,
                repoId = uploadRequest.repoId,
                repoName = uploadRequest.repoName,
                targetDir = uploadRequest.targetDir,
                relativePath = uploadRequest.relativePath,
                localFilePath = uploadRequest.localFilePath,
                fileUploadType = uploadRequest.fileUploadType,
                isCopyToLocal = true,
                state = UploadFile.State.BLOCK_PROCESSING,
                startTime = System.currentTimeMillis(),
                updateTime = System.currentTimeMillis(),
                fileSourceLocation = UploadFile.FileSourceLocation.CACHE,
                failureReason = UploadFile.FailureReason.FAILURE_REASON_NONE
            )
            uploadFileDao.insert(process)
            val uri = Uri.parse(process.localFilePath)

            val documentFile = DocumentFile.fromSingleUri(context, uri)

            val length = (documentFile?.length() ?: 0).toString()

            val fName = documentFile?.name ?: ""
            val name: String = fName.substring(0, fName.lastIndexOf("."))
            val path = Utils.pathJoin(process.targetDir, name, "block") + "/"
            val localRepoFile = dataManager.getLocalRepoFile(process.repoName, process.repoId, path)

            if (localRepoFile.exists()) {
                localRepoFile.listFiles().forEach { it.delete() }
            } else {
                localRepoFile.mkdirs()
            }

            val size = (ByteSize.MB.bytes() * 2).toLong()
            val block = FileFragmenter.fragmentFileDynamically(
                context,
                uri,
                size,
                localRepoFile.absolutePath,
                ""
            )

            val uploadFileBlock = block.map {
                UploadFileBlock(
                    blockId = it.id,
                    index = it.index,
                    path = it.fullPath,
                    uploadComplete = false,
                    uploadFileId = process.uploadFileId
                )
            }
            uploadFileBlockDao.insert(uploadFileBlock)
            uploadFileDao.updateTotalBlock(process.uploadFileId, uploadFileBlock.size)
            uploadFileDao.updateState(process.uploadFileId, UploadFile.State.BLOCK_PROCESS_SUCCESS)
            uploadFileDao.updateState(process.uploadFileId, UploadFile.State.BLOCK_URL_INIT)

            val ids = uploadFileBlock.map { it.blockId }
            val json = dataManager.sc.getBlockUploadLink(process.repoId, ids)
            val infoBean = BlockInfoBean.fromJson(json)
            uploadFileDao.updateBlockUrl(
                process.uploadFileId,
                infoBean.commiturl,
                infoBean.rawblksurl
            )
            uploadFileDao.updateState(process.uploadFileId, UploadFile.State.BLOCK_URL_SUCCESS)
            uploadFileDao.updateState(process.uploadFileId, UploadFile.State.UPLOADING)
            state = TaskState.TRANSFERRING

            var count = 0

            val monitor: ProgressMonitor = object : ProgressMonitor {
                override fun onProgressNotify(uploaded: Long, updateTotal: Boolean) {

                    publishProgress(uploaded)
                }

                override fun isCancelled(): Boolean {
                    return this@UploadTaskNew.isCancelled()
                }
            }


            for (block in uploadFileBlock) {
                try {
                    dataManager.sc.uploadBlocks(
                        infoBean.rawblksurl,
                        fName,
                        block,
                        monitor,
                        infoBean.blkIds.get(0)
                    )
                    uploadFileBlockDao.updateState(block.id, true)
                    count++

                } catch (e: Exception) {

                }
            }

            if (count == uploadFileBlock.size) {
                val update = process.fileUploadType == UploadFile.FileUploadType.REPLACE
                dataManager.sc.commitUpload(
                    infoBean.commiturl,
                    ids,
                    process.targetDir,
                    process.relativePath,
                    fName,
                    length,
                    update
                )
            }

        } catch (e: SeafException) {
            state = TaskState.FAILED
            err = e
        } catch (e: Exception) {
            state = TaskState.FAILED
            err = SeafException(100, e.message)
        }
        return null
    }

    override fun onPostExecute(file: File?) {
        state = if (err == null) TaskState.FINISHED else TaskState.FAILED
        if (uploadStateListener != null) {
            if (err == null) {
                SettingsManager.instance().saveUploadCompletedTime(Utils.getSyncCompletedTime())
                uploadStateListener.onFileUploaded(taskID)
            } else {
                if (err.code == HTTP_ABOVE_QUOTA) {
                    Toast.makeText(
                        NihaoApplication.getAppContext(),
                        R.string.above_quota,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                uploadStateListener.onFileUploadFailed(taskID)
            }
        }
    }

    override fun onCancelled() {
        uploadStateListener?.onFileUploadCancelled(taskID)
    }

    companion object {
        const val DEBUG_TAG = "UploadTaskNew"
        const val HTTP_ABOVE_QUOTA = 443
    }
}