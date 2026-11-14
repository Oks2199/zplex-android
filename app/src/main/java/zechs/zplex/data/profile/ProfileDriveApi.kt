package zechs.zplex.data.profile

import androidx.annotation.Keep
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

@Keep
internal data class ProfileDriveUser(val permissionId: String)

@Keep
internal data class ProfileDriveAbout(val user: ProfileDriveUser)

@Keep
internal data class ProfileDriveFile(val id: String, val name: String, val version: String)

@Keep
internal data class ProfileDriveFiles(val files: List<ProfileDriveFile>, val nextPageToken: String? = null)

@Keep
internal data class ProfileDriveIds(val ids: List<String>)

internal interface ProfileDriveApi {
    @GET("drive/v3/about?fields=user(permissionId)")
    suspend fun account(@Header("Authorization") token: String): ProfileDriveAbout

    @GET("drive/v3/files?spaces=appDataFolder&fields=nextPageToken,files(id,name,version)&pageSize=100")
    suspend fun files(
        @Header("Authorization") token: String,
        @Query("q") query: String,
        @Query("pageToken") pageToken: String?
    ): ProfileDriveFiles

    @GET("drive/v3/files/generateIds?space=appDataFolder&count=1")
    suspend fun generateId(@Header("Authorization") token: String): ProfileDriveIds

    @GET("drive/v3/files/{id}?alt=media")
    suspend fun read(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): ProfileDocument

    @POST("upload/drive/v3/files?uploadType=multipart&fields=id,name,version")
    suspend fun create(@Header("Authorization") token: String, @Body body: RequestBody): ProfileDriveFile

    @PATCH("upload/drive/v3/files/{id}?uploadType=media&fields=id,name,version")
    suspend fun update(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body body: RequestBody
    ): ProfileDriveFile
}
