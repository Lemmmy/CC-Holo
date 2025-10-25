package sh.lem.ccholo.objects.object3d

import dan200.computercraft.api.lua.IArguments
import dan200.computercraft.api.lua.LuaFunction
import net.minecraft.world.phys.Vec3
import sh.lem.ccholo.util.getVec3
import sh.lem.ccholo.util.toResult

/**
 * An object which can be positioned in 3D.
 */
interface Positionable3d {
  var position: Vec3

  /**
   * function():number, number, number -- Get the position for this object.
   */
  @LuaFunction
  fun getPosition() = position.toResult()

  /**
   * function(number, number, number) -- Set the position for this object.
   */
  @LuaFunction
  fun setPosition(args: IArguments) {
    position = args.getVec3()
  }
}
