package ci.harma.mapsdemo.ui.satellites

import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import ci.harma.mapsdemo.GoogleMapsFragment
import ci.harma.mapsdemo.databinding.FragmentSatellitesBinding
import ci.harma.mapsdemo.ui.satellites.nmea.MyNMEAHandler
import com.github.petr_s.nmea.GpsSatellite
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class SatellitesFragment : GoogleMapsFragment() {
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var _binding: FragmentSatellitesBinding? = null
    private var googleMap: GoogleMap? = null

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

        // TODO subscribe to nmea messages. something like this
//        val lm = context?.getSystemService(LOCATION_SERVICE) as LocationManager?
//        val parser = NMEAParser(MyNMEAHandler(locationListener, satellitesListener))
//        lm?.addNmeaListener(OnNmeaMessageListener { message, timestamp -> parser.parse(message) })
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
            TODO("Not yet implemented")
        }
    }

    private val satellitesListener = object : MyNMEAHandler.SatellitesListener {
        override fun onSatellites(satellites: MutableList<GpsSatellite>?) {
            TODO("Not yet implemented")
        }
    }

}