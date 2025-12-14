package sh.lem.ccholo.objects.object3d

import dan200.computercraft.api.lua.IArguments
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.phys.Vec3
import sh.lem.ccholo.canvas.CanvasRoot
import sh.lem.ccholo.objects.ColourableObject
import sh.lem.ccholo.objects.ObjectRegistry.BOX_3D
import sh.lem.ccholo.util.*

class Box3d(
  id: Int,
  parent: Int,
  canvasRoot: CanvasRoot,
) : ColourableObject(id, parent, BOX_3D, canvasRoot), Positionable3d, Rotatable3d, DepthTestable {
  override var position: Vec3 by DirtyingProperty(Vec3.ZERO)
  override var rotation: Vec3? by DirtyingProperty(Vec3.ZERO)
  override var hasDepthTest by DirtyingProperty(true)

  internal var width: Double = 0.0
  internal var height: Double = 0.0
  internal var depth: Double = 0.0

  var size
    get() = Vec3(width, height, depth)
    set(value) {
      if (value != size) {
        width = value.x
        height = value.y
        depth = value.z
        setDirty()
      }
    }

  /**
   * function():number, number, number -- Get the size of this box.
   */
  @LuaFunction
  fun getSize(): MethodResult = MethodResult.of(width, height, depth)

  /**
   * function(number, number, number) -- Set the size of this box.
   */
  @LuaFunction
  fun setSize(args: IArguments) {
    size = args.getVec3()
  }

  override fun readInitial(buf: FriendlyByteBuf) {
    super.readInitial(buf)
    position = buf.readVec3()
    rotation = buf.readOptVec3()
    width = buf.readDouble()
    height = buf.readDouble()
    depth = buf.readDouble()
    hasDepthTest = buf.readBoolean()
  }

  override fun writeInitial(buf: FriendlyByteBuf) {
    super.writeInitial(buf)
    buf.writeVec3(position)
    buf.writeOptVec3(rotation)
    buf.writeDouble(width)
    buf.writeDouble(height)
    buf.writeDouble(depth)
    buf.writeBoolean(hasDepthTest)
  }
}
