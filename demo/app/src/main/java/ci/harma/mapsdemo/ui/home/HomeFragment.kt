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
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class HomeFragment : Fragment(), OnMapReadyCallback {

	private var _binding: FragmentHomeBinding? = null

	// This property is only valid between onCreateView and
	// onDestroyView.
	private val binding get() = _binding!!

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

		binding.mapView.onCreate(savedInstanceState)
		binding.mapView.getMapAsync(this)

		return root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onMapReady(googleMap: GoogleMap) {
		val sydney = LatLng(-34.0, 151.0)

		googleMap.addMarker(
			MarkerOptions()
				.position(sydney)
				.title("Marker in Sydney")
		)
		googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
	}
}