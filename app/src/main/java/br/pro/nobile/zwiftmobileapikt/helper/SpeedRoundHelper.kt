package br.pro.nobile.zwiftmobileapikt.helper

import java.util.*

object SpeedRoundHelper {
    fun roundToString(speedFloat: Float): String {
        return "%.1f".format(Locale.US, speedFloat)
    }
    fun roundToFloat(speedString: String): Float {
        return roundToString(speedString.toFloat()).toFloat()
    }
}