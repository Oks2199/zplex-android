package zechs.zplex.data.repository

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import zechs.zplex.data.model.drive.DriveFile
import zechs.zplex.data.model.drive.ShortcutDetails
import zechs.zplex.utils.state.Resource
import java.io.FileNotFoundException
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Read-only access to a folder selected with Android's Storage Access Framework.
 * Google Drive exposes its files through this framework, so Movynex does not need
 * a Google OAuth client or broad access to the user's Drive account.
 */
@Singleton
class DocumentTreeRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun listChildren(folderUri: String): Resource<List<DriveFile>> =
        withContext(Dispatchers.IO) {
            try {
                val treeUri = Uri.parse(folderUri)
                require(treeUri.scheme == "content") { "The selected folder is no longer valid" }

                val parentDocumentId = if (DocumentsContract.isDocumentUri(context, treeUri)) {
                    DocumentsContract.getDocumentId(treeUri)
                } else {
                    DocumentsContract.getTreeDocumentId(treeUri)
                }
                val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
                    treeUri,
                    parentDocumentId
                )

                val projection = arrayOf(
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                    DocumentsContract.Document.COLUMN_MIME_TYPE,
                    DocumentsContract.Document.COLUMN_SIZE,
                    DocumentsContract.Document.COLUMN_LAST_MODIFIED
                )

                val files = mutableListOf<DriveFile>()
                context.contentResolver.query(
                    childrenUri,
                    projection,
                    null,
                    null,
                    null
                )?.use { cursor ->
                    val idColumn = cursor.getColumnIndexOrThrow(
                        DocumentsContract.Document.COLUMN_DOCUMENT_ID
                    )
                    val nameColumn = cursor.getColumnIndexOrThrow(
                        DocumentsContract.Document.COLUMN_DISPLAY_NAME
                    )
                    val mimeColumn = cursor.getColumnIndexOrThrow(
                        DocumentsContract.Document.COLUMN_MIME_TYPE
                    )
                    val sizeColumn = cursor.getColumnIndex(
                        DocumentsContract.Document.COLUMN_SIZE
                    )
                    val modifiedColumn = cursor.getColumnIndex(
                        DocumentsContract.Document.COLUMN_LAST_MODIFIED
                    )

                    while (cursor.moveToNext()) {
                        val documentId = cursor.getString(idColumn)
                        val documentUri = DocumentsContract.buildDocumentUriUsingTree(
                            treeUri,
                            documentId
                        )
                        files += DriveFile(
                            id = documentUri.toString(),
                            name = cursor.getString(nameColumn),
                            size = cursor.getNullableLong(sizeColumn),
                            mimeType = cursor.getString(mimeColumn),
                            iconLink = null,
                            modifiedTime = cursor.getNullableLong(modifiedColumn),
                            shortcutDetails = ShortcutDetails()
                        )
                    }
                } ?: throw FileNotFoundException("Unable to open the selected folder")

                Resource.Success(files)
            } catch (exception: Exception) {
                Resource.Error(exception.message ?: "Unable to read the selected folder")
            }
        }

    fun openInputStream(fileUri: String): InputStream {
        val uri = Uri.parse(fileUri)
        require(uri.scheme == "content") { "The media file is no longer valid" }
        return context.contentResolver.openInputStream(uri)
            ?: throw FileNotFoundException("Unable to open the media file")
    }

    fun hasPersistedReadAccess(uriString: String?): Boolean {
        if (!isDocumentUri(uriString)) return false
        val uri = Uri.parse(uriString)
        return context.contentResolver.persistedUriPermissions.any { permission ->
            permission.isReadPermission && permission.uri == uri
        }
    }

    companion object {
        fun isDocumentUri(value: String?): Boolean =
            !value.isNullOrBlank() && Uri.parse(value).scheme == "content"
    }
}

private fun android.database.Cursor.getNullableLong(columnIndex: Int): Long? =
    if (columnIndex < 0 || isNull(columnIndex)) null else getLong(columnIndex)
