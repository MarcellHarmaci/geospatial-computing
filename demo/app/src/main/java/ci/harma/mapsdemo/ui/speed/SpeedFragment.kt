package ci.harma.mapsdemo.ui.speed

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ci.harma.mapsdemo.R
import ci.harma.mapsdemo.RotationTransformation
import ci.harma.mapsdemo.databinding.FragmentSpeedBinding
import ci.harma.mapsdemo.format
import com.bumptech.glide.Glide
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng

class SpeedFragment : Fragment(), OnMapReadyCallback {
	private lateinit var viewModel: SpeedViewModel

	private var mapView: MapView? = null
	private var googleMap: GoogleMap? = null
	private var locationProvider: FusedLocationProviderClient? = null

	// This property is only valid between onCreateView and
	// onDestroyView.
	private val binding get() = _binding!!
	private var _binding: FragmentSpeedBinding? = null

	@SuppressLint("SetTextI18n")
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		viewModel = ViewModelProvider(this)[SpeedViewModel::class.java]

		_binding = FragmentSpeedBinding.inflate(inflater, container, false)
		val root: View = binding.root

		viewModel.location.observe(viewLifecycleOwner) {
			displayLocation(viewModel.location.value)
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
				initLocationHandling()
			} else {
				Toast.makeText(
					context,
					"Unable to display current location without user permissions",
					Toast.LENGTH_SHORT
				).show()
			}
		}

	override fun onMapReady(map: GoogleMap) {
		googleMap = map
		googleMap?.moveCamera(CameraUpdateFactory.zoomBy(16.0F))

		if (checkPermissions()) {
			initLocationHandling()
		} else {
			requestPermissions()
		}
	}

	private fun initLocationHandling() {
		if (googleMap == null || viewModel.isSubscribedToLocationUpdates) return

		viewModel.isSubscribedToLocationUpdates = true
		initLocationProvider()
		subscribeToLocationUpdates()
	}

	private fun initLocationProvider() {
		activity?.let {
			locationProvider = LocationServices.getFusedLocationProviderClient(it)
		}
	}

	/**
	 * Should only be called when permissions are granted
	 */
	@SuppressLint("MissingPermission")
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
		googleMap?.isMyLocationEnabled = true
	}

	private val locationCallback: LocationCallback = object : LocationCallback() {
		override fun onLocationResult(locationResult: LocationResult) {
			super.onLocationResult(locationResult)

			val locationList = locationResult.locations
			if (locationList.isNotEmpty()) {
				// the last location in the list is the most recent
				val newLocation = locationList.last()
				viewModel.setLocation(newLocation)

				Log.d("SpeedFragment", "Speed: ${newLocation.speed.format(2)} m/s")
				Log.d("SpeedFragment", "Bearing: ${newLocation.bearing.format(2)} degrees")
			}
		}
	}

	@SuppressLint("SetTextI18n")
	private fun displayLocation(location: Location?) {
		val speed: Float = location?.speed ?: 0F
		val bearing: Float = location?.bearing ?: 0F

		binding.tvSpeed.text = "${speed.format(2)} m/s";

		val rotationTransformation = RotationTransformation(bearing - 90)
		Glide.with(this)
			.load(R.drawable.ic_baseline_arrow_right_alt_24)
			.transform(rotationTransformation)
			.into(binding.ivDirection)

		// move map camera
		location?.let {
			val latLng = LatLng(it.latitude, it.longitude)
			googleMap?.moveCamera(CameraUpdateFactory.newLatLng(latLng))
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onResume() {
		mapView?.onResume()
		super.onResume()

		initLocationHandling()
	}

	override fun onPause() {
		// stop location updates when Activity is no longer active
		// onMapReady starts location updates again when the map is visible again
		locationProvider?.removeLocationUpdates(locationCallback)
		viewModel.isSubscribedToLocationUpdates = false

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