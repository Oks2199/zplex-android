package zechs.zplex.data.remote

import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import zechs.zplex.data.model.drive.AuthorizationResponse
import zechs.zplex.data.model.drive.RefreshTokenRequest
import zechs.zplex.data.model.drive.TokenResponse

interface TokenApi {

    @POST("/o/oauth2/token")
    suspend fun getAccessToken(
        @Body request: RefreshTokenRequest
    ): TokenResponse

    @FormUrlEncoded
    @POST("https://oauth2.googleapis.com/token")
    suspend fun getRefreshToken(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("redirect_uri") redirectUri: String,
        @Field("code") authorizationCode: String,
        @Field("code_verifier") codeVerifier: String,
        @Field("grant_type") grantType: String = "authorization_code"
    ): AuthorizationResponse

}
