package br.pro.nobile.zwiftmobileapikt.model.communication

import br.pro.nobile.zwiftmobileapikt.model.communication.protocol.ZwiftMessages
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Path

interface RequestZwiftApi {
    companion object {
        const val BASE_URL = "https://us-or-rly101.zwift.com/"
    }

    @Headers(
        "User-Agent: Zwift/115 CFNetwork/758.0.2 Darwin/15.0.0",
        "Accept: application/json"
    )
    @GET("relay/worlds/{worldId}")
    fun getPlayers(
        @Path("worldId") worldId: Int,
        @Header("Authorization") headerAuthorizationString: String
    ): Call<WorldResponse>

    @Headers(
        "User-Agent: Zwift/115 CFNetwork/758.0.2 Darwin/15.0.0",
        "Accept: application/x-protobuf-lite"
    )
    @GET("relay/worlds/{worldId}/players/{playerId}")
    fun getPlayerState(
        @Path("worldId") worldId: Int,
        @Path("playerId") playerId: Int,
        @Header("Authorization") headerAuthorizationString: String
    ): Call<ZwiftMessages.PlayerState>
}