package sh.lem.ccholo.objects.object3d

import dan200.computercraft.api.lua.IArguments
import dan200.computercraft.api.lua.LuaFunction
import net.minecraft.world.phys.Vec3
import sh.lem.ccholo.peripheral.HologramPeripheral
import sh.lem.ccholo.util.getVec3
import sh.lem.ccholo.util.getVec3Table

/**
 * Wrapper for a real [Origin3d] that is created for each Hologram peripheral, to pass down a reference to the world.
 * This is necessary because IComputerAccess is not available when the object is serialised from a global scope.
 */
data class WrappedOrigin3d(
  val hologram: HologramPeripheral,
  val origin3d: Origin3d,
) {
  /**
   * Create a new 3D canvas centred at the world origin (0, 0, 0), the hologram peripheral (if `true` is passed), or the
   * specified coordinates (as a table, or three separate number arguments).
   *
   * function():ObjectRoot3d -- Create a 3D canvas centred at the world origin.
   * function(centre:boolean):ObjectRoot3d -- Create a 3D canvas centred at the hologram peripheral.
   * function(pos:table):ObjectRoot3d -- Create a 3D canvas centred at the coordinates specified in the table.
   * function(x:number,y:number,z:number):ObjectRoot3d -- Create a 3D canvas centred at the coordinates specified.
   */
  @LuaFunction
  fun create(args: IArguments): ObjectRoot3d {
    val canvasRoot = origin3d.canvasRootServer
    val blockEntity = hologram.blockEntity
    val level = blockEntity.level
      ?: throw IllegalStateException("Hologram block entity is not loaded on the server?")

    val pos = when (args.count()) {
      0 -> Vec3.ZERO // No arguments -> Centre on world origin
      1 -> if (args.get(0) == true) {
        // `true` -> Centre on the hologram peripheral
        with (blockEntity.blockPos) {
          Vec3(x + 0.5, y + 0.5, z + 0.5)
        }
      } else {
        // Table -> get coordinates from table
        args.getVec3Table(0)
      }
      3 -> args.getVec3(0) // Three numbers -> get coordinates from arguments
      else -> throw IllegalArgumentException("Expected 0, 1, or 3 arguments")
    }

    val root = ObjectRoot3d(canvasRoot.newObjectId(), origin3d.id, canvasRoot)
    root.init(level, pos)
    canvasRoot.add(root)
    return root
  }
}
