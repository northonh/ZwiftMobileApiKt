package br.pro.nobile.zwiftmobileapikt.view

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import br.pro.nobile.zwiftmobileapikt.R
import br.pro.nobile.zwiftmobileapikt.model.ZwiftUpdaterService
import br.pro.nobile.zwiftmobileapikt.model.ZwiftUpdaterService.Constants.ACTION_VALIDATE_RESULT_ZWIFT_CREDENTIALS
import br.pro.nobile.zwiftmobileapikt.model.ZwiftUpdaterService.Constants.ACTION_VALIDATE_ZWIFT_CREDENTIALS
import br.pro.nobile.zwiftmobileapikt.model.ZwiftUpdaterService.Constants.EXTRA_VALIDATE_RESULT_ZWIFT_CREDENTIALS
import br.pro.nobile.zwiftmobileapikt.view.ZwiftCredentialsActivity.Extras.EXTRA_ZWIFT_PASSWORD
import br.pro.nobile.zwiftmobileapikt.view.ZwiftCredentialsActivity.Extras.EXTRA_ZWIFT_USERNAME
import kotlinx.android.synthetic.main.activity_zwift_credentials.*
import splitties.toast.toast

class ZwiftCredentialsActivity : AppCompatActivity() {
    object Extras {
        const val EXTRA_ZWIFT_USERNAME  = "EXTRA_ZWIFT_USERNAME"
        const val EXTRA_ZWIFT_PASSWORD  = "EXTRA_ZWIFT_PASSWORD"
    }

    private val actionsReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_VALIDATE_RESULT_ZWIFT_CREDENTIALS) {
                val validCredentials = intent.getBooleanExtra(EXTRA_VALIDATE_RESULT_ZWIFT_CREDENTIALS, false)
                if (validCredentials) {
                    toast(getString(R.string.valid_credentials))

                    val intentToMainActivity = Intent(this@ZwiftCredentialsActivity, MainActivity::class.java)
                    intentToMainActivity.putExtra(EXTRA_ZWIFT_USERNAME, zwiftUsernameEt.text.toString())
                    intentToMainActivity.putExtra(EXTRA_ZWIFT_PASSWORD, zwiftPasswordEt.text.toString())
                    startActivity(intentToMainActivity)

                    finish()
                }
                else {
                    toast(getString(R.string.invalid_credentials))
                    setVisibilityViewWhenValidatingCredentias(inProgress = false)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_zwift_credentials)

        supportActionBar?.subtitle = getString(R.string.zwift_credentials)

        // Se o serviço atualização de informações vindas do Zwift não estiver executando, executa.
        startService(Intent(this, ZwiftUpdaterService::class.java))
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(
            actionsReceiver,
            IntentFilter().apply {
                addAction(ACTION_VALIDATE_RESULT_ZWIFT_CREDENTIALS)
            }
        )
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(actionsReceiver)
    }

    fun onClick(view: View) {
        if (view == checkCredentialsIb) {
            // Lança Action de validação de credenciais para o serviço de atualização de informações
            // do Zwift
            if (isViewsFilledBeforeValidate()) {
                setVisibilityViewWhenValidatingCredentias(inProgress = true)
                sendBroadcast(
                    Intent(ACTION_VALIDATE_ZWIFT_CREDENTIALS).apply {
                        putExtra(EXTRA_ZWIFT_USERNAME, zwiftUsernameEt.text.toString())
                        putExtra(EXTRA_ZWIFT_PASSWORD, zwiftPasswordEt.text.toString())
                    }
                )
            }
        }
    }

    private fun isViewsFilledBeforeValidate(): Boolean {
        if (zwiftUsernameEt.text.toString().isEmpty() || zwiftUsernameEt.text.toString().isBlank()){
            zwiftUsernameEt.requestFocus()
            toast(getString(R.string.zwift_username_wrong_filled))
            return false
        }

        if (zwiftPasswordEt.text.toString().isEmpty() || zwiftPasswordEt.text.toString().isBlank()){
            zwiftPasswordEt.requestFocus()
            toast(getString(R.string.zwift_password_wrong_filled))
            return false
        }

        return true
    }

    private fun setVisibilityViewWhenValidatingCredentias(inProgress: Boolean) {
        if (inProgress) {
            checkCredentialsIb.visibility = GONE
            checkCredentialsPb.visibility = VISIBLE
        }
        else {
            checkCredentialsIb.visibility = VISIBLE
            checkCredentialsPb.visibility = GONE
        }
        zwiftUsernameEt.isEnabled = !inProgress
        zwiftPasswordEt.isEnabled = !inProgress
    }
}
