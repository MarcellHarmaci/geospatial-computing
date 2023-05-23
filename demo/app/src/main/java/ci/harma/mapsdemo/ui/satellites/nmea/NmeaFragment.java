package ci.harma.mapsdemo.ui.satellites.nmea;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.Context.LOCATION_SERVICE;

import android.content.Context;
import android.location.LocationManager;
import android.location.OnNmeaMessageListener;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.RequiresPermission;

import ci.harma.mapsdemo.GoogleMapsFragment;

/**
 * This is an ugly solution to an ugly problem
 * in Kotlin type inference and function override resolution.
 */
public abstract class NmeaFragment extends GoogleMapsFragment {

	/**
	 * Force compiler to use new non-deprecated api for adding NMEA listener
	 * @param listener OnNmeaMessageListener and NOT GpsStatus.NmeaListener which is a no-op
	 */
	@RequiresPermission(ACCESS_FINE_LOCATION)
	protected void addNmeaMessageListener(OnNmeaMessageListener listener) {
		LocationManager locationManager = getLocationManager();
		if (locationManager == null) return;

		locationManager.addNmeaListener(listener, new Handler(Looper.myLooper()));
	}

	protected void removeNmeaMessageListener(OnNmeaMessageListener listener) {
		LocationManager locationManager = getLocationManager();
		if (locationManager == null) return;

		locationManager.removeNmeaListener(listener);
	}

	private LocationManager getLocationManager() {
		Context context = getContext();
		if (context == null) return null;

		return (LocationManager) context.getSystemService(LOCATION_SERVICE);
	}
}
