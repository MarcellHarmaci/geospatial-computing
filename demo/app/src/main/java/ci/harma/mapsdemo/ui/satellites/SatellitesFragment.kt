package ci.harma.mapsdemo.ui.satellites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import ci.harma.mapsdemo.GoogleMapsFragment
import ci.harma.mapsdemo.databinding.FragmentSatellitesBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class SatellitesFragment : GoogleMapsFragment() {
	private var mapView: MapView? = null

	private var googleMap: GoogleMap? = null
	// This property is only valid between onCreateView and
	// onDestroyView.
	private val binding get() = _binding!!
	private var _binding: FragmentSatellitesBinding? = null

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		val viewModel = ViewModelProvider(this).get(SatellitesViewModel::class.java)

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

	override fun onMapReady(map: GoogleMap) {
		val sydney = LatLng(-34.0, 151.0)

		map.addMarker(
			MarkerOptions()
				.position(sydney)
				.title("Marker in Sydney")
		)
		map.moveCamera(CameraUpdateFactory.newLatLng(sydney))
	}
}