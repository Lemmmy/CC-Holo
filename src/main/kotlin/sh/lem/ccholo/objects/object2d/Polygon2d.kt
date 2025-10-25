package sh.lem.ccholo.objects.object2d

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.phys.Vec2
import sh.lem.ccholo.canvas.CanvasRoot
import sh.lem.ccholo.objects.ColourableObject
import sh.lem.ccholo.objects.ObjectRegistry.POLYGON_2D

open class Polygon2d(
  id: Int,
  parent: Int,
  canvasRoot: CanvasRoot,
  type: Byte = POLYGON_2D,
) : ColourableObject(id, parent, type, canvasRoot), MultiPointResizable2d {
  override var points = mutableListOf<Vec2>()
  override val vertices: Int
    get() = points.size

  override fun readInitial(buf: FriendlyByteBuf) {
    super.readInitial(buf)
    readPoints(buf)
  }

  override fun writeInitial(buf: FriendlyByteBuf) {
    super.writeInitial(buf)
    writePoints(buf)
  }

  override fun addVertex(idx: Int, point: Vec2) {
    if (idx == points.size) {
      points.add(point)
    } else {
      points.add(idx, point)
    }
    setDirty()
  }

  override fun removeVertex(idx: Int) {
    points.removeAt(idx)
    setDirty()
  }
}
