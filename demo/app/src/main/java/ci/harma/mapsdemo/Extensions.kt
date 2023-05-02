package ci.harma.mapsdemo

fun Float.format(digits: Int) = "%.${digits}f".format(this)