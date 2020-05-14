package br.pro.nobile.zwiftmobileapikt.zwift

import androidx.lifecycle.MutableLiveData
import br.pro.nobile.zwiftmobileapikt.model.communication.RequestZwiftService
import br.pro.nobile.zwiftmobileapikt.model.communication.WorldResponse
import br.pro.nobile.zwiftmobileapikt.model.communication.protocol.ZwiftMessages

class World(private val worldId: Int, private val accessToken: String) {
    private val requestZwiftService: RequestZwiftService = RequestZwiftService()

    fun getSyncPlayers(): WorldResponse? {
        return requestZwiftService.callSyncGetPlayers(worldId, accessToken)
    }

    fun getAsyncPlayers(): MutableLiveData<WorldResponse?> {
        return requestZwiftService.callAsyncGetPlayers(worldId, accessToken)
    }

    fun getSyncPlayerState(playerId: Int): ZwiftMessages.PlayerState? {
        return requestZwiftService.callSyncGetPlayerState(worldId, playerId, accessToken)
    }

    fun getAsyncPlayerState(playerId: Int): MutableLiveData<ZwiftMessages.PlayerState?> {
        return requestZwiftService.callAsyncGetPlayerState(worldId, playerId, accessToken)
    }
}