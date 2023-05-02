package ci.harma.mapsdemo.ui.satellites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ci.harma.mapsdemo.databinding.FragmentSatellitesBinding

class SatellitesFragment : Fragment() {

	private var _binding: FragmentSatellitesBinding? = null

	// This property is only valid between onCreateView and
	// onDestroyView.
	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		val satellitesViewModel =
			ViewModelProvider(this).get(SatellitesViewModel::class.java)

		_binding = FragmentSatellitesBinding.inflate(inflater, container, false)
		val root: View = binding.root

		val textView: TextView = binding.textDashboard
		satellitesViewModel.text.observe(viewLifecycleOwner) {
			textView.text = it
		}
		return root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}