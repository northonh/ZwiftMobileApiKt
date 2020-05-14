package br.pro.nobile.zwiftmobileapikt.model.communication

data class WorldResponse(
    val worldId: Int,
    val name: String,
    val playerCount: Int,
    val currentWorldTime: Long,
    val currentDateTime: Int,
    val friendsInWorld: List<FriendsInWorld>
)