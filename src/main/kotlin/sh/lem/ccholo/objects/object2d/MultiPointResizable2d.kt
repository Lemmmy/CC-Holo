package sh.lem.ccholo.objects.object2d

import dan200.computercraft.api.lua.IArguments
import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaFunction
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.FriendlyByteBuf.limitValue
import net.minecraft.world.phys.Vec2
import sh.lem.ccholo.util.assertIntBetween
import sh.lem.ccholo.util.getVec2
import sh.lem.ccholo.util.readVec2

/**
 * A polygon for which you can set multiple points and adjust the number of points dynamically.
 */
interface MultiPointResizable2d: MultiPoint2d {
  fun addVertex(idx: Int, point: Vec2)
  fun removeVertex(idx: Int)

  override fun readPoints(buf: FriendlyByteBuf) {
    points = buf.readCollection(limitValue({ mutableListOf() }, MAX_SIZE), FriendlyByteBuf::readVec2)
  }

  /**
   * function():number -- Get the number of vertices on this object.
   */
  @LuaFunction
  fun getPointCount() = vertices

  /**
   * function(idx:int) -- Remove the specified vertex of this object.
   */
  @LuaFunction
  fun removePoint(args: IArguments) {
    val idx = args.assertIntBetween(0, 1, vertices, "Index out of range (%s)")
    removeVertex(idx)
  }

  /**
   * function([idx:int, ]x:number, y:number) -- Add a specified vertex to this object.
   */
  @LuaFunction
  fun insertPoint(args: IArguments) {
    if (vertices >= MAX_SIZE) throw LuaException("Too many vertices")

    val idx: Int
    val pos: Vec2
    if (args.count() >= 3) {
      idx = args.assertIntBetween(0, 1, vertices, "Index out of range (%s)")
      pos = args.getVec2(1)
    } else {
      idx = vertices
      pos = args.getVec2(2)
    }

    addVertex(idx - 1, pos)
  }

  companion object {
    const val MAX_SIZE = 255
  }
}
