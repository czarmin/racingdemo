
import GameMath.intersectsCC
import GameMath.intersectsCT
import org.khronos.webgl.WebGLRenderingContext
import vision.gears.webglmath.Vec2
import vision.gears.webglmath.Vec3
import kotlin.math.acos
import kotlin.math.exp
import kotlin.math.pow
import kotlin.reflect.typeOf

class Car(val gl: WebGL2RenderingContext): GameObject() {

    val jsonLoader = JsonLoader()

    val carTex = Texture2D(gl, "media/chevy/chevy.png")

    val vsIdle = Shader(gl, WebGLRenderingContext.VERTEX_SHADER, "trafo-vs.glsl")
    val fsPhong = Shader(gl, WebGLRenderingContext.FRAGMENT_SHADER, "phongblinn-fs.glsl")
    val program = Program(gl, vsIdle, fsPhong)
    val material = Material(program).apply {
        this["diffuse"]?.set(carTex)
        this["specular"]?.set(Vec3(1f, 1f, 1f))
        this["shininess"]?.set(50f)
    }
    val meshes = jsonLoader.loadMeshes(gl, "media/chevy/chassis.json", material)

    override val mass = 1000f

    init {
        position.set(100f, 0f, 0f)
        scale.set(.05f, .05f, .05f)
        capsule.height = 2f
        capsule.radius = 1.5f
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

                if("W" in keysPressed) {
                    acceleration += ahead * 10f / mass;
                }
                if("A" in keysPressed) {
                    angVelocity.y += 4f *dt;
                }
                if("D" in keysPressed) {
                    angVelocity.y += -4f * dt;
                }
                if("SPACE" in keysPressed) {
                    if(velocity.lengthSquared() < .5) velocity.set(Vec3.zeros.clone())
                    velocity *= exp(-dt *  3f)
                }

                if(!("W" in keysPressed || "SPACE" in keysPressed)) {
                    acceleration.set(Vec3.zeros.clone())
                }

                velocity += acceleration * dt
                angVelocity += angAcceleration * dt

                if(velocity.length() > 0f) {
                    val road = gameObjects[1] as Road
                    val closest = road.closestPointToUp(position + velocity)
                    if((road.valueAtFlat(closest) - Vec2(position.x, position.z)).lengthSquared() < (capsule.radius + road.WIDTH).pow(2)) {
                        position.y = road.valueAt(closest).y

                        velocity.y = 0f

                        val ang = acos(road.normalAt(closest).dot(Y))
                        euler.z = ang
                    }
                }
                position += velocity * dt
                euler += angVelocity * dt

                //console.log("POSITION: ${position.x} ${position.y} ${position.z}")
                //console.log("VEL: ${velocity.x} ${velocity.y} ${velocity.z}")
                //console.log("ACC: ${acceleration.x} ${acceleration.y} ${acceleration.z}")

                return true;
            }
        }
    }

}