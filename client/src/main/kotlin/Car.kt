
import GameMath.intersectsCC
import org.khronos.webgl.WebGLRenderingContext
import vision.gears.webglmath.Vec2
import vision.gears.webglmath.Vec3
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin
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

    override val mass = 500f

    var previousGuess = 0f

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
                velocity.y -= 9.8f * dt
                velocity *= exp(-dt * (500f / mass))
                acceleration *= exp(-dt * (500f / mass))
                angVelocity *= exp(-dt * (5000f / mass))

                capsule.axis.set(ahead)
                capsule.center.set(position)

                if("W" in keysPressed) {
                    acceleration += ahead * 100f / mass;
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
                position += velocity * dt
                euler += angVelocity * dt

                if(velocity.length() > 0f) {
                    val road = gameObjects[1] as Road
                    val closest = road.closestPoint(position, previousGuess)
                    previousGuess = closest
                    if((road.valueAt(closest) - position).lengthSquared() < (capsule.radius + road.WIDTH/2f).pow(2)) {
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
                    for(obj in gameObjects) {
                        if(obj == this@Car) continue
                        val info = intersectsCC(capsule, obj.capsule)
                        if(info.intersect) {
                            collide(obj, info)
                        }
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