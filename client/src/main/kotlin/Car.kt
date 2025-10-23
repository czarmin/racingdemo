
import org.khronos.webgl.WebGLRenderingContext
import vision.gears.webglmath.Vec3
import kotlin.math.exp

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

    override val mass = 1f

    init {
        setMeshes(*meshes)

        move = object : GameObject.Motion() {
            override fun invoke(
                dt: Float,
                t: Float,
                keysPressed: Set<String>,
                gameObjects: List<GameObject>,
                spawn: List<GameObject>
            ): Boolean {
                velocity *= exp(-dt * (1.3f / mass))
                acceleration *= exp(-dt * (1.3f / mass))
                angVelocity *= exp(-dt * (3f / mass))

                if("W" in keysPressed) {
                    acceleration += ahead * 5f * mass;
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


                var i = 0.0f
                var done = false;
                if(velocity.length() > 0f) {
                while(i <= velocity.length() && !done) {

                    for(obj in gameObjects) {
                        if(obj == this@Car) continue;

                        if(capsule.intersects(obj.capsule)) {
                            i -= velocity.length() / 5f
                            done = true
                            break
                        }
                    }

                    i += velocity.length() / 10.0f
                }

                }
                position += velocity * dt
                euler += angVelocity * dt

                //console.log("POSITION: ${position.x} ${position.y} ${position.z}")
                console.log("VEL: ${velocity.x} ${velocity.y} ${velocity.z}")
                //console.log("ACC: ${acceleration.x} ${acceleration.y} ${acceleration.z}")

                return true;
            }
        }
    }

}