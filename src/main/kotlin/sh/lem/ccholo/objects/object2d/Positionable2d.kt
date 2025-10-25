package sh.lem.ccholo.objects.object2d

import dan200.computercraft.api.lua.IArguments
import dan200.computercraft.api.lua.LuaFunction
import net.minecraft.world.phys.Vec2
import sh.lem.ccholo.util.getVec2
import sh.lem.ccholo.util.toResult

/**
 * An object which can be positioned in 2D.
 */
interface Positionable2d {
  var position: Vec2

  /**
   * function():number, number -- Get the position for this object.
   */
  @LuaFunction
  fun getPosition() = position.toResult()

  /**
   * function(number, number) -- Set the position for this object.
   */
  @LuaFunction
  fun setPosition(args: IArguments) {
    position = args.getVec2()
  }
}
