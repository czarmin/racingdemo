import GameMath.slerp
import GameMath.smoothOverStep
import GameMath.smoothStep
import vision.gears.webglmath.UniformProvider
import vision.gears.webglmath.Vec2
import vision.gears.webglmath.Vec3
import vision.gears.webglmath.Mat4
import kotlin.math.tan
import org.w3c.dom.events.*
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.sqrt

class PerspectiveCamera(vararg programs : Program) : UniformProvider("camera") {

  val position by Vec3(0.0f, 0.0f, 1.0f) 
  var roll = 0.0f
  var pitch = 0.0f
  var yaw = 0.0f

  var fov = 2
  var aspect = 1.0f
  var nearPlane = 0.1f
  var farPlane = 1000.0f 

  var ahead = Vec3(0.0f, 0.0f,-1.0f) 
  var right = Vec3(1.0f, 0.0f, 0.0f) 
  var up    = Vec3(0.0f, 1.0f, 0.0f)  

  val followRadius = 2f;

  var isDragging = false
  val mouseDelta = Vec2(0.0f, 0.0f) 

  val rotationMatrix = Mat4()
  val viewProjMatrix by Mat4()
  val rayDirMatrix by Mat4()

  companion object {
    val worldUp = Vec3(0.0f, 1.0f, 0.0f)
  }

  init {
    update()
    addComponentsAndGatherUniforms(*programs)
  }
  
  fun update() { 
    rotationMatrix.set().
      rotate(roll).
      rotate(pitch, 1.0f, 0.0f, 0.0f).
      rotate(yaw, 0.0f, 1.0f, 0.0f)

    viewProjMatrix.set(rotationMatrix).
      translate(position).
      invert()

    val yScale = 1.0f / tan(fov * 0.5f) 
    val xScale = yScale / aspect
    val f = farPlane 
    val n = nearPlane 
    viewProjMatrix *= Mat4( 
        xScale ,    0.0f ,         0.0f ,   0.0f, 
          0.0f ,  yScale ,         0.0f ,   0.0f, 
          0.0f ,    0.0f ,  (n+f)/(n-f) ,  -1.0f, 
          0.0f ,    0.0f ,  2*n*f/(n-f) ,   0.0f)

    rayDirMatrix.set().translate(position)
    rayDirMatrix *= viewProjMatrix
    rayDirMatrix.invert()
  }

  fun setAspectRatio(ar : Float) { 
    aspect = ar
    update()
  } 

  fun move(entity: GameObject, dt: Float) {

      val ang = PI.toFloat() / 4f
      val backPosition = entity.position + (entity.up * sin(ang) * followRadius) + (-entity.ahead * cos(ang) * followRadius)

      val t = exp(-dt * 100f)

      position.set(smoothStep(position, backPosition, t))

      lookAt(entity.position, dt)

      update()
  }

    fun lookAt(pos: Vec3, dt: Float) {
        val weight = exp(-dt * 100f)

        val newAhead = (position - pos).normalize()
        val newRight = worldUp.cross(ahead).normalize()
        val newUp = ahead.cross(right)

        ahead = slerp(ahead, newAhead, weight)
        right = slerp(right, newRight, weight)
        up = slerp(up, newUp, weight)
        yaw = atan2(ahead.x, ahead.z);
        pitch = atan2(-ahead.y, sqrt(ahead.x*ahead.x + ahead.y*ahead.y))

    }
  
  fun mouseDown() { 
    isDragging = true 
    mouseDelta.set() 
  } 

  fun mouseMove(event : MouseEvent) { 
    mouseDelta.x += event.asDynamic().movementX as Float
    mouseDelta.y += event.asDynamic().movementY as Float
    event.preventDefault()
  } 
  fun mouseUp() { 
    isDragging = false
  }  
}