import org.khronos.webgl.WebGLRenderingContext
import vision.gears.webglmath.Vec2
import vision.gears.webglmath.Vec3
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class Road(val gl: WebGL2RenderingContext) : GameObject() {


    val roadTex = Texture2D(gl, "media/road.jpg")

    val vsTrafo = Shader(gl, WebGLRenderingContext.VERTEX_SHADER, "trafo-vs.glsl")
    val fsSolid = Shader(gl, WebGLRenderingContext.FRAGMENT_SHADER, "phongblinn-fs.glsl")
    val program = Program(gl, vsTrafo, fsSolid)
    val material = Material(program).apply {
        this["diffuse"]?.set(roadTex)
        this["specular"]?.set(Vec3(1f, 1f, 1f))
        this["shininess"]?.set(50f)
    }

    val SIZE = 100f
    val HEIGHT = 5f
    val STEP = SIZE/100f
    val WIDTH = 2f

    val geometry: RoadGeometry
    val mesh : Mesh

    val vertices: MutableList<Vec3> = mutableListOf()
    val normals : MutableList<Vec3> = mutableListOf()

    fun x(t: Float): Float {
        return SIZE*cos(t)
    }

    fun y(t: Float): Float {
        return HEIGHT * sin(3*t);
    }

    fun z(t: Float): Float {
        return SIZE*sin(t)
    }

    fun xp(t: Float): Float {
        return SIZE*-sin(t);
    }

    fun yp(t: Float): Float {
        return HEIGHT * 3 * cos(3*t)
    }

    fun zp(t: Float): Float {
        return SIZE * cos(t)
    }

    fun xpp(t: Float): Float {
        return SIZE*-cos(t)
    }

    fun ypp(t: Float): Float {
        return HEIGHT * 9 * -sin(3*t)
    }

    fun zpp(t: Float): Float {
        return SIZE * -sin(t)
    }

    fun squaredDistanceFun(t: Float, p: Vec3): Float {
        return (x(t) - p.x)*xp(t)+(y(t) - p.y)*yp(t)+(z(t)-p.z)*zp(t)
    }

    fun squaredDistanceFunDer(t: Float, p: Vec3): Float {
        return xp(t)*xp(t) +yp(t)*yp(t) + zp(t)*zp(t) + (x(t) - p.x)*xpp(t) + (y(t) - p.y)*ypp(t) + (z(t) - p.z)*zpp(t)
    }

    fun valueAt(t: Float): Vec3 {
        return Vec3(x(t), y(t), z(t))
    }

    fun valueAtFlat(t: Float): Vec2 {
        return Vec2(x(t), z(t))
    }

    fun directionAt(t: Float): Vec3 {
        return Vec3(xp(t), yp(t), zp(t))
    }

    fun normalAt(t: Float): Vec3 {
        val tangent = directionAt(t) // This is the 'ahead' vector
        val worldUp = Vec3(0f, 1f, 0f)

        val right = worldUp.cross(tangent).normalize()

        val up = tangent.cross(right).normalize()
        return up
    }

    fun closestPoint(pos: Vec3, initial: Float): Float {
        // Newtons method
        var approx = initial
        for(i in 0..1000) {
            approx -= squaredDistanceFun(approx, pos) / squaredDistanceFunDer(approx, pos)
        }
        return approx
    }

    init {

        capsule.center.set(Vec3(0f,0f,0f))
        capsule.radius = 0.0001f
        capsule.height = 0.0001f
        var i = 0f
        while(i <= SIZE) {

            val t = (i / SIZE) * 2f * PI.toFloat()

            val current = valueAt(t)
            val ahead = directionAt(t)
            val right = Vec3(0f, 1f, 0f).cross(ahead).normalize()
            val up = normalAt(t)

            normals.add(up)
            normals.add(up)
            vertices.add(current + right * WIDTH)
            vertices.add(current - right * WIDTH)
            i += STEP
        }

        geometry = RoadGeometry(gl, vertices, normals)
        mesh = Mesh(material, geometry)

        setMeshes(mesh)
    }

}