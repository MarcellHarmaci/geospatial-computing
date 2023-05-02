package ci.harma.mapsdemo

import android.graphics.Bitmap
import android.graphics.Matrix
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.util.Util
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.security.MessageDigest

/**
 * @see <a href="https://bumptech.github.io/glide/doc/transformations.html#required-methods">
 *     Glide Documentation
 * </a>
 */
class RotationTransformation(var rotationAngle: Float = 0f) :
	BitmapTransformation() {
	private val ID = "ci.harma.mapsdemo.RotationTransformation"
	private val ID_BYTES: ByteArray = ID.toByteArray(Charset.forName("UTF-8"))

	override fun transform(
		pool: BitmapPool,
		toTransform: Bitmap,
		outWidth: Int,
		outHeight: Int
	): Bitmap {
		val matrix = Matrix()
		matrix.postRotate(rotationAngle)

		return Bitmap.createBitmap(
			toTransform, 0, 0, toTransform.width, toTransform.height, matrix, true
		)
	}

	override fun equals(other: Any?): Boolean {
		if (other is RotationTransformation) {
			return rotationAngle == other.rotationAngle
		}
		return false
	}

	override fun hashCode(): Int {
		return Util.hashCode(ID_BYTES, Util.hashCode(rotationAngle))
	}

	override fun updateDiskCacheKey(messageDigest: MessageDigest) {
		messageDigest.update(ID_BYTES)
		messageDigest.update(ByteBuffer.allocate(4).putFloat(rotationAngle))
	}
}