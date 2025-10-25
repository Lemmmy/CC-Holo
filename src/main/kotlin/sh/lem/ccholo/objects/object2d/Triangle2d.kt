package sh.lem.ccholo.objects.object2d

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.phys.Vec2
import sh.lem.ccholo.canvas.CanvasRoot
import sh.lem.ccholo.objects.ColourableObject
import sh.lem.ccholo.objects.ObjectRegistry.TRIANGLE_2D

class Triangle2d(
  id: Int,
  parent: Int,
  canvasRoot: CanvasRoot,
) : ColourableObject(id, parent, TRIANGLE_2D, canvasRoot), MultiPoint2d {
  override var points = mutableListOf(Vec2.ZERO, Vec2.ZERO, Vec2.ZERO)
  override val vertices: Int
    get() = 3

  override fun readInitial(buf: FriendlyByteBuf) {
    super.readInitial(buf)
    readPoints(buf)
  }

  override fun writeInitial(buf: FriendlyByteBuf) {
    super.writeInitial(buf)
    writePoints(buf)
  }
}
