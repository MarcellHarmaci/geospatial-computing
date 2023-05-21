package ci.harma.mapsdemo

fun Float.format(digits: Int) = "%.${digits}f".format(this)

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
