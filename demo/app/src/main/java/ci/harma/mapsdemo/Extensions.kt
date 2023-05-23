package ci.harma.mapsdemo

import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import kotlin.math.roundToInt


fun Float.format(digits: Int) = "%.${digits}f".format(this)

fun Fragment.getDrawable(@DrawableRes resId: Int): Drawable? {
	return AppCompatResources.getDrawable(requireContext(), resId)
}

fun Fragment.dpToPx(dps: Int): Int = TypedValue.applyDimension(
	TypedValue.COMPLEX_UNIT_DIP,
	dps.toFloat(),
	resources.displayMetrics
).roundToInt()

// The extensions below should work and not be deprecated, but Kotlin's overload detection is broken
// and doesn't want to call the new non-deprecated api with the new OnNmeaMessageListener instead of
// the old GpsStatus.NmeaListener, even with explicit types.

//@RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
//fun LocationManager.addNmeaMessageListener(listener: OnNmeaMessageListener, handler: Handler) {
//	addNmeaListener(listener, handler)
//}
//
//@RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
//fun LocationManager.addNmeaMessageListener(executor: Executor, listener: OnNmeaMessageListener) {
//	addNmeaListener(executor, listener)
//}
