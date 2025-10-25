package sh.lem.ccholo.objects.object3d

import dan200.computercraft.api.lua.IArguments
import dan200.computercraft.api.lua.LuaFunction
import net.minecraft.world.phys.Vec3
import sh.lem.ccholo.util.getVec3Nullable
import sh.lem.ccholo.util.toResult

/**
 * An object which can be rotated in 3D.
 */
interface Rotatable3d {
  var rotation: Vec3?

  /**
   * function():nil|number, number, number -- Get the rotation for this object, or nil if it faces the player.
   */
  @LuaFunction
  fun getRotation() = rotation.toResult()

  /**
   * function([x:number, y:number, z:number]) -- Set the rotation for this object, passing nothing if it should face
   *   the player.
   */
  @LuaFunction
  fun setRotation(args: IArguments) {
    rotation = args.getVec3Nullable()
  }
}
