package sh.lem.ccholo.objects.object2d

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.phys.Vec2
import sh.lem.ccholo.canvas.CanvasRoot
import sh.lem.ccholo.objects.ColourableObject
import sh.lem.ccholo.objects.ObjectRegistry.LINE_2D
import sh.lem.ccholo.objects.Scalable
import sh.lem.ccholo.util.DirtyingProperty

class Line2d(
  id: Int,
  parent: Int,
  canvasRoot: CanvasRoot,
) : ColourableObject(id, parent, LINE_2D, canvasRoot), Scalable, MultiPoint2d {
  override var points = mutableListOf(Vec2.ZERO, Vec2.ZERO)
  override val vertices: Int
    get() = 2

  val start: Vec2
    get() = points[0]
  val end: Vec2
    get() = points[1]

  /** Line thickness */
  override var scale by DirtyingProperty(1.0f)

  override fun readInitial(buf: FriendlyByteBuf) {
    super.readInitial(buf)
    readPoints(buf)
    scale = buf.readFloat()
  }

  override fun writeInitial(buf: FriendlyByteBuf) {
    super.writeInitial(buf)
    writePoints(buf)
    buf.writeFloat(scale)
  }
}
