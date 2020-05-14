package br.pro.nobile.zwiftmobileapikt.view

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import br.pro.nobile.zwiftmobileapikt.R
import br.pro.nobile.zwiftmobileapikt.helper.SpeedRoundHelper
import br.pro.nobile.zwiftmobileapikt.model.ZwiftUpdaterService
import br.pro.nobile.zwiftmobileapikt.model.ZwiftUpdaterService.Constants.ACTION_UPDATE_PLAYER_SPEED
import br.pro.nobile.zwiftmobileapikt.model.ZwiftUpdaterService.Constants.EXTRA_PLAYER_SPEED
import br.pro.nobile.zwiftmobileapikt.model.bluetooth.SmartFanBluetoothService
import br.pro.nobile.zwiftmobileapikt.model.bluetooth.SmartFanBluetoothService.Constants.ACTION_SEND_STAGE_COMMAND
import br.pro.nobile.zwiftmobileapikt.model.bluetooth.SmartFanBluetoothService.Constants.ACTION_UPDATE_CONNECTED_STATUS
import br.pro.nobile.zwiftmobileapikt.model.bluetooth.SmartFanBluetoothService.Constants.EXTRA_CONNNECTED_STATUS
import br.pro.nobile.zwiftmobileapikt.model.bluetooth.SmartFanBluetoothService.Constants.EXTRA_STAGE
import br.pro.nobile.zwiftmobileapikt.view.MainActivity.Constants.HIDE_SYSTEM_UI_DELAY
import br.pro.nobile.zwiftmobileapikt.view.MainActivity.Constants.MAX_FAN_STAGE
import br.pro.nobile.zwiftmobileapikt.view.MainActivity.Constants.MAX_PLAYER_SPEED
import br.pro.nobile.zwiftmobileapikt.view.MainActivity.Constants.MIN_FAN_STAGE
import br.pro.nobile.zwiftmobileapikt.view.MainActivity.Constants.MIN_PLAYER_SPEED
import br.pro.nobile.zwiftmobileapikt.view.MainActivity.FanStages.STAGE_OFF
import br.pro.nobile.zwiftmobileapikt.view.MainActivity.FanStages.STAGE_ONE
import br.pro.nobile.zwiftmobileapikt.view.MainActivity.FanStages.STAGE_THREE
import br.pro.nobile.zwiftmobileapikt.view.MainActivity.FanStages.STAGE_TWO
import br.pro.nobile.zwiftmobileapikt.viewmodel.SettingsViewModel
import com.hookedonplay.decoviewlib.DecoView
import com.hookedonplay.decoviewlib.charts.SeriesItem
import com.hookedonplay.decoviewlib.events.DecoEvent
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    object Constants {
        const val HIDE_SYSTEM_UI_DELAY = 3000L
        const val MIN_PLAYER_SPEED = 0f
        const val MAX_PLAYER_SPEED = 50f
        const val MIN_FAN_STAGE = 0f
        const val MAX_FAN_STAGE = 3f
    }

    object FanStages {
        const val STAGE_OFF = 0
        const val STAGE_ONE = 1
        const val STAGE_TWO = 2
        const val STAGE_THREE = 3
    }

    private lateinit var settingsViewModel: SettingsViewModel

    // Propriedades para ArcView
    private var playerSpeedSerieIndex = -1
    private var fanSpeedSerieIndex    = -1

    private val updateSpeedReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_UPDATE_PLAYER_SPEED -> {
                    // Atualiza view de velocidade do player
                    val speed = intent.getFloatExtra(EXTRA_PLAYER_SPEED,0f)
                    Log.d(this.javaClass.name, "Velocidade recebida: $speed")

                    updateValueInArcView(
                        playerSpeedVisorAv,
                        if (speed <= MAX_PLAYER_SPEED)  // Para deixar o odômetro mais dinâmico :)
                            speed
                        else
                            MAX_PLAYER_SPEED,
                        playerSpeedSerieIndex
                    )
                    playerSpeedVisorTv.text = SpeedRoundHelper.roundToString(speed)

                    // Atualiza view de velocidade do fan
                    val fanStage = calculateFanStage(speed)
                    updateValueInArcView(fanSpeedVisorAv, fanStage.toFloat(), fanSpeedSerieIndex)
                    fanSpeedVisorTv.text = fanStage.toString()

                    // Propaga estágio para serviço de comunicação bluetooth com o fan
                    val sendStageCommandIntent = Intent(ACTION_SEND_STAGE_COMMAND)
                    sendStageCommandIntent.putExtra(EXTRA_STAGE, fanStage)
                    sendBroadcast(sendStageCommandIntent)
                }
                ACTION_UPDATE_CONNECTED_STATUS -> {
                    val connected = intent.getBooleanExtra(EXTRA_CONNNECTED_STATUS, false)
                    val drawableId = if (connected) R.drawable.ic_connected else R.drawable.ic_disconnected
                    statusFanConnectionTv.setCompoundDrawablesWithIntrinsicBounds(null, null, null, getDrawable(drawableId))
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        // Agenda o desaparecimento da UI do sistema quando o usuário clicar nas bordas da janela
        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                Handler().postDelayed( { hideSystemUi()}, HIDE_SYSTEM_UI_DELAY)
            }
        }

        // ViewModel
        settingsViewModel = SettingsViewModel(this)

        // ArcView para velocidade
        playerSpeedSerieIndex = createArcView(
            playerSpeedVisorAv,
            MIN_PLAYER_SPEED,
            MAX_PLAYER_SPEED,
            MIN_PLAYER_SPEED,
            getColor(R.color.orange)
        )
        fanSpeedSerieIndex = createArcView(
            fanSpeedVisorAv,
            MIN_FAN_STAGE,
            MAX_FAN_STAGE,
            MIN_FAN_STAGE,
            getColor(R.color.colorPrimaryDark)
        )

        // Registra-se para receber Actions no onCreate para que usuário possa deixar app em bg
        registerReceiver(
            updateSpeedReceiver,
            IntentFilter().apply {
                addAction(ACTION_UPDATE_PLAYER_SPEED)
                addAction(ACTION_UPDATE_CONNECTED_STATUS)
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(updateSpeedReceiver)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.settingsMi -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.exitMi -> {
                // Finaliza serviços em execução
                stopService(Intent(this, SmartFanBluetoothService::class.java))
                stopService(Intent(this, ZwiftUpdaterService::class.java))

                finish()
                true
            }
            else -> {false}
        }
    }

    // Esconde a UI do sistema para manter a janela em tela cheia
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUi()
        }
    }

    private fun hideSystemUi() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_FULLSCREEN
                )
    }

    private fun createArcView(arcView: DecoView, minValue: Float, maxValue: Float, initialValue: Float, color: Int): Int {
        // Configurando para ser um círculo completo com início na parte de baixo
        arcView.configureAngles(360, 180)

        // trilha background
        arcView.addSeries(
            SeriesItem.Builder(Color.argb(255, 218, 218, 218))
                .setRange(minValue, maxValue, maxValue)
                .setInitialVisibility(true)
                .setLineWidth(32f)
                .build()
        )
        return arcView.addSeries(
            SeriesItem.Builder(color)
                .setRange(minValue, maxValue, initialValue)
                .setLineWidth(32f)
                .setShadowSize(30f)
                .setShadowColor(Color.DKGRAY)
                .build()
        )
    }

    private fun updateValueInArcView(arcView: DecoView, value: Float, index: Int) {
        arcView.addEvent(DecoEvent.Builder(value).setIndex(index).setDelay(0).setDuration(1000).build())
    }

    private fun calculateFanStage(speed: Float): Int {
        return when {
            speed < settingsViewModel.getMaxSpeedStageOff() -> STAGE_OFF
            speed < settingsViewModel.getMaxSpeedStageOne() -> STAGE_ONE
            speed < settingsViewModel.getMaxSpeedStageTwo() -> STAGE_TWO
            else -> STAGE_THREE
        }
    }
}