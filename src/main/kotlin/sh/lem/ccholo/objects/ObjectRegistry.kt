package sh.lem.ccholo.objects

import net.minecraft.network.FriendlyByteBuf
import sh.lem.ccholo.canvas.CanvasRootClient
import sh.lem.ccholo.objects.BaseObject.Factory
import sh.lem.ccholo.objects.object2d.*
import sh.lem.ccholo.objects.object3d.Box3d
import sh.lem.ccholo.objects.object3d.Item3d
import sh.lem.ccholo.objects.object3d.ObjectFrame3d
import sh.lem.ccholo.objects.object3d.ObjectRoot3d

object ObjectRegistry {
  // 2D
  const val RECTANGLE_2D: Byte = 0
  const val LINE_2D: Byte = 1
  const val DOT_2D: Byte = 2
  const val TEXT_2D: Byte = 3
  const val TRIANGLE_2D: Byte = 4
  const val POLYGON_2D: Byte = 5
  const val LINES_2D: Byte = 6
  const val ITEM_2D: Byte = 7
  const val GROUP_2D: Byte = 8
  const val FRAME_2D: Byte = 9
  const val IMAGE_2D: Byte = 10

  // 3D
  const val ORIGIN_3D: Byte = 11
  const val FRAME_3D: Byte = 12
  const val BOX_3D: Byte = 13
  const val ITEM_3D: Byte = 14
  // const val LINE_3D: Byte = 15

  private val factories = arrayOf(
    // 2D
    Factory(::Rectangle2d),
    Factory(::Line2d),
    Factory(::Dot2d),
    Factory(::Text2d),
    Factory(::Triangle2d),
    Factory(::Polygon2d),
    Factory(::Lines2d),
    Factory(::Item2d),
    Factory(::ObjectGroup2d),
    Factory(::ObjectFrame2d),
    Factory(::Image2d),

    // 3D
    Factory(::ObjectRoot3d),
    Factory(::ObjectFrame3d),
    Factory(::Box3d),
    Factory(::Item3d),
    null, // Factory(::Line3d)
  )

  fun create(id: Int, parent: Int, type: Byte): BaseObject {
    check(!(type < 0 || type >= factories.size)) { "Unknown type $type" }
    val factory = factories[type.toInt()] ?: throw IllegalStateException("No factory for type $type")

    val obj = factory.create(id, parent, CanvasRootClient)
    check(obj.type == type) { "Created object of type " + obj.type + ", expected " + type }

    return obj
  }

  fun read(buf: FriendlyByteBuf): BaseObject {
    val id = buf.readVarInt()
    val parent = buf.readVarInt()
    val type = buf.readByte()

    val obj = create(id, parent, type)
    obj.readInitial(buf)
    return obj
  }

  fun write(buf: FriendlyByteBuf, obj: BaseObject) {
    buf.writeVarInt(obj.id)
    buf.writeVarInt(obj.parent)
    buf.writeByte(obj.type.toInt())
    obj.writeInitial(buf)
  }
}
