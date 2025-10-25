package sh.lem.ccholo.objects.object2d

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.phys.Vec2
import sh.lem.ccholo.canvas.CanvasRoot
import sh.lem.ccholo.objects.ColourableObject
import sh.lem.ccholo.objects.ObjectRegistry.DOT_2D
import sh.lem.ccholo.objects.Scalable
import sh.lem.ccholo.util.DirtyingProperty
import sh.lem.ccholo.util.readVec2
import sh.lem.ccholo.util.writeVec2

class Dot2d(
  id: Int,
  parent: Int,
  canvasRoot: CanvasRoot,
) : ColourableObject(id, parent, DOT_2D, canvasRoot), Positionable2d, Scalable {
  override var position: Vec2 by DirtyingProperty(Vec2.ZERO)
  override var scale by DirtyingProperty(1.0f)

  override fun readInitial(buf: FriendlyByteBuf) {
    super.readInitial(buf)
    position = buf.readVec2()
    scale = buf.readFloat()
  }

  override fun writeInitial(buf: FriendlyByteBuf) {
    super.writeInitial(buf)
    buf.writeVec2(position)
    buf.writeFloat(scale)
  }
}
