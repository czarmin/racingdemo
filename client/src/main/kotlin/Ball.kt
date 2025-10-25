import org.khronos.webgl.WebGLRenderingContext
import vision.gears.webglmath.Vec2
import vision.gears.webglmath.Vec3
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin

class Ball(val gl: WebGL2RenderingContext): GameObject() {

    val jsonLoader = JsonLoader()

    val vsIdle = Shader(gl, WebGLRenderingContext.VERTEX_SHADER, "trafo-vs.glsl")
    val fsPhong = Shader(gl, WebGLRenderingContext.FRAGMENT_SHADER, "solid-fs.glsl")
    val program = Program(gl, vsIdle, fsPhong)
    val material = Material(program)
    val meshes = jsonLoader.loadMeshes(gl, "media/sphere.json", material)

    override val mass = 50f

    init {
        position.set(100f, 0f, 0f)
        capsule.height = .001f
        capsule.radius = 1f
        setMeshes(*meshes)

        move = object : GameObject.Motion() {
            override fun invoke(
                dt: Float,
                t: Float,
                keysPressed: Set<String>,
                gameObjects: List<GameObject>,
                spawn: List<GameObject>
            ): Boolean {
                velocity.y -= 1 * dt
                velocity *= exp(-dt * (500f / mass))
                acceleration *= exp(-dt * (500f / mass))
                angVelocity *= exp(-dt * (5000f / mass))

                capsule.axis.set(ahead)
                capsule.center.set(position)

                position += velocity * dt
                euler += angVelocity * dt

                if(velocity.length() > 0f) {
                    val road = gameObjects[1] as Road
                    val closest = road.closestPointToUp(position)
                    if((road.valueAtFlat(closest) - Vec2(position.x, position.z)).lengthSquared() < (capsule.radius + road.WIDTH).pow(2)) {
                        // Snap position
                        position.y = road.valueAt(closest).y + .5f
                        velocity.y = 0f

                        // Get road normal
                        val roadNormal = road.normalAt(closest)
                        up.set(roadNormal)

                        // Get car's ahead direction from its yaw
                        val yaw = euler.y
                        val flatAhead = Vec3(sin(yaw), 0f, cos(yaw))

                        // Build new basis
                        right.set(roadNormal.cross(flatAhead).normalize())
                        ahead.set(right.cross(roadNormal).normalize())

                        // Extract Pitch and Roll from new basis
                        euler.x = asin(-ahead.y)
                        euler.z = atan2(right.y, up.y)
                    }
                }

                //console.log("POSITION: ${position.x} ${position.y} ${position.z}")
                //console.log("VEL: ${velocity.x} ${velocity.y} ${velocity.z}")
                //console.log("ACC: ${acceleration.x} ${acceleration.y} ${acceleration.z}")

                return true;
            }
        }
    }

}
