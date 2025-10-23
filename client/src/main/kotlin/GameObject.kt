import vision.gears.webglmath.*
import kotlin.math.exp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

open class GameObject: UniformProvider("gameObject") {

    val X = Vec3(0f, 0f, 1f)
    val Y = Vec3(0f, 1f, 0f)
    val Z = Vec3(1f, 0f, 0f)

    val ahead = X
    val up = Y
    val right = Z

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

    fun collide(other: GameObject) {

        val combined = velocity * mass + other.velocity * other.mass
        val myVel = combined / mass;
        val otherVel = combined / other.mass;

        velocity.set(myVel)
        other.velocity.set(otherVel)
    }

    fun setMeshes(vararg meshes: Mesh) {
      addComponentsAndGatherUniforms(*meshes)
    }

  open fun update() {
      capsule.center.set(position)
      capsule.axis.set(up)
      val rotMatrix = Mat4().
      rotate(euler.x, X).
      rotate(euler.y, Y).
      rotate(euler.z, Z)

    modelMatrix.set().
      scale(scale).
    rotate(euler.x, X).
    rotate(euler.y, Y).
    rotate(euler.z, Z).
      translate(position)


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
