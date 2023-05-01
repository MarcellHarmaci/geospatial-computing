package ci.harma.mapsdemo.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ci.harma.mapsdemo.databinding.FragmentHomeBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class HomeFragment : Fragment(), OnMapReadyCallback {
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

		return root
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