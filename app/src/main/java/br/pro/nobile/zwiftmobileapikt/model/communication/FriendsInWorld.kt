package br.pro.nobile.zwiftmobileapikt.model.communication

data class FriendsInWorld(
    val playerId: Int,
    val firstName: String,
    val lastName: String,
    val totalDistanceInMeters: Int,
    val rideDurationInSeconds: Int,
    val countryISOCode: Int,
    val playerType: String,
    val followerStatusOfLoggedInPlayer: String,
    val rideOnGiven: Boolean,
    val currentSport: String,
    val male: Boolean,
    val enrolledZwiftAcademy: Boolean,
    val mapId: Int,
    val ftp: Int,
    val runTime10kmInSeconds: Int
)