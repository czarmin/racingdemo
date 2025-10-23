
import org.khronos.webgl.WebGLRenderingContext
import vision.gears.webglmath.Vec3

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

    init {
        setMeshes(*meshes)
    }
}