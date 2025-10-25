package sh.lem.ccholo.objects.object3d

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import sh.lem.ccholo.canvas.CanvasRoot
import sh.lem.ccholo.canvas.CanvasRootClient
import sh.lem.ccholo.canvas.CanvasRootServer
import sh.lem.ccholo.objects.BaseObject
import sh.lem.ccholo.objects.ObjectRegistry.ORIGIN_3D
import sh.lem.ccholo.util.readOptResourceLocation
import sh.lem.ccholo.util.readVec3
import sh.lem.ccholo.util.writeOptResourceLocation
import sh.lem.ccholo.util.writeVec3

class ObjectRoot3d(
  id: Int,
  parent: Int,
  canvasRoot: CanvasRoot,
) : BaseObject(id, parent, ORIGIN_3D, canvasRoot), Group3d {
  internal var origin: Vec3 = Vec3.ZERO
  internal var levelKey = ResourceLocation.tryBuild("minecraft", "overworld")

  override val canvasRootClient: CanvasRootClient
    get() = canvasRoot as? CanvasRootClient ?: error("Tried to access client canvas from server context")
  override val canvasRootServer: CanvasRootServer
    get() = canvasRoot as? CanvasRootServer ?: error("Tried to access server canvas from client context")

  fun init(level: Level, origin: Vec3) {
    val levelKey = level.dimension().location()

    if (origin != this.origin || levelKey != this.levelKey) {
      this.origin = origin
      this.levelKey = levelKey
      setDirty()
    }
  }

  // TODO: recenter() (will need to wrap ObjectRoot3d in WrappedOrigin3d to pass block entity around)

  override fun readInitial(buf: FriendlyByteBuf) {
    origin = buf.readVec3()
    levelKey = buf.readOptResourceLocation()
  }

  override fun writeInitial(buf: FriendlyByteBuf) {
    buf.writeVec3(origin)
    buf.writeOptResourceLocation(levelKey)
  }
}
