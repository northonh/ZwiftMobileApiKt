package br.pro.nobile.zwiftmobileapikt.model.bluetooth

import android.app.Service
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.util.Log
import br.pro.nobile.zwiftmobileapikt.model.bluetooth.SerialPortProfile.Service.SERIAL_PORT_SERVICE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.DataOutputStream
import java.io.IOException

class SmartFanBluetoothService: Service() {
    companion object Constants {
        val ACTION_SEND_STAGE_COMMAND = "ACTION_SEND_STAGE_COMMAND"
        val EXTRA_STAGE = "EXTRA_STAGE"

        val ACTION_UPDATE_CONNECTED_STATUS = "ACTION_UPDATE_CONNECTED_STATUS"
        val EXTRA_CONNNECTED_STATUS = "EXTRA_CONNNECTED_STATUS"
    }

    private lateinit var btDevice: BluetoothDevice
    private lateinit var btSocket: BluetoothSocket
    private lateinit var btOutputStream: DataOutputStream

    private val sendStageCommandReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_SEND_STAGE_COMMAND) {
                val stage = intent.getIntExtra(EXTRA_STAGE, 0)
                sendStageCommand(stage)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
            openBluetoothConnection()
        }

        registerReceiver(
            sendStageCommandReceiver,
            IntentFilter().apply {
                addAction(ACTION_SEND_STAGE_COMMAND)
            }
        )

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun sendStageCommand(stage: Int) {
        val stageCommand = "S${stage}"

        Log.d(this.javaClass.name, "stageCommand: $stageCommand")

        /* Envia comando para dispositivo Bluetooth */
        GlobalScope.launch (Dispatchers.IO) {
            try {
                openBluetoothConnection()
                btOutputStream.write(stageCommand.toByteArray())
                btOutputStream.flush()

                sendBroadcast(
                    Intent(ACTION_UPDATE_CONNECTED_STATUS).apply { putExtra(EXTRA_CONNNECTED_STATUS, true) }
                )
            }
            catch (ioe: IOException) {
                ioe.printStackTrace()
                closeBluetoothConnection()
            }
        }
    }

    private fun openBluetoothConnection() {
        if (!::btSocket.isInitialized) {
            btSocket = btDevice.createInsecureRfcommSocketToServiceRecord(SERIAL_PORT_SERVICE)
        }
        try {
            if (!btSocket.isConnected) {
                btSocket.connect()
                btOutputStream = DataOutputStream(btSocket.outputStream)
            }
        }
        catch (ioe: IOException) {
            ioe.printStackTrace()
            closeBluetoothConnection()
        }
    }

    private fun closeBluetoothConnection() {
        try {
            if (::btOutputStream.isInitialized) {
                btOutputStream.close()
            }
            if (::btSocket.isInitialized) {
                btSocket.close()
            }
        }
        catch(ioe: IOException) {
            ioe.printStackTrace()
        }

        sendBroadcast(
            Intent(ACTION_UPDATE_CONNECTED_STATUS).apply { putExtra(EXTRA_CONNNECTED_STATUS, false) }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(this.javaClass.name, "Encerrando serviço.")
        closeBluetoothConnection()
        unregisterReceiver(sendStageCommandReceiver)
    }
}