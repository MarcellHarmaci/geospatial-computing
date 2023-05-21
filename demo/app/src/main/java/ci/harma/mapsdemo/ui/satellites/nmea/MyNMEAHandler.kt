package ci.harma.mapsdemo.ui.satellites.nmea

import android.location.Location
import android.util.Log
import com.github.petr_s.nmea.GpsSatellite
import com.github.petr_s.nmea.NMEAHandler

class MyNMEAHandler(
    private val locationListener: LocationListener? = null,
    private val satellitesListener: SatellitesListener? = null
) : NMEAHandler {
    interface LocationListener {
        fun onLocation(location: Location?)
    }

    interface SatellitesListener {
        fun onSatellites(satellites: MutableList<GpsSatellite>?)
    }


    override fun onLocation(location: Location?) {
        locationListener?.onLocation(location)
    }

    override fun onSatellites(satellites: MutableList<GpsSatellite>?) {
        satellitesListener?.onSatellites(satellites)
    }

    override fun onUnrecognized(sentence: String?) {
        Log.i("NMEA-unrecognised", sentence ?: "empty")
    }

    override fun onBadChecksum(expected: Int, actual: Int) {
        Log.i("NMEA-badCheckSum", "expected: $expected =/= $actual :actual")
    }

    override fun onException(e: Exception?) {
        Log.i("NMEA-exception", e?.message ?: "empty message")
        e?.printStackTrace()
    }

    override fun onStart() {
        Log.i("NMEA-start", "parsing started")
    }

    override fun onFinish() {
        Log.i("NMEA-finish", "parsing finished")
    }
}