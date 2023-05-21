package ci.harma.mapsdemo.ui.satellites.nmea

import android.location.Location
import android.location.LocationListener
import android.os.Bundle

class EmptyLocationListener : LocationListener {
	override fun onLocationChanged(location: Location) {
	}

	@Deprecated("Deprecated in Java")
	override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
	}

	override fun onProviderEnabled(provider: String) {
	}

	override fun onProviderDisabled(provider: String) {
	}
}