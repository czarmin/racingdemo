import vision.gears.webglmath.*
import kotlin.math.exp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

open class GameObject: UniformProvider("gameObject") {

    val X = Vec3(1f, 0f, 0f)
    val Y = Vec3(0f, 1f, 0f)
    val Z = Vec3(0f, 0f, 1f)

    val ahead = Z.clone()
    val up = Y.clone()
    val right = X.clone()

  val position = Vec3()
  val euler = Vec3()
  val scale = Vec3(1.0f, 1.0f, 1.0f)

    val velocity = Vec3()
    val angVelocity = Vec3()

    val acceleration = Vec3()
    val angAcceleration = Vec3()

    open val mass : Float = 1f

    val capsule = Capsule(position, up, 1.0f, 1.0f)


  val modelMatrix by Mat4()
    val modelMatrixInverse by Mat4()

    fun collide(other: GameObject, info: Intersection) {

        val normal = info.normal

        // How much the objects are overlapping
        val penetrationDepth = info.penetrationDepth

        // --- 3. Calculate Relative Velocity ---

        // The velocity of 'other' relative to 'this'
        val relativeVelocity = other.velocity - this.velocity

        // The relative velocity along the collision normal (using dot product)
        val velocityAlongNormal = relativeVelocity.dot(normal)

        // If objects are already moving apart, do not apply impulse.
        // This prevents them from being "pulled" back together.
        if (velocityAlongNormal < 0) {
            return
        }

        // --- 4. Calculate and Apply Impulse (The "Bounce") ---

        // Combine the restitution (bounciness) of the two objects
        val e = 0f

        // Calculate the impulse scalar (j). This is the core physics formula.
        val j = -(1 + e) * velocityAlongNormal

        val totalInverseMass = 1f/mass + 1f/other.mass

        // If both objects are immovable, we can't apply impulse
        if (totalInverseMass <= 0) {
            return
        }

        val impulseScalar = j / totalInverseMass

        // The impulse vector is the scalar applied in the direction of the normal
        val impulseVector = normal * impulseScalar

        // Apply the impulse to each object's velocity
        // (multiplied by inverse mass)
        this.velocity -= impulseVector * 1f/mass
        other.velocity += impulseVector * 1f/other.mass

        // Percentage to correct (avoids jitter from 100% correction)
        val correctionPercent = 0.4f
        // A small allowance to prevent over-correction
        val penetrationSlop = 0.01f

        val correctionAmount = maxOf(penetrationDepth - penetrationSlop, 0.0f) / totalInverseMass
        val correctionVector = normal * correctionAmount * correctionPercent

/*
        this.position -= correctionVector * 1f/mass
        other.position += correctionVector * 1f/other.mass
*/
    }

    fun setMeshes(vararg meshes: Mesh) {
      addComponentsAndGatherUniforms(*meshes)
    }

  open fun update() {
      capsule.center.set(position)
      capsule.axis.set(up)

    modelMatrix.set().
      scale(scale).
    rotate(euler.x, X).
    rotate(euler.y, Y).
    rotate(euler.z, Z).
      translate(position)


      modelMatrixInverse.set(modelMatrix).invert()

      ahead.x = sin(euler.y) * cos(euler.z)
      ahead.y = -sin(euler.z)
      ahead.z = cos(euler.y) * cos(euler.z)

      right.set( Y.cross(ahead))

      up.set(ahead.cross(right))

      ahead.normalize()
      right.normalize()
      up.normalize()
  }

  open inner class Motion {
    open operator fun invoke(
      dt : Float = 0.016666f, 
      t : Float = 0.0f, 
      keysPressed : Set<String> = emptySet<String>(), 
      gameObjects : List<GameObject> = emptyList<GameObject>(),
      spawn : List<GameObject> = emptyList<GameObject>()
      ) : Boolean { 
        return true 
    }
  }
  var move = Motion()

}
