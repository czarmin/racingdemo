import vision.gears.webglmath.Vec3
import kotlin.math.acos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

data class Intersection(val intersect: Boolean, val penetrationDepth: Float, val normal: Vec3)

object GameMath {

    fun clamp(num: Float, min: Float, max: Float): Float {
        if(num < min) return min
        if(num > max) return max
        return num
    }

    fun smoothStep(from: Vec3, to: Vec3, time: Float): Vec3 {
        if((to-from).lengthSquared() < .00001f) return to

        val c = clamp(time, 0f, 1f)

        val t = (3*c*c - 2*c*c*c)
        return from * (1-t) + to * t
    }

    fun smoothOverStep(from: Vec3, to: Vec3, time: Float): Vec3 {
        if((to-from).lengthSquared() < .00000f) return to
        val c = clamp(time, 0f, 1f)

        val t = 10 * c * c - 9 * c * c * c
        val oneminus = clamp(1-t, 0f, 1f)
        return from * oneminus + to * t

    }

    fun slerp(from: Vec3, to: Vec3, time: Float): Vec3 {
        if((to-from).lengthSquared() < .00001f) return to

        val gamma = acos(from.dot(to))

        val c = clamp(time, 0f, 1f)

        return from*(sin((1-c)*gamma)/sin(gamma)) + to*(sin(c*gamma)/sin(gamma))
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

    fun distanceSqBetweenSegmentPoint(s1: Vec3, s2: Vec3, p0: Vec3): Float {
        val w = p0 - s1
        val v = (s2 - s1).normalize()

        val t = (w.dot(v))/(v.dot(v))

        if( t <= 0 ) return (p0 - s1).length()
        if( t >= 1 ) return (p0 - s2).length()
        return (s1 * (1-t) + s2 * t - p0).lengthSquared()
    }

    fun intersectsCC(a:Capsule,b: Capsule): Intersection {

        val vec = shortestVectorBetweenSegments(a.bottom , a.top , b.bottom, b.top)
        val distSq = vec.lengthSquared()
        val radiusSUm = a.radius + b.radius

        if(distSq <= (radiusSUm * radiusSUm)) {
            return Intersection(true, radiusSUm - sqrt(distSq), vec.normalize())
        } else {
            return Intersection(false, -1f, Vec3.zeros)
        }

    }
}