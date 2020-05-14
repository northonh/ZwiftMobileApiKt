package br.pro.nobile.zwiftmobileapikt.model.communication

import android.util.Log
import androidx.lifecycle.MutableLiveData
import br.pro.nobile.zwiftmobileapikt.model.communication.protocol.ZwiftMessages
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.protobuf.ProtoConverterFactory
import java.io.IOException

/* Class to handling requests */
class RequestZwiftService {
    companion object{
        const val BEARER_PREFIX = "Bearer"
    }
    private val protoBufRequestZwiftApi = with (Retrofit.Builder()) {
        baseUrl(RequestZwiftApi.BASE_URL)
        addConverterFactory(ProtoConverterFactory.create())
        build()
    }.create(RequestZwiftApi::class.java)

    private val jsonRequestZwiftApi = with (Retrofit.Builder()) {
        baseUrl(RequestZwiftApi.BASE_URL)
        addConverterFactory(GsonConverterFactory.create())
        build()
    }.create(RequestZwiftApi::class.java)

    fun callSyncGetPlayers(
        worldId: Int,
        accessToken: String
    ): WorldResponse? {
        var worldResponse: WorldResponse? = null
        try {
            val response = jsonRequestZwiftApi.getPlayers(
                worldId,
                "$BEARER_PREFIX $accessToken"
            ).execute()
            worldResponse = if (response.isSuccessful) response.body() else null
        } catch (ioe: IOException) {
            Log.e(this.javaClass.name, "callGetPlayers: IOException")
        }
        return worldResponse
    }

    fun callAsyncGetPlayers(
        worldId: Int,
        accessToken: String
    ): MutableLiveData<WorldResponse?> {
        val worldResponse: MutableLiveData<WorldResponse?> = MutableLiveData()
        try {
            jsonRequestZwiftApi.getPlayers(
                worldId,
                "$BEARER_PREFIX $accessToken"
            ).enqueue(
                object: Callback<WorldResponse?> {
                    override fun onFailure(call: Call<WorldResponse?>, t: Throwable) {
                        worldResponse.value = null
                    }

                    override fun onResponse(
                        call: Call<WorldResponse?>,
                        response: Response<WorldResponse?>
                    ) {
                        worldResponse.value = response.body()
                    }
                }
            )
        } catch (ioe: IOException) {
            Log.e(this.javaClass.name, "callGetPlayers: IOException")
        }
        return worldResponse
    }

    fun callSyncGetPlayerState(
        worldId: Int,
        playerId: Int,
        accessToken: String
    ): ZwiftMessages.PlayerState? {
        var playerStateResponse: ZwiftMessages.PlayerState? = null
        try {
            val response = protoBufRequestZwiftApi.getPlayerState(
                worldId,
                playerId,
                "$BEARER_PREFIX $accessToken"
            ).execute()
            playerStateResponse = if (response.isSuccessful) response.body() else null
        }
        catch (ioe: IOException) {
            Log.e(this.javaClass.name, "callGetPlayerState: IOException")
        }
        return playerStateResponse
    }

    fun callAsyncGetPlayerState(
        worldId: Int,
        playerId: Int,
        accessToken: String
    ): MutableLiveData<ZwiftMessages.PlayerState?> {
        val playerStateResponse: MutableLiveData<ZwiftMessages.PlayerState?> = MutableLiveData()
        try {
            protoBufRequestZwiftApi.getPlayerState(
                worldId,
                playerId,
                "$BEARER_PREFIX $accessToken"
            ).enqueue(
                object : Callback<ZwiftMessages.PlayerState> {
                    override fun onFailure(call: Call<ZwiftMessages.PlayerState>, t: Throwable) {
                        playerStateResponse.value = null
                    }

                    override fun onResponse(
                        call: Call<ZwiftMessages.PlayerState>,
                        response: Response<ZwiftMessages.PlayerState>
                    ) {
                        if (response.body() != null){
                            playerStateResponse.value = response.body()
                        }
                        else {
                            playerStateResponse.value = ZwiftMessages.PlayerState.getDefaultInstance()
                            Log.i(this.javaClass.name, "callGetPlayerState: User is not zwifting")
                        }
                    }
                }
            )
        }
        catch (ioe: IOException) {
            Log.e(this.javaClass.name, "callGetPlayerState: IOException")
        }
        return playerStateResponse
    }
}