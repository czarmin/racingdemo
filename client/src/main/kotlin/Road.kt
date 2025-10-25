import org.khronos.webgl.WebGLRenderingContext
import vision.gears.webglmath.Geometry
import vision.gears.webglmath.Vec2
import vision.gears.webglmath.Vec3
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class Road(val gl: WebGL2RenderingContext) : GameObject() {


    val vsTrafo = Shader(gl, WebGLRenderingContext.VERTEX_SHADER, "trafo-vs.glsl")
    val fsSolid = Shader(gl, WebGLRenderingContext.FRAGMENT_SHADER, "solid-fs.glsl")
    val program = Program(gl, vsTrafo, fsSolid)
    val material = Material(program)

    val SIZE = 100f
    val HEIGHT = 5f
    val STEP = .5f / SIZE
    val WIDTH = 2f

    val geometry: RoadGeometry
    val mesh : Mesh

    val vertices: MutableList<Vec3> = mutableListOf()
    val normals : MutableList<Vec3> = mutableListOf()
    val tris : MutableList<Triangle> = mutableListOf()

    fun valueAt(t: Float): Vec3 {
        return Vec3(SIZE* cos(t), HEIGHT * sin(3*t), SIZE * sin(t))
    }
    fun directionAt(t: Float): Vec3 {
        return Vec3(SIZE* -sin(t), HEIGHT * 3* cos(3*t), SIZE * cos(t)).normalize()
    }

    fun normalAt(t: Float): Vec3 {
        // FIX: Calculate the true surface normal (binormal)
        val tangent = directionAt(t) // This is the 'ahead' vector
        val worldUp = Vec3(0f, 1f, 0f)

        // 1. Find the 'right' vector (90° to tangent and world up)
        val right = worldUp.cross(tangent).normalize()

        // 2. Find the 'up' vector (90° to tangent and right)
        val up = tangent.cross(right).normalize()
        return up
    }


    fun valueAtFlat(t: Float): Vec2 {
        return Vec2(SIZE* cos(t), SIZE * sin(t))
    }

    fun closestPointToUp(pos: Vec3): Float {
        val flatPos = Vec2(pos.x, pos.z)
        var minT = 0f
        var minDist = (flatPos-valueAtFlat(0f)).lengthSquared()
        var i = 0f
        while(i <= SIZE) {
            val t = (i / SIZE) * 6.1f * PI.toFloat()

            val current = valueAtFlat(t)
            val dist = (current - flatPos).lengthSquared()
            if(dist < minDist) {
                minDist = dist
                minT = t
            }

            i += STEP
        }

        return minT
    }

    init {

        var i = 0f
        var c = 0
        while(i <= SIZE) {

            val t = (i / SIZE) * 6.1f * PI.toFloat()

            val current = valueAt(t)
            val ahead = directionAt(t)
            val right = Vec3(0f, 1f, 0f).cross(ahead).normalize()
            val up = normalAt(t)

            normals.add(up)
            normals.add(up)
            vertices.add(current + right * WIDTH)
            vertices.add(current - right * WIDTH)

            if(c > 2) {
                val p0 = vertices[vertices.size - 1]
                val p1 = vertices[vertices.size - 2]
                val p2 = vertices[vertices.size - 3]
                val normal = (p0-p1).cross((p0-p2)).normalize()

                tris.add(Triangle(p0, p1, p2, normal))
            }
            c++;
            i += STEP
        }

        geometry = RoadGeometry(gl, vertices, normals)
        mesh = Mesh(material, geometry)

        setMeshes(mesh)
    }

}