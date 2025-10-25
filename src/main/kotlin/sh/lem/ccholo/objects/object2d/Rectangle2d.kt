package sh.lem.ccholo.objects.object2d

import dan200.computercraft.api.lua.IArguments
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.phys.Vec2
import sh.lem.ccholo.canvas.CanvasRoot
import sh.lem.ccholo.objects.ColourableObject
import sh.lem.ccholo.objects.ObjectRegistry.RECTANGLE_2D
import sh.lem.ccholo.util.DirtyingProperty
import sh.lem.ccholo.util.getVec2
import sh.lem.ccholo.util.readVec2
import sh.lem.ccholo.util.writeVec2

class Rectangle2d(
  id: Int,
  parent: Int,
  canvasRoot: CanvasRoot,
) : ColourableObject(id, parent, RECTANGLE_2D, canvasRoot), Positionable2d {
  override var position: Vec2 by DirtyingProperty(Vec2.ZERO)

  internal var width: Float = 0.0f
  internal var height: Float = 0.0f

  var size
    get() = Vec2(width, height)
    set(value) {
      if (value != size) {
        width = value.x
        height = value.y
        setDirty()
      }
    }

  /**
   * function():number, number -- Get the size of this rectangle.
   */
  @LuaFunction
  fun getSize(): MethodResult = MethodResult.of(width, height)

  /**
   * function(number, number) -- Set the size of this rectangle.
   */
  @LuaFunction
  fun setSize(args: IArguments) {
    size = args.getVec2()
  }

  override fun readInitial(buf: FriendlyByteBuf) {
    super.readInitial(buf)
    position = buf.readVec2()
    width = buf.readFloat()
    height = buf.readFloat()
  }

  override fun writeInitial(buf: FriendlyByteBuf) {
    super.writeInitial(buf)
    buf.writeVec2(position)
    buf.writeFloat(width)
    buf.writeFloat(height)
  }
}
