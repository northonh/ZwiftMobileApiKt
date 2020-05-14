package br.pro.nobile.zwiftmobileapikt.model.playerid

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

// Busca o playerId a partir do nome de usuário e senha
// Foi utilizado como base o código de https://www.virtualonlinecycling.com/p/zwiftid.html
interface PlayerIdApi {
    companion object {
        const val BASE_URL = "https://z00pbp8lig.execute-api.us-west-1.amazonaws.com/"
    }

    @GET("latest/zwiftId")
    fun getPlayerId(
        @Query(value = "username", encoded = true) username: String,
        @Query(value = "pw", encoded = true) password: String
    ): Call<Int>
}
