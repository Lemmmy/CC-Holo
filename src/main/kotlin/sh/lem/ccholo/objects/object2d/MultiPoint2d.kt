package sh.lem.ccholo.objects.object2d

import dan200.computercraft.api.lua.IArguments
import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.phys.Vec2
import sh.lem.ccholo.util.*

/**
 * A polygon for which you can set multiple points.
 */
interface MultiPoint2d: Dirtyable {
  var points: MutableList<Vec2>
  val vertices: Int

  open fun getVertex(idx: Int): Vec2 =
    if (idx in 0 until points.size) {
      points[idx]
    } else {
      throw LuaException("Index out of range (%s)")
    }

  open fun setVertex(idx: Int, point: Vec2) {
    if (idx in 0 until points.size && points[idx] != point) {
      points[idx] = point
      setDirty()
    }
  }

  fun readPoints(buf: FriendlyByteBuf) {
    points = buf.readList(FriendlyByteBuf::readVec2)
  }

  fun writePoints(buf: FriendlyByteBuf) {
    buf.writeCollection(points, FriendlyByteBuf::writeVec2)
  }

  /**
   * function(idx:int):number, number -- Get the specified vertex of this object.
   */
  @LuaFunction
  fun getPoint(args: IArguments): MethodResult {
    val idx = args.assertIntBetween(0, 1, vertices, "Index out of range (%s)")
    return getVertex(idx).toResult()
  }

  /**
   * function(idx:int, x:number, y:number) -- Set the specified vertex of this object.
   */
  @LuaFunction
  fun setPoint(args: IArguments) {
    val idx = args.assertIntBetween(0, 1, vertices, "Index out of range (%s)")
    setVertex(idx - 1, args.getVec2(1))
  }
}
