package br.pro.nobile.zwiftmobileapikt.model.secure

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface SecureZwiftApi {
    companion object {
        const val BASE_URL = "https://secure.zwift.com/auth/realms/zwift/"
    }

    @FormUrlEncoded
    @POST("tokens/access/codes/")
    fun getTokenData(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("grant_type") grantType: String,
        @Field("client_id") clientId: String
    ): Call<TokenData>

    @FormUrlEncoded
    @POST("tokens/access/codes/")
    fun refreshTokenData(
        @Field("access_token") accessToken: String,
        @Field("grant_type") grantType: String,
        @Field("client_id") clientId: String
    ): Call<TokenData>
}