package ci.harma.mapsdemo.ui.satellites

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SatellitesViewModel : ViewModel() {

	private val _text = MutableLiveData<String>().apply {
		value = "This is satellites Fragment"
	}
	val text: LiveData<String> = _text
}