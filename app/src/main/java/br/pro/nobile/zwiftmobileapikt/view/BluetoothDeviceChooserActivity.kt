package br.pro.nobile.zwiftmobileapikt.view

import android.Manifest.permission.*
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.*
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.ACTION_FOUND
import android.bluetooth.BluetoothDevice.EXTRA_DEVICE
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import br.pro.nobile.zwiftmobileapikt.R
import br.pro.nobile.zwiftmobileapikt.adapter.BluetoothDeviceListAdapter
import br.pro.nobile.zwiftmobileapikt.model.bluetooth.SmartFanBluetoothService
import br.pro.nobile.zwiftmobileapikt.view.BluetoothDeviceChooserActivity.Constants.ENABLE_BLUETOOTH_ADAPTER_REQUEST_CODE
import br.pro.nobile.zwiftmobileapikt.view.BluetoothDeviceChooserActivity.Constants.GRANT_BLUETOOTH_PERMISSIONS_REQUEST_CODE
import br.pro.nobile.zwiftmobileapikt.view.BluetoothDeviceChooserActivity.Constants.PP_SMART_FAN_DEVICE_NAME
import kotlinx.android.synthetic.main.activity_bluetooth_device_chooser.*
import splitties.toast.toast
import java.util.*

class BluetoothDeviceChooserActivity : AppCompatActivity() {
    private object Constants {
        const val ENABLE_BLUETOOTH_ADAPTER_REQUEST_CODE    = 0
        const val GRANT_BLUETOOTH_PERMISSIONS_REQUEST_CODE = 1
        const val PP_SMART_FAN_DEVICE_NAME = "PP-SMART-FAN"
    }

    private val btAdapter: BluetoothAdapter = getDefaultAdapter()
    private lateinit var btDeviceListAdapter: BluetoothDeviceListAdapter
    private lateinit var btDeviceList: MutableList<BluetoothDevice>
    private lateinit var btActionsBroadcastReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth_device_chooser)

        supportActionBar?.subtitle = getString(R.string.choose_smart_fan)

        btDeviceList = mutableListOf()
        btDeviceListAdapter = BluetoothDeviceListAdapter(
            this,
            R.layout.bluetooth_device_list_item_layout,
            btDeviceList
        )
        btDevicesLv.adapter = btDeviceListAdapter

        btDevicesLv.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, _, position, _ ->
            // Inicia o serviço de comunicação com o fan
            val smartFanBluetoothServiceIntent = Intent(this, SmartFanBluetoothService::class.java)
            smartFanBluetoothServiceIntent.putExtra(EXTRA_DEVICE, btDeviceList[position])
            startService(smartFanBluetoothServiceIntent)

            // Abre a activity de credenciais Zwift
            val zwiftCredentialsActivityIntent = Intent(this, ZwiftCredentialsActivity::class.java)
            startActivity(zwiftCredentialsActivityIntent)

            finish()

            true
        }
    }

    override fun onResume() {
        super.onResume()

        // Verificando permissões
        val noGrantedArray = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P)
            arrayOf(BLUETOOTH, BLUETOOTH_ADMIN, ACCESS_FINE_LOCATION).filter {
                checkSelfPermission(it) != PERMISSION_GRANTED
            }
        else
            arrayOf(BLUETOOTH, BLUETOOTH_ADMIN, ACCESS_COARSE_LOCATION).filter {
                checkSelfPermission(it) != PERMISSION_GRANTED
            }

        if (noGrantedArray.isNotEmpty()) {
            requestPermissions(noGrantedArray.toTypedArray(), GRANT_BLUETOOTH_PERMISSIONS_REQUEST_CODE)
        }

        if (!btAdapter.isEnabled) {
            val enableBtAdapterIntent = Intent(ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtAdapterIntent, ENABLE_BLUETOOTH_ADAPTER_REQUEST_CODE)
        }

        btActionsBroadcastReceiver = object: BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    ACTION_FOUND -> {
                        val btDevice =
                            intent.getParcelableExtra<BluetoothDevice>(EXTRA_DEVICE)
                        btDevice?.let {
                            if (it.name.toUpperCase(Locale.US) == PP_SMART_FAN_DEVICE_NAME) {
                                btDeviceList.takeIf { !btDeviceList.contains(btDevice) }?.add(btDevice)
                            }
                        }
                    }
                    ACTION_DISCOVERY_FINISHED -> {
                        btDeviceListAdapter.notifyDataSetChanged()
                        discoveryInProgress(toggleOn = false)
                        toast(getString(R.string.discovery_finished))
                    }
                    ACTION_STATE_CHANGED -> {
                        val btState = intent.getIntExtra(EXTRA_STATE, STATE_DISCONNECTED)
                        if (btState == STATE_TURNING_OFF || btState == STATE_OFF) {
                            toast(getString(R.string.bluetooth_adapter_enabled_required))
                            finish()
                        }
                    }
                }
            }
        }
        registerReceiver(btActionsBroadcastReceiver, IntentFilter().apply{
            addAction(ACTION_FOUND)
            addAction(ACTION_DISCOVERY_FINISHED)
            addAction(ACTION_STATE_CHANGED)
        })

        // Preenchendo primeiramente com os dispositivos já pareados, caso contrário, inicia a busca
        if (btDeviceList.isEmpty()) {
            if (!btAdapter.bondedDevices.isNullOrEmpty()) {
                btDeviceListAdapter.clear()
                btDeviceList.addAll(btAdapter.bondedDevices.filter {
                    it.name.toUpperCase(Locale.US) == PP_SMART_FAN_DEVICE_NAME
                })
                btDeviceListAdapter.notifyDataSetChanged()
                discoveryInProgress(toggleOn = false)
            }
            else {
                btDeviceList.clear()
                btAdapter.startDiscovery()
                discoveryInProgress(toggleOn = true)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == GRANT_BLUETOOTH_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.any { it != PERMISSION_GRANTED }) {
                toast(getString(R.string.bluetooth_permissions_required))
                finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ENABLE_BLUETOOTH_ADAPTER_REQUEST_CODE && resultCode != Activity.RESULT_OK) {
            toast(getString(R.string.bluetooth_adapter_enabled_required))
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(btActionsBroadcastReceiver)
        btAdapter.cancelDiscovery()
        discoveryInProgress(toggleOn = false)
    }

    private fun discoveryInProgress(toggleOn: Boolean) {
        if (toggleOn) {
            btDeviceSearchPb.visibility = VISIBLE
            btDevicesLv.visibility = GONE
        }
        else {
            btDeviceSearchPb.visibility = GONE
            btDevicesLv.visibility = VISIBLE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_bluetooth_device_chooser_activity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.refreshBtDevicesMi -> {
                btDeviceList.clear()
                btAdapter.startDiscovery()
                discoveryInProgress(toggleOn = true)
            }
            R.id.exitMi -> {
                btAdapter.cancelDiscovery()
                discoveryInProgress(toggleOn = false)
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}