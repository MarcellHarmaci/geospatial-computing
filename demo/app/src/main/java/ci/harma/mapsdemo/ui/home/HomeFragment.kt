package ci.harma.mapsdemo.ui.home

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
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
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class HomeFragment : Fragment(), OnMapReadyCallback {
	private lateinit var locationProvider: FusedLocationProviderClient
	private var mapView: MapView? = null

	// This property is only valid between onCreateView and
	// onDestroyView.
	private val binding get() = _binding!!
	private var _binding: FragmentHomeBinding? = null

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
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

		val permissionsGranted = checkPermissions()
		if (permissionsGranted) {
			initLocationProvider()
			getLastLocation()
		}

		return root
	}

	private fun initLocationProvider() {
		activity?.let {
			locationProvider = LocationServices.getFusedLocationProviderClient(it)
		}
	}

	private fun getLastLocation() {
		locationProvider.lastLocation.addOnSuccessListener { location ->
			val altitude = location?.altitude
			val latitude = location?.latitude
			val accuracy = location?.accuracy
			Log.d("demo-location", "altitude:$altitude | latitude:$latitude | accuracy:$accuracy")
		}
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

			if (fineLocationPermission == PackageManager.PERMISSION_GRANTED
				|| coarseLocationPermission == PackageManager.PERMISSION_GRANTED
			) {
				return true
			} else {
				requestPermissions()
				return false
			}
		}

		return false
	}

	private fun requestPermissions() {
		requestPermissionLauncher.launch(
			arrayOf(
				android.Manifest.permission.ACCESS_FINE_LOCATION,
				android.Manifest.permission.ACCESS_COARSE_LOCATION
			)
		)
	}

	private val requestPermissionLauncher =
		registerForActivityResult(
			ActivityResultContracts.RequestMultiplePermissions()
		) { results ->
			val permissionsGranted = results.values.stream().allMatch { it }

			if (permissionsGranted) {
				initLocationProvider()
			} else {
				Toast.makeText(
					context,
					"Unable to display current location without user permissions",
					Toast.LENGTH_SHORT
				).show()
			}
		}

	override fun onMapReady(map: GoogleMap) {
		val sydney = LatLng(-34.0, 151.0)

		map.addMarker(
			MarkerOptions()
				.position(sydney)
				.title("Marker in Sydney")
		)
		map.moveCamera(CameraUpdateFactory.newLatLng(sydney))
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