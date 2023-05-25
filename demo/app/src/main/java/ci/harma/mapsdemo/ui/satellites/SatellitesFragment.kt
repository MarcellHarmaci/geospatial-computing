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
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import ci.harma.mapsdemo.R
import ci.harma.mapsdemo.databinding.FragmentSatellitesBinding
import ci.harma.mapsdemo.dpToPx
import ci.harma.mapsdemo.ui.satellites.nmea.EmptyLocationListener
import ci.harma.mapsdemo.ui.satellites.nmea.MyNMEAHandler
import ci.harma.mapsdemo.ui.satellites.nmea.NmeaFragment
import com.github.petr_s.nmea.GpsSatellite
import com.github.petr_s.nmea.NMEAParser
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

class SatellitesFragment : NmeaFragment() {
	private lateinit var viewModel: SatellitesViewModel
	private val satImages = mutableListOf<ImageView>() // not in VM, because it's view state

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
		viewModel = ViewModelProvider(this)[SatellitesViewModel::class.java]

		_binding = FragmentSatellitesBinding.inflate(inflater, container, false)
		val root: View = binding.root

		mapView = binding.mapView
		mapView?.onCreate(savedInstanceState)
		mapView?.getMapAsync(this)

		viewModel.satellites.observe(viewLifecycleOwner) {
			satImages.forEach { imageView ->
				binding.root.removeView(imageView)
			}
			satImages.clear()

			it.forEach { sat ->
				val rootSize = measureRootView()
				val position = calcPosition(
					rootSize,
					sat.azimuth
				)
				val imageView = addSatelliteImage(position.first, position.second)
				satImages.add(imageView)
			}
		}

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
			viewModel.satellites.value = satellites?.filterNotNull() ?: listOf()
		}
	}

	private fun measureRootView(): Pair<Int, Int> {
//		binding.root.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
		val width = binding.root.measuredWidth
		val height = binding.root.measuredHeight

		return Pair(width, height)
	}

	private fun calcPosition(
		rootSize: Pair<Int, Int>,
		azimuth: Float
	): Pair<Int, Int> {
		val width = min(rootSize.first, rootSize.second).toDouble()
		val height = min(rootSize.first, rootSize.second).toDouble()

		val centerX = width / 2
		val centerY = height / 2
		val azimuthRad = azimuth * PI / 180

		val x: Double
		val y: Double

		/**
		 * |---------------------------------|
		 * |                |                x (X, Y) = ?
		 * |                |               /|
		 * |                |  azimuth    /  |
		 * |                |   angle   /    |
		 * |                |         /      |
		 * |                |       /        |
		 * |                |     /          |
		 * |                |   /            |
		 * |                | /              |
		 * |                x                |
		 * |                                 |
		 * |                                 |
		 * |                                 |
		 * |                                 |
		 * |                                 |
		 * |                                 |
		 * |                                 |
		 * |                                 |
		 * |---------------------------------|
		 * Calculate (X, Y), then if it is on the right/bottom side shift it left/up 64 dps
		 * so the image can fit on the screen.
		 * The bottom-right 64dp should be shifted both left & up.
		 */
		// TODO fix calculation
		when (azimuth) {
			in 0.0..90.0 -> {
				x = centerX + (width / 2) * cos(azimuthRad)
				y = centerY - (height / 2) * sin(azimuthRad)
			}

			in 90.0..180.0 -> {
				val adjustedAngleRad = azimuthRad - PI / 2
				x = centerX - (width / 2) * sin(adjustedAngleRad)
				y = centerY - (height / 2) * cos(adjustedAngleRad)
			}

			in 180.0..270.0 -> {
				val adjustedAngleRad = azimuthRad - PI
				x = centerX - (width / 2) * cos(adjustedAngleRad)
				y = centerY + (height / 2) * sin(adjustedAngleRad)
			}

			else -> {
				val adjustedAngleRad = azimuthRad - 3 * PI / 2
				x = centerX + (width / 2) * sin(adjustedAngleRad)
				y = centerY + (height / 2) * cos(adjustedAngleRad)
			}
		}

		return Pair(x.roundToInt(), y.roundToInt())
	}

	private fun addSatelliteImage(marginLeft: Int, marginTop: Int): ImageView {
		val layoutParams = ConstraintLayout.LayoutParams(
			dpToPx(64),
			dpToPx(64)
		)
		layoutParams.setMargins(marginLeft, marginTop, 0, 0)

		val imageView = ImageView(requireContext())
		imageView.layoutParams = layoutParams
		imageView.id = View.generateViewId()
		imageView.setImageResource(R.drawable.ic_baseline_satellite_alt_24)
		imageView.imageTintList = ColorStateList.valueOf(Color.WHITE)

		binding.root.addView(imageView)

		val constraints = ConstraintSet()
		constraints.clone(binding.root)
		// startToStartOf="parent"
		constraints.connect(
			imageView.id, ConstraintSet.START,
			ConstraintSet.PARENT_ID, ConstraintSet.START
		)
		// topToTopOf="parent"
		constraints.connect(
			imageView.id, ConstraintSet.TOP,
			ConstraintSet.PARENT_ID, ConstraintSet.TOP
		)
		constraints.applyTo(binding.root)

		return imageView
	}

}