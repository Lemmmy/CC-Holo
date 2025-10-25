package sh.lem.ccholo.objects.object3d

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.phys.Vec3
import sh.lem.ccholo.canvas.CanvasRoot
import sh.lem.ccholo.canvas.CanvasRootClient
import sh.lem.ccholo.canvas.CanvasRootServer
import sh.lem.ccholo.objects.BaseObject
import sh.lem.ccholo.objects.ObjectRegistry.FRAME_3D
import sh.lem.ccholo.objects.Scalable
import sh.lem.ccholo.objects.object2d.Frame2d
import sh.lem.ccholo.util.*

class ObjectFrame3d(
  id: Int,
  parent: Int,
  canvasRoot: CanvasRoot,
) : BaseObject(id, parent, FRAME_3D, canvasRoot), Frame2d, Positionable3d, Rotatable3d, Scalable, DepthTestable {
  override var position: Vec3 by DirtyingProperty(Vec3.ZERO)
  override var rotation: Vec3? by DirtyingProperty(Vec3.ZERO)
  override var hasDepthTest by DirtyingProperty(true)
  override var scale by DirtyingProperty(DEFAULT_SCALE)

  override val canvasRootClient: CanvasRootClient
    get() = canvasRoot as? CanvasRootClient ?: error("Tried to access client canvas from server context")
  override val canvasRootServer: CanvasRootServer
    get() = canvasRoot as? CanvasRootServer ?: error("Tried to access server canvas from client context")

  override fun readInitial(buf: FriendlyByteBuf) {
    position = buf.readVec3()
    rotation = buf.readOptVec3()
    hasDepthTest = buf.readBoolean()
    scale = buf.readFloat()
  }

  override fun writeInitial(buf: FriendlyByteBuf) {
    buf.writeVec3(position)
    buf.writeOptVec3(rotation)
    buf.writeBoolean(hasDepthTest)
    buf.writeFloat(scale)
  }

  companion object {
    const val DEFAULT_SCALE = 1 / 64.0f
  }
}
