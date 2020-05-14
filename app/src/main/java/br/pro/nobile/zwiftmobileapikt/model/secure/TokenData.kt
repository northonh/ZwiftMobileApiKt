package br.pro.nobile.zwiftmobileapikt.model.secure

import com.google.gson.annotations.SerializedName

data class TokenData(
    @SerializedName("access_token")
    var accessToken: String? = null,
    @SerializedName("expires_in")
    var expiresIn: Int? = null,
    @SerializedName("refresh_expires_in")
    var refreshExpiresIn: Int? = null,
    @SerializedName("refresh_token")
    var refreshToken: String? = null,
    @SerializedName("token_type")
    var tokenType: String? = null,
    @SerializedName("not-before-policy")
    var notBeforePolicy: Int? = null,
    @SerializedName("session_state")
    var sessionState: String? = null
)