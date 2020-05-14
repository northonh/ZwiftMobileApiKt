package br.pro.nobile.zwiftmobileapikt.zwift

class Client(username: String, password: String) {
    private val authToken: AuthToken = AuthToken(username, password)

    fun getSyncWorld(worldId: Int = 1): World = World(worldId, authToken.getSyncAccessToken()!!)
}