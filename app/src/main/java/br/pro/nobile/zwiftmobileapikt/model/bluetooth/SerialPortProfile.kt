package br.pro.nobile.zwiftmobileapikt.model.bluetooth

import java.util.*

// Serviço de Bluetooth convencional
class SerialPortProfile {
    object Service {
        /* Serial Port Profile (SPP)
           NOTE: The example SDP record in SPP v1.0 does not include a BluetoothProfileDescriptorList attribute, but some implementations may also use this UUID for the Profile Identifier. */
        val SERIAL_PORT_SERVICE: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }
}