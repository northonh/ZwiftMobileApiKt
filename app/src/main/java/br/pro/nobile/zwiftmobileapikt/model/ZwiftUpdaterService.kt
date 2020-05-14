package br.pro.nobile.zwiftmobileapikt.model

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.util.Log
import br.pro.nobile.zwiftmobileapikt.model.ZwiftUpdaterService.Constants.ACTION_UPDATE_PLAYER_SPEED
import br.pro.nobile.zwiftmobileapikt.model.ZwiftUpdaterService.Constants.ACTION_VALIDATE_RESULT_ZWIFT_CREDENTIALS
import br.pro.nobile.zwiftmobileapikt.model.ZwiftUpdaterService.Constants.ACTION_VALIDATE_ZWIFT_CREDENTIALS
import br.pro.nobile.zwiftmobileapikt.model.ZwiftUpdaterService.Constants.INVALID_PLAYER_ID
import br.pro.nobile.zwiftmobileapikt.model.ZwiftUpdaterService.Constants.EXTRA_PLAYER_SPEED
import br.pro.nobile.zwiftmobileapikt.model.ZwiftUpdaterService.Constants.EXTRA_PLAYER_SPORT
import br.pro.nobile.zwiftmobileapikt.model.ZwiftUpdaterService.Constants.UPDATE_DELAY_IN_MILISECONDS
import br.pro.nobile.zwiftmobileapikt.model.ZwiftUpdaterService.Constants.EXTRA_VALIDATE_RESULT_ZWIFT_CREDENTIALS
import br.pro.nobile.zwiftmobileapikt.model.ZwiftUpdaterService.Constants.WORLD_ID
import br.pro.nobile.zwiftmobileapikt.model.ZwiftUpdaterService.Constants.EXTRA_ZWIFT_PASSWORD
import br.pro.nobile.zwiftmobileapikt.model.ZwiftUpdaterService.Constants.EXTRA_ZWIFT_USERNAME
import br.pro.nobile.zwiftmobileapikt.model.ZwiftUpdaterService.Constants.ZWIFT_SPEED_FACTOR
import br.pro.nobile.zwiftmobileapikt.model.playerid.PlayerIdService
import br.pro.nobile.zwiftmobileapikt.zwift.AuthToken
import br.pro.nobile.zwiftmobileapikt.zwift.Client
import br.pro.nobile.zwiftmobileapikt.zwift.World
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ZwiftUpdaterService: Service() {
    object Constants {
        const val ACTION_VALIDATE_ZWIFT_CREDENTIALS = "ACTION_VALIDATE_ZWIFT_CREDENTIALS"
        const val EXTRA_ZWIFT_USERNAME  = "EXTRA_ZWIFT_USERNAME"
        const val EXTRA_ZWIFT_PASSWORD  = "EXTRA_ZWIFT_PASSWORD"
        const val ACTION_VALIDATE_RESULT_ZWIFT_CREDENTIALS = "ACTION_VALIDATE_RESULT_ZWIFT_CREDENTIALS"
        const val EXTRA_VALIDATE_RESULT_ZWIFT_CREDENTIALS = "EXTRA_VALIDATE_RESULT_ZWIFT_CREDENTIALS"

        const val ACTION_UPDATE_PLAYER_SPEED = "ACTION_UPDATE_PLAYER_SPEED"
        const val EXTRA_PLAYER_SPEED = "EXTRA_PLAYER_SPEED"
        const val EXTRA_PLAYER_SPORT = "EXTRA_PLAYER_SPORT"

        const val UPDATE_DELAY_IN_MILISECONDS = 3000L
        const val INVALID_PLAYER_ID = -1

        const val WORLD_ID = 1 // A API do Zwift ainda não faz essa verificação, então sempre 1
        const val ZWIFT_SPEED_FACTOR = 998437 // Extraído por engenharia reversa, analisando requisições enviadas
    }

    private lateinit var username: String
    private lateinit var password: String
    private var playerId: Int = INVALID_PLAYER_ID

    private val actionsReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_VALIDATE_ZWIFT_CREDENTIALS) {
                username = intent.getStringExtra(EXTRA_ZWIFT_USERNAME)?:""
                password = intent.getStringExtra(EXTRA_ZWIFT_PASSWORD)?:""

                GlobalScope.launch (Dispatchers.IO) {
                    // Usa serviço PlayerId
                    playerId = PlayerIdService().callGetPlayerId(username, password)?: INVALID_PLAYER_ID

                    // Usa serviço SecureZwiftApi
                    val authToken = AuthToken(username, password)
                    val accessToken = authToken.getSyncAccessToken()

                    // AccessToken é nulo se credenciais inválidas
                    val validateResultIntent = Intent(ACTION_VALIDATE_RESULT_ZWIFT_CREDENTIALS)
                    validateResultIntent.putExtra(EXTRA_VALIDATE_RESULT_ZWIFT_CREDENTIALS,accessToken != null)
                    sendBroadcast(validateResultIntent)

                    // Se as credenciais forem válidas, inicia o processo de atualização de velocidade
                    if (accessToken != null) {
                        startSpeedUpdateFromZwift()
                    }
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val intentFilter = IntentFilter()
        intentFilter.addAction(ACTION_VALIDATE_ZWIFT_CREDENTIALS)
        registerReceiver(actionsReceiver, intentFilter)

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        unregisterReceiver(actionsReceiver)
        Log.d(this.javaClass.name, "Encerrando serviço.")
        super.onDestroy()
    }

    private fun startSpeedUpdateFromZwift() {
        GlobalScope.launch (Dispatchers.IO) {
            if (username.isNotEmpty() && password.isNotEmpty() && playerId != INVALID_PLAYER_ID) {
                val client = Client(username, password)
                val world: World = client.getSyncWorld(WORLD_ID)

                while (true) {
                    delay(UPDATE_DELAY_IN_MILISECONDS)
                    val playerState = world.getSyncPlayerState(playerId)
                    val speed: Float = if (playerState != null)
                        (playerState.speed / ZWIFT_SPEED_FACTOR).toFloat()
                    else
                        0f
                    val sport: Long = playerState?.sport ?: 0L

                    // Lança action de atualização de velocidade
                    sendBroadcast(
                        Intent(ACTION_UPDATE_PLAYER_SPEED).apply {
                            putExtra(EXTRA_PLAYER_SPEED, speed)
                            putExtra(EXTRA_PLAYER_SPORT, sport)
                        }
                    )
                }
            }
        }
    }
}