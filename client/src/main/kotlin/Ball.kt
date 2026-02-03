import GameMath.intersectsCC
import org.khronos.webgl.WebGLRenderingContext
import vision.gears.webglmath.Vec2
import vision.gears.webglmath.Vec3
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin

class Ball(val gl: WebGL2RenderingContext): GameObject() {

    val jsonLoader = JsonLoader()
    val ballTex = Texture2D(gl, "media/ball.jpg")

    val vsIdle = Shader(gl, WebGLRenderingContext.VERTEX_SHADER, "trafo-vs.glsl")
    val fsPhong = Shader(gl, WebGLRenderingContext.FRAGMENT_SHADER, "phongblinn-fs.glsl")
    val program = Program(gl, vsIdle, fsPhong)
    val material = Material(program).apply {
        this["diffuse"]?.set(ballTex)
        this["specular"]?.set(Vec3(1f, 1f, 1f))
        this["shininess"]?.set(250f)
    }
    val meshes = jsonLoader.loadMeshes(gl, "media/sphere.json", material)

    override val mass = 5f

    var previousGuess = 0f

    init {
        position.set(100f, 2f, 10f)
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
                val avel = velocity / capsule.radius
                angVelocity.set(avel.length(), 0f, 0f)

                capsule.axis.set(ahead)
                capsule.center.set(position)

                position += velocity * dt
                euler += angVelocity * dt

                euler.y = acos(velocity.clone().normalize().dot(Vec3(0f,0f,1f)))

                if(velocity.length() > 0f) {
                    val road = gameObjects[1] as Road
                    val closest = road.closestPoint(position, previousGuess)
                    previousGuess = closest
                    if((road.valueAt(closest) - position).lengthSquared() <= (capsule.radius + road.WIDTH).pow(2)) {
                        position.y = road.valueAt(closest).y + capsule.radius
                        velocity.y = 0f
                    }

                    for(obj in gameObjects) {
                        if(obj == this@Ball) continue
                        val info = intersectsCC(capsule, obj.capsule)
                        if(info.intersect) {
                            collide(obj, info)
                        }
                    }
                }

                //console.log("POSITION: ${position.x} ${position.y} ${position.z}")
                //console.log("VEL: ${velocity.x} ${velocity.y} ${velocity.z}")
                //console.log("ACC: ${acceleration.x} ${acceleration.y} ${acceleration.z}")

                if(position.y < -30f) return false;
                return true;
            }
        }
    }

}
