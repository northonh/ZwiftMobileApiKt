package br.pro.nobile.zwiftmobileapikt.model.playerid

import android.net.Uri
import android.util.Log
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class PlayerIdService {
    private val playerIdApi = with (Retrofit.Builder()) {
        baseUrl(PlayerIdApi.BASE_URL)
        addConverterFactory(GsonConverterFactory.create())
        build()
    }.create(PlayerIdApi::class.java)

    fun callGetPlayerId(username: String, password: String): Int? {
        try {
            Log.v("URL: ", playerIdApi.getPlayerId(
                username,
                password
            ).request().url().toString()
            )
            val response = playerIdApi.getPlayerId(
                Uri.encode(username),
                Uri.encode(password)
            ).execute()
            Log.v("CODIGO", response.code().toString())


            return if (response.isSuccessful) response.body() else null
        }
        catch (ioe: IOException) {
            Log.e(this.javaClass.name, "callGetPlayerId: IOException ${ioe.message}")
        }
        return null
    }
}