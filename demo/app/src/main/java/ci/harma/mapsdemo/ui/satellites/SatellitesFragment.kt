package ci.harma.mapsdemo.ui.satellites

import android.Manifest
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.location.OnNmeaMessageListener
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import ci.harma.mapsdemo.databinding.FragmentSatellitesBinding
import ci.harma.mapsdemo.ui.satellites.nmea.EmptyLocationListener
import ci.harma.mapsdemo.ui.satellites.nmea.MyNMEAHandler
import com.github.petr_s.nmea.GpsSatellite
import com.github.petr_s.nmea.NMEAParser
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class SatellitesFragment : NmeaFragment() {
	private val binding get() = _binding!!
	private var _binding: FragmentSatellitesBinding? = null

	private var googleMap: GoogleMap? = null
	private lateinit var nmeaListener: OnNmeaMessageListener
	private lateinit var emptyLocationListener: EmptyLocationListener

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		val viewModel = ViewModelProvider(this)[SatellitesViewModel::class.java]

		_binding = FragmentSatellitesBinding.inflate(inflater, container, false)
		val root: View = binding.root

		mapView = binding.mapView
		mapView?.onCreate(savedInstanceState)
		mapView?.getMapAsync(this)

		return root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onResume() {
		super.onResume()
		subscribeToNmeaMessages()
	}

	override fun onPause() {
		val locationManager = context?.getSystemService(LOCATION_SERVICE) as LocationManager?
		locationManager?.removeUpdates(emptyLocationListener)

		removeNmeaMessageListener(nmeaListener)
		super.onPause()
	}

	private fun subscribeToNmeaMessages(askedOnce: Boolean = false) {
		// Permission check
		if (ActivityCompat.checkSelfPermission(
				requireContext(),
				Manifest.permission.ACCESS_FINE_LOCATION
			) != PackageManager.PERMISSION_GRANTED
		) {
			if (askedOnce) return

			permissionRequestLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
			return
		}

		// App doesn't receive NMEA messages until the Fragment is not subscribed to location updates
		val locationManager = context?.getSystemService(LOCATION_SERVICE) as LocationManager?
		emptyLocationListener = EmptyLocationListener()
		locationManager?.requestLocationUpdates(
			LocationManager.GPS_PROVIDER,
			1000L, 0F,
			emptyLocationListener,
			Looper.getMainLooper()
		)

		// Subscription
		val parser = NMEAParser(MyNMEAHandler(locationListener, satellitesListener))
		nmeaListener = OnNmeaMessageListener { message, _ -> parser.parse(message) }
		addNmeaMessageListener(nmeaListener)
	}

	private val permissionRequestLauncher =
		registerForActivityResult(
			ActivityResultContracts.RequestPermission()
		) { result ->
			if (result) {
				subscribeToNmeaMessages(true)
			} else {
				Toast.makeText(
					context,
					"Unable to operate without user permissions",
					Toast.LENGTH_SHORT
				).show()
			}
		}

	override fun onMapReady(map: GoogleMap) {
		googleMap = map

		val sydney = LatLng(-34.0, 151.0)

		map.addMarker(
			MarkerOptions()
				.position(sydney)
				.title("Marker in Sydney")
		)
		map.moveCamera(CameraUpdateFactory.newLatLng(sydney))
	}

	private val locationListener = object : MyNMEAHandler.LocationListener {
		override fun onLocation(location: Location?) {
			Log.d("NMEA-onLocation", location?.toString() ?: "")
//			TODO display user location on the map
		}
	}

	private val satellitesListener = object : MyNMEAHandler.SatellitesListener {
		override fun onSatellites(satellites: MutableList<GpsSatellite>?) {
			Log.d("NMEA-onSatellites", satellites?.toString() ?: "")
//			TODO display satellites around the map
		}
	}

}