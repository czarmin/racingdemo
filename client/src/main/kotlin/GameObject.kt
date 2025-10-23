import vision.gears.webglmath.*
import kotlin.math.exp
import kotlin.math.PI
import kotlin.math.floor

open class GameObject: UniformProvider("gameObject") {

    val X = Vec3(0f, 0f, 1f)
    val Y = Vec3(0f, 1f, 0f)
    val Z = Vec3(1f, 0f, 0f)

    val ahead = X
    val up = Y
    val right = Z

  val position = Vec3()
  var euler = Vec3()
  val scale = Vec3(1.0f, 1.0f, 1.0f)

  val modelMatrix by Mat4()

    fun setMeshes(vararg meshes: Mesh) {
      addComponentsAndGatherUniforms(*meshes)
    }

  fun update() {
    modelMatrix.set().
      scale(scale).
      rotate(euler.x, X).
        rotate(euler.y, Y).
        rotate(euler.z, Z).
      translate(position)

      val rotMatrix = Mat4().
      rotate(euler.x, X).
      rotate(euler.y, Y).
      rotate(euler.z, Z)

      ahead.set(rotMatrix.times(Vec4(X, 1f)))
      up.set(rotMatrix.times(Vec4(Y, 1f)))
      right.set(rotMatrix.times(Vec4(Z, 1f)))
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
