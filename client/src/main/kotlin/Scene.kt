import org.w3c.dom.HTMLCanvasElement
import org.khronos.webgl.WebGLRenderingContext as GL
import vision.gears.webglmath.UniformProvider
import vision.gears.webglmath.Vec3
import kotlin.js.Date
import kotlin.math.PI
import kotlin.random.Random

class Scene (
  val gl : WebGL2RenderingContext) : UniformProvider("scene") {
    val vsQuad = Shader(gl, GL.VERTEX_SHADER, "quad-vs.glsl")
  val fsBackground = Shader(gl, GL.FRAGMENT_SHADER, "background-fs.glsl")
  val backgroundProgram = Program(gl, vsQuad, fsBackground)
  val backgroundMaterial = Material(backgroundProgram)
  val skyCubeTexture = TextureCube(gl,
      "media/px.png", "media/nx.png",
      "media/py.png", "media/ny.png",
      "media/pz.png", "media/nz.png"
    )
  init {
    backgroundMaterial["envTexture"]?.set( skyCubeTexture )
  }
  val quadGeometry = TexturedQuadGeometry(gl)
  val backgroundMesh = Mesh(backgroundMaterial, quadGeometry)

    val car = Car(gl)
    val road = Road(gl)
    val ball = Ball(gl)

  val gameObjects = ArrayList<GameObject>()

    init {
        gameObjects.add( car )
        gameObjects.add( road )
        gameObjects.add( ball )
    }

    var cooldown = 0f

    val lights = Array<Light>(1) { Light(it, *Program.all) }
    init{
        lights[0].position.set(1.0f, 1.0f, 1.0f, 0.0f).normalize();
        lights[0].powerDensity.set(1f, 1f, 0.9f);
    }

    //val camera = FreeViewCamera(*Program.all)
    val camera = PerspectiveCamera(*Program.all)

  val timeAtFirstFrame = Date().getTime()
  var timeAtLastFrame =  timeAtFirstFrame

  init{
    gl.enable(GL.DEPTH_TEST)
    addComponentsAndGatherUniforms(*Program.all)
  }


  fun resize(gl : WebGL2RenderingContext, canvas : HTMLCanvasElement) {
    gl.viewport(0, 0, canvas.width, canvas.height)
    camera.setAspectRatio(canvas.width.toFloat() / canvas.height.toFloat())
  }

    var fps = 0f
    var lastTime = 0

  fun update(gl : WebGL2RenderingContext, keysPressed : Set<String>) {

    val timeAtThisFrame = Date().getTime() 
    val dt = (timeAtThisFrame - timeAtLastFrame).toFloat() / 1000.0f
    val t  = (timeAtThisFrame - timeAtFirstFrame).toFloat() / 1000.0f    
    timeAtLastFrame = timeAtThisFrame

      fps += 1
      if(t.toInt() != lastTime) {
          lastTime = t.toInt()
          console.log(fps)
          fps = 0f
      }

    camera.move(car, dt)
      //camera.move(dt, keysPressed)

      if("F" in keysPressed && cooldown <= 0f) {
          val rand = Random.nextFloat()
          val pos = road.valueAt(rand * 2f * PI.toFloat())
          pos.apply { y += 5f }
          val ball = Ball(gl).apply {
              position.set(pos)
              previousGuess = rand * 2f * PI.toFloat()
          }
          gameObjects.add(ball)
          cooldown = 1f
      }
      cooldown -= dt

    // clear the screen
    gl.clearColor(0.3f, 0.0f, 0.3f, 1.0f)
    gl.clearDepth(1.0f)
    gl.clear(GL.COLOR_BUFFER_BIT or GL.DEPTH_BUFFER_BIT)

    val spawn = ArrayList<GameObject>()
    val killList = ArrayList<GameObject>()    

    gameObjects.forEach {
      if(!it.move(dt, t, keysPressed, gameObjects, spawn)){
        killList.add(it)
      }
    }

    killList.forEach{ gameObjects.remove(it) }
    spawn.forEach{ gameObjects.add(it) }

    gameObjects.forEach { it.update() }

    backgroundMesh.draw(camera)
    gameObjects.forEach { it.draw( camera, *lights) }
  }
}
