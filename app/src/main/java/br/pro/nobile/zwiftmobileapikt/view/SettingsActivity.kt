package br.pro.nobile.zwiftmobileapikt.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import br.pro.nobile.zwiftmobileapikt.R
import br.pro.nobile.zwiftmobileapikt.helper.SpeedRoundHelper
import br.pro.nobile.zwiftmobileapikt.model.SettingsService
import br.pro.nobile.zwiftmobileapikt.viewmodel.SettingsViewModel
import kotlinx.android.synthetic.main.activity_settings.*
import splitties.toast.toast

class SettingsActivity : AppCompatActivity() {
    private lateinit var settingsViewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        supportActionBar?.subtitle = getString(R.string.fan_bounds)
        
        settingsViewModel = SettingsViewModel(this)
    }

    override fun onResume() {
        super.onResume()
        
        maxSpeedStageOffEt.setText(SpeedRoundHelper.roundToString(settingsViewModel.getMaxSpeedStageOff()))
        maxSpeedStageOneEt.setText(SpeedRoundHelper.roundToString(settingsViewModel.getMaxSpeedStageOne()))
        maxSpeedStageTwoEt.setText(SpeedRoundHelper.roundToString(settingsViewModel.getMaxSpeedStageTwo()))
    }
    
    fun onClick(view: View) {
        if (view == saveBt) {
            if (validateFields()) {
                settingsViewModel.setMaxSpeedStageOff(SpeedRoundHelper.roundToFloat(maxSpeedStageOffEt.text.toString()))
                settingsViewModel.setMaxSpeedStageOne(SpeedRoundHelper.roundToFloat(maxSpeedStageOneEt.text.toString()))
                settingsViewModel.setMaxSpeedStageTwo(SpeedRoundHelper.roundToFloat(maxSpeedStageTwoEt.text.toString()))

                toast(getString(R.string.settings_saved_successfully))
                finish()
            }
        }
    }
    
    private fun validateFields(): Boolean {
        if (
            maxSpeedStageOffEt.text.isEmpty()
            || maxSpeedStageOffEt.text.toString().toFloat() <= SettingsService.Defaults.MIN_SPEED
            || maxSpeedStageOffEt.text.toString().toFloat() > maxSpeedStageOneEt.text.toString().toFloat()
        ) {
            maxSpeedStageOffEt.requestFocus()
            toast(getString(R.string.invalid_max_speed_to_stage_off))
            return false
        }

        if (
            maxSpeedStageOneEt.text.isEmpty()
            || maxSpeedStageOneEt.text.toString().toFloat() <= maxSpeedStageOffEt.text.toString().toFloat()
            || maxSpeedStageOneEt.text.toString().toFloat() > maxSpeedStageTwoEt.text.toString().toFloat()
        ) {
            maxSpeedStageOneEt.requestFocus()
            toast(getString(R.string.invalid_max_speed_to_stage_one))
            return false
        }

        if (
            maxSpeedStageTwoEt.text.isEmpty()
            || maxSpeedStageTwoEt.text.toString().toFloat() <= maxSpeedStageOneEt.text.toString().toFloat()
            || maxSpeedStageTwoEt.text.toString().toFloat() > SettingsService.Defaults.MAX_SPEED
        ) {
            maxSpeedStageTwoEt.requestFocus()
            toast(getString(R.string.invalid_max_speed_to_stage_two))
            return false
        }

        return true
    }
}
