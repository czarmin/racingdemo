import org.khronos.webgl.Float32Array
import org.khronos.webgl.Uint16Array
import vision.gears.webglmath.Geometry
import vision.gears.webglmath.Vec3
import org.khronos.webgl.WebGLRenderingContext as GL

class RoadGeometry(val gl : WebGL2RenderingContext, val vertices : List<Vec3>, val normals : List<Vec3>) : Geometry() {

    val vertexBuffer = gl.createBuffer()
    init{


        gl.bindBuffer(GL.ARRAY_BUFFER, vertexBuffer)
        gl.bufferData(GL.ARRAY_BUFFER,
            Float32Array( vertices.flatMap { it -> listOf(it.x, it.y, it.z) }.toTypedArray()  ),
            GL.STATIC_DRAW)
    }

    val vertexNormalBuffer = gl.createBuffer()
    init{
        gl.bindBuffer(GL.ARRAY_BUFFER, vertexNormalBuffer)
        gl.bufferData(GL.ARRAY_BUFFER,
            Float32Array( normals.flatMap { it -> listOf(it.x, it.y, it.z) }.toTypedArray()  ),
            GL.STATIC_DRAW)
    }

    val indexBuffer = gl.createBuffer()
    val indexCount = vertices.size - 2
    init{
        val indices: MutableList<Short> = mutableListOf()
        for(i in 0 .. indexCount-3){
            indices.add(i.toShort())
            indices.add((i+2).toShort())
            indices.add((i+1).toShort())
        }

        gl.bindBuffer(GL.ELEMENT_ARRAY_BUFFER, indexBuffer)
        gl.bufferData(GL.ELEMENT_ARRAY_BUFFER,
            Uint16Array(indices.toTypedArray()),
            GL.STATIC_DRAW)
    }

    val inputLayout = gl.createVertexArray()
    init{
        gl.bindVertexArray(inputLayout)

        gl.bindBuffer(GL.ARRAY_BUFFER, vertexBuffer)
        gl.enableVertexAttribArray(0)
        gl.vertexAttribPointer(0,
            3, GL.FLOAT, //< three pieces of float
            false, //< do not normalize (make unit length)
            0, //< tightly packed
            0 //< data starts at array start
        )
        gl.bindBuffer(GL.ARRAY_BUFFER, vertexNormalBuffer)
        gl.enableVertexAttribArray(1)
        gl.vertexAttribPointer(1,
            3, GL.FLOAT, //< three pieces of float
            false, //< do not normalize (make unit length)
            0, //< tightly packed
            0 //< data starts at array start
        )
        gl.bindVertexArray(null)
    }

    override fun draw() {
        gl.bindVertexArray(inputLayout)
        gl.bindBuffer(GL.ELEMENT_ARRAY_BUFFER, indexBuffer)

        gl.drawElements(GL.TRIANGLES, indexCount, GL.UNSIGNED_SHORT, 0)
    }


}