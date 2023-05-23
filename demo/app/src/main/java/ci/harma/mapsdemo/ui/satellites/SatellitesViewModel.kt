package ci.harma.mapsdemo.ui.satellites

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.petr_s.nmea.GpsSatellite

class SatellitesViewModel : ViewModel() {
	val satellites: MutableLiveData<List<GpsSatellite>> =
		MutableLiveData<List<GpsSatellite>>()
}