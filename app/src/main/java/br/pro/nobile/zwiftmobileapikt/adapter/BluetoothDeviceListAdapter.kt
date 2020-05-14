package br.pro.nobile.zwiftmobileapikt.adapter

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import br.pro.nobile.zwiftmobileapikt.R

class BluetoothDeviceListAdapter(
    context: Context,
    private val layout: Int,
    deviceList: MutableList<BluetoothDevice>
): ArrayAdapter<BluetoothDevice>(context, layout, deviceList) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView?: LayoutInflater.from(context).inflate(layout, parent, false).apply {
            val viewHolder = ViewHolder(
                this.findViewById(R.id.bluetoothDeviceNameTv),
                this.findViewById(R.id.bluetoothDeviceAddressTv)
            )
            this.tag = viewHolder
        }

        (view?.tag as ViewHolder).btDeviceNameTv.text = getItem(position)?.name?:context.getString(
                    R.string.unknown
        )
        (view.tag as ViewHolder).btDeviceAddressTv.text = getItem(position)?.address?:context.getString(
            R.string.unknown)

        return view
    }

    private data class ViewHolder(val btDeviceNameTv: TextView, val btDeviceAddressTv: TextView)
}