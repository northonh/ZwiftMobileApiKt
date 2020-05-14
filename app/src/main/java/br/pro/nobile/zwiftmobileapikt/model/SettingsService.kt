package br.pro.nobile.zwiftmobileapikt.model

import android.content.Context
import android.content.SharedPreferences
import br.pro.nobile.zwiftmobileapikt.model.SettingsService.Defaults.MAX_SPEED_STAGE_OFF
import br.pro.nobile.zwiftmobileapikt.model.SettingsService.Defaults.MAX_SPEED_STAGE_ONE
import br.pro.nobile.zwiftmobileapikt.model.SettingsService.Defaults.MAX_SPEED_STAGE_TWO
import br.pro.nobile.zwiftmobileapikt.model.SettingsService.Fields.MAX_SPEED_STAGE_OFF_FIELD
import br.pro.nobile.zwiftmobileapikt.model.SettingsService.Fields.MAX_SPEED_STAGE_ONE_FIELD
import br.pro.nobile.zwiftmobileapikt.model.SettingsService.Fields.MAX_SPEED_STAGE_TWO_FIELD

class SettingsService(context: Context) {
    object Defaults {
        // Limites de velocidade
        const val MIN_SPEED = 0f
        const val MAX_SPEED_STAGE_OFF = 10f
        const val MAX_SPEED_STAGE_ONE = 20f
        const val MAX_SPEED_STAGE_TWO = 30f
        const val MAX_SPEED = 150f
    }

    object Fields {
        // Nome dos campos no arquivo de shared preferences
        const val MAX_SPEED_STAGE_OFF_FIELD = "max_speed_stage_off"
        const val MAX_SPEED_STAGE_ONE_FIELD = "max_speed_stage_one"
        const val MAX_SPEED_STAGE_TWO_FIELD = "max_speed_stage_two"
    }

    private val sharedPreferencesFilename = "settings"
    private val settingsSharedPrefs: SharedPreferences = context.getSharedPreferences(sharedPreferencesFilename, Context.MODE_PRIVATE)

    fun readMaxSpeedStageOff() = settingsSharedPrefs.getFloat(MAX_SPEED_STAGE_OFF_FIELD, MAX_SPEED_STAGE_OFF)
    fun readMaxSpeedStageOne() = settingsSharedPrefs.getFloat(MAX_SPEED_STAGE_ONE_FIELD, MAX_SPEED_STAGE_ONE)
    fun readMaxSpeedStageTwo() = settingsSharedPrefs.getFloat(MAX_SPEED_STAGE_TWO_FIELD, MAX_SPEED_STAGE_TWO)

    fun writeMaxSpeedStageOff(value: Float) = writeSpeedToSharedPreferences(MAX_SPEED_STAGE_OFF_FIELD, value)
    fun writeMaxSpeedStageOne(value: Float) = writeSpeedToSharedPreferences(MAX_SPEED_STAGE_ONE_FIELD, value)
    fun writeMaxSpeedStageTwo(value: Float) = writeSpeedToSharedPreferences(MAX_SPEED_STAGE_TWO_FIELD, value)

    private fun writeSpeedToSharedPreferences(name: String, value: Float) {
        val editor = settingsSharedPrefs.edit()
        editor.putFloat(name, value)
        editor.apply()
    }
}