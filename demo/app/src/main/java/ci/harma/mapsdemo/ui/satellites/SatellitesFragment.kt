package ci.harma.mapsdemo.ui.satellites

import android.Manifest
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.location.OnNmeaMessageListener
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import ci.harma.mapsdemo.R
import ci.harma.mapsdemo.databinding.FragmentSatellitesBinding
import ci.harma.mapsdemo.dpToPx
import ci.harma.mapsdemo.getDrawable
import ci.harma.mapsdemo.ui.satellites.nmea.EmptyLocationListener
import ci.harma.mapsdemo.ui.satellites.nmea.MyNMEAHandler
import ci.harma.mapsdemo.ui.satellites.nmea.NmeaFragment
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
		override fun onSatellites(satellites: List<GpsSatellite?>?) {
			Log.d("NMEA-onSatellites", satellites?.toString() ?: "")

			// TODO remove images drawn before

			satellites?.filterNotNull()?.forEach { sat ->
				val rootSize = measureRootView()
				val position = calcPosition(rootSize, sat.azimuth)
				val imageView = createImageView(position.first, position.second)
				binding.root.addView(imageView)
			}
		}
	}

	private fun measureRootView(): Pair<Int, Int> {
		binding.root.measure(View.MeasureSpec.EXACTLY, View.MeasureSpec.EXACTLY)
		val width = binding.root.measuredWidth
		val height = binding.root.measuredHeight

		return Pair(width, height)
	}

	private fun calcPosition(rootSize: Pair<Int, Int>, azimuth: Float):Pair<Int, Int> {
		val rootWidth: Int = rootSize.first
		val rootHeight: Int = rootSize.second

		// TODO calculate top left coordinates of imageview
		return Pair(0,0)
	}

	private fun createImageView(marginLeft: Int, marginTop: Int): ImageView {
		val imageView = ImageView(requireContext())

		val layoutParams = ConstraintLayout.LayoutParams(
			dpToPx(64),
			dpToPx(64)
		)
		layoutParams.startToStart = binding.root.id
		layoutParams.topToTop = binding.root.id
		layoutParams.setMargins(marginLeft, marginTop, 0,0)
		imageView.layoutParams = layoutParams

		imageView.setImageDrawable(getDrawable(R.drawable.ic_baseline_satellite_alt_24))
		imageView.imageTintList = ColorStateList.valueOf(Color.WHITE)

		return imageView
	}

}