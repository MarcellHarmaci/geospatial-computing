package ci.harma.mapsdemo.ui.speed

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SpeedViewModel : ViewModel() {
	private val _location: MutableLiveData<Location?> = MutableLiveData<Location?>()
	val location: LiveData<Location?>
		get() = _location

	fun setLocation(location: Location?) {
		_location.value = location
	}
}