package br.pro.nobile.zwiftmobileapikt.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import br.pro.nobile.zwiftmobileapikt.model.SettingsService

class SettingsViewModel(context: Context): ViewModel(){
    private val settingsModel = SettingsService(context)

    fun getMaxSpeedStageOff() = settingsModel.readMaxSpeedStageOff()
    fun getMaxSpeedStageOne() = settingsModel.readMaxSpeedStageOne()
    fun getMaxSpeedStageTwo() = settingsModel.readMaxSpeedStageTwo()

    fun setMaxSpeedStageOff(value: Float) = settingsModel.writeMaxSpeedStageOff(value)
    fun setMaxSpeedStageOne(value: Float) = settingsModel.writeMaxSpeedStageOne(value)
    fun setMaxSpeedStageTwo(value: Float) = settingsModel.writeMaxSpeedStageTwo(value)
}