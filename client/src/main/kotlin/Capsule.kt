import GameMath.clamp
import vision.gears.webglmath.Vec3

data class Capsule (
    val center: Vec3,
    val axis: Vec3,
    var height: Float,
    var radius: Float
) {

    init {
        require(height > 0) { "Height must be greater than zero." }
        require(radius > 0) { "Radius must be greater than zero." }
        axis.normalize()
    }

    val bottom: Vec3
        get() {
            return center - axis * (height / 2f)
        }

    val top: Vec3
        get() {
            return center + axis * (height / 2f)
        }
}