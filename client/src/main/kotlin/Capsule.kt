import vision.gears.webglmath.Vec3

data class Capsule (
    val center: Vec3,
    val axis: Vec3,
    val height: Float,
    val radius: Float
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

    fun clamp(num: Float, min: Float, max: Float): Float {
        if(num < min) return min
        if(num > max) return max
        return num
    }

    // https://stackoverflow.com/questions/2824478/shortest-distance-between-two-line-segments
    fun shortestVectorBetweenSegments(p1: Vec3, q1: Vec3, p2: Vec3, q2: Vec3): Vec3 {
        val d1 = q1 - p1 // Direction vector of segment 1
        val d2 = q2 - p2 // Direction vector of segment 2
        val r = p1 - p2  // Vector between segment origins

        val a = d1.lengthSquared() // Squared length of segment 1
        val e = d2.lengthSquared() // Squared length of segment 2
        val f = d2.dot(r)

        val epsilon = 1e-12

        var s: Float
        var t: Float

        if (a <= epsilon && e <= epsilon) {
            // Both segments are points
            s = 0f
            t = 0f
            return r
        }

        if (a <= epsilon) {
            // First segment is a point
            s = 0f
            t = clamp(-f / e, 0f, 1f)
        } else {
            val c = d1.dot(r)
            if (e <= epsilon) {
                // Second segment is a point
                t = 0f
                s = clamp(-c / a, 0f, 1f)
            } else {
                // General case: both are segments
                val b = d1.dot(d2)
                val denom = a * e - b * b

                // If segments are not parallel, compute closest point on
                // infinite lines and clamp to segment
                s = if (denom != 0f) {
                    clamp((b * f - c * e) / denom, 0f, 1f)
                } else {
                    0f // Parallel, just pick 0 for s
                }

                // Compute point on segment 2 closest to segment 1's clamped point
                val tnom = b * s + f
                t = if (tnom < 0f) {
                    s = clamp(-c / a, 0f, 1f)
                    0f
                } else if (tnom > e) {
                    s = clamp((b - c) / a, 0f, 1f)
                    1f
                } else {
                    tnom / e
                }
            }
        }

        val closestPoint1 = p1 + (d1 * s)
        val closestPoint2 = p2 + (d2 * t)
        return closestPoint1 - closestPoint2
    }

    fun intersects(b: Capsule): Boolean {

        val distSq = shortestVectorBetweenSegments(bottom , top , b.bottom, b.top).lengthSquared()
        val radiusSUm = radius + b.radius

        return distSq <= (radiusSUm * radiusSUm)

    }

}