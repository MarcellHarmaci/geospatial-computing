package ci.harma.mapsdemo.ui.home

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ci.harma.mapsdemo.databinding.FragmentHomeBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng

class HomeFragment : Fragment(), OnMapReadyCallback {
	private var mapView: MapView? = null
	private lateinit var googleMap: GoogleMap
	private var locationProvider: FusedLocationProviderClient? = null

	private var lastLocation: Location? = null
//	private var secondLastLocation: Location? = null
//	private val vector: Pair<Double, Double>?
//		get() {
//			return if (secondLastLocation == null || lastLocation == null) {
//				null
//			} else {
//				Pair(
//					secondLastLocation!!.latitude - lastLocation!!.latitude,
//					secondLastLocation!!.longitude - lastLocation!!.longitude
//				)
//			}
//		}

	// This property is only valid between onCreateView and
	// onDestroyView.
	private val binding get() = _binding!!
	private var _binding: FragmentHomeBinding? = null

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
//		MapsInitializer.initialize(requireContext(), MapsInitializer.Renderer.LATEST) {}
		val homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]

		_binding = FragmentHomeBinding.inflate(inflater, container, false)
		val root: View = binding.root

		val textView: TextView = binding.textHome
		homeViewModel.text.observe(viewLifecycleOwner) {
			textView.text = it
		}

		mapView = binding.mapView
		binding.mapView.onCreate(savedInstanceState)
		binding.mapView.getMapAsync(this)

		return root
	}

	private fun checkPermissions(): Boolean {
		context?.let {
			val fineLocationPermission = ActivityCompat.checkSelfPermission(
				it,
				android.Manifest.permission.ACCESS_FINE_LOCATION
			)
			val coarseLocationPermission = ActivityCompat.checkSelfPermission(
				it,
				android.Manifest.permission.ACCESS_COARSE_LOCATION
			)

			return (fineLocationPermission == PackageManager.PERMISSION_GRANTED
					&& coarseLocationPermission == PackageManager.PERMISSION_GRANTED)
		}

		return false
	}

	private fun requestPermissions() {
		permissionRequestLauncher.launch(
			arrayOf(
				android.Manifest.permission.ACCESS_FINE_LOCATION,
				android.Manifest.permission.ACCESS_COARSE_LOCATION
			)
		)
	}

	private val permissionRequestLauncher =
		registerForActivityResult(
			ActivityResultContracts.RequestMultiplePermissions()
		) { results ->
			val permissionsGranted = results.values.stream().allMatch { it }

			if (permissionsGranted) {
				initLocationProvider()
				subscribeToLocationUpdates()
			} else {
				Toast.makeText(
					context,
					"Unable to display current location without user permissions",
					Toast.LENGTH_SHORT
				).show()
			}
		}

	private fun initLocationProvider() {
		activity?.let {
			locationProvider = LocationServices.getFusedLocationProviderClient(it)
		}
	}

	override fun onMapReady(map: GoogleMap) {
		googleMap = map
		googleMap.moveCamera(CameraUpdateFactory.zoomBy(16.0F))

		if (checkPermissions()) {
			initLocationProvider()
			subscribeToLocationUpdates()
		} else {
			requestPermissions()
		}
	}

	@SuppressLint("MissingPermission")
	/**
	 * Should only be called when checkPermissions() == true
	 */
	private fun subscribeToLocationUpdates() {
		val mLocationRequest = LocationRequest.Builder(1000)
			.setPriority(Priority.PRIORITY_HIGH_ACCURACY)
			.setIntervalMillis(1000)
			.setMinUpdateIntervalMillis(500)
			.setWaitForAccurateLocation(true)
			.build()

		//Location Permission already granted
		locationProvider?.requestLocationUpdates(
			mLocationRequest,
			locationCallback,
			Looper.getMainLooper()
		)
		googleMap.isMyLocationEnabled = true
	}

	private val locationCallback: LocationCallback = object : LocationCallback() {
		override fun onLocationResult(locationResult: LocationResult) {
			super.onLocationResult(locationResult)

			val locationList = locationResult.locations
			if (locationList.isNotEmpty()) {
				//The last location in the list is the newest
				val location = locationList.last()
				val latLng = LatLng(location.latitude, location.longitude)

				Log.i("MapsActivity", "Location: " + location.latitude + " " + location.longitude)
//				Log.i("MapsActivity", "Direction: $vector")

//				secondLastLocation = lastLocation
				lastLocation = location

				// move map camera
				googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
			}
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onResume() {
		mapView?.onResume()
		super.onResume()
	}

	override fun onPause() {
		// stop location updates when Activity is no longer active
		// onMapReady starts location updates again when the map is visible again
		locationProvider?.removeLocationUpdates(locationCallback)

		mapView?.onPause()
		super.onPause()
	}

	override fun onDestroy() {
		mapView?.onDestroy()
		super.onDestroy()
	}

	override fun onLowMemory() {
		mapView?.onLowMemory()
		super.onLowMemory()
	}
}