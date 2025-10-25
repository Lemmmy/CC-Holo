package sh.lem.ccholo.objects.object2d

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.phys.Vec2
import sh.lem.ccholo.canvas.CanvasRoot
import sh.lem.ccholo.canvas.CanvasRootClient
import sh.lem.ccholo.canvas.CanvasRootServer
import sh.lem.ccholo.objects.BaseObject
import sh.lem.ccholo.objects.ObjectRegistry.GROUP_2D
import sh.lem.ccholo.util.DirtyingProperty
import sh.lem.ccholo.util.readVec2
import sh.lem.ccholo.util.writeVec2

class ObjectGroup2d(
  id: Int,
  parent: Int,
  canvasRoot: CanvasRoot,
) : BaseObject(id, parent, GROUP_2D, canvasRoot), Group2d, Positionable2d {
  override var position: Vec2 by DirtyingProperty(Vec2.ZERO)

  override val canvasRootClient: CanvasRootClient
    get() = canvasRoot as? CanvasRootClient ?: error("Tried to access client canvas from server context")
  override val canvasRootServer: CanvasRootServer
    get() = canvasRoot as? CanvasRootServer ?: error("Tried to access server canvas from client context")

  override fun readInitial(buf: FriendlyByteBuf) {
    position = buf.readVec2()
  }

  override fun writeInitial(buf: FriendlyByteBuf) {
    buf.writeVec2(position)
  }
}
