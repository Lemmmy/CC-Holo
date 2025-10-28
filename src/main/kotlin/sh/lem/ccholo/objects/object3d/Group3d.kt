package sh.lem.ccholo.objects.object3d

import dan200.computercraft.api.lua.IArguments
import dan200.computercraft.api.lua.LuaFunction
import net.minecraft.world.phys.Vec3
import sh.lem.ccholo.objects.DEFAULT_COLOUR
import sh.lem.ccholo.objects.ObjectGroup
import sh.lem.ccholo.util.getItem
import sh.lem.ccholo.util.getVec3

/**
 * A group for 3D objects
 */
interface Group3d : ObjectGroup {
  /**
   * function(x:number, y:number, z:number[, width:number, height:number, depth:number][, color:number]):Box3d
   * -- Create a new box.
   */
  @LuaFunction
  fun addBox(args: IArguments): Box3d {
    val pos = args.getVec3(0)
    val colour = args.optInt(if (args.count() <= 4) 3 else 6, DEFAULT_COLOUR.toInt())
    val size = if (args.count() <= 4) Vec3(1.0, 1.0, 1.0) else args.getVec3(3)

    val box = Box3d(canvasRootServer.newObjectId(), id, canvasRootServer)
    box.position = pos
    box.colour = colour
    box.size = size

    canvasRootServer.add(box)
    return box
  }

  /**
   * function(x:number, y:number, z:number, item:string[, scale:number]):Item3d -- Create an item model.
   */
  @LuaFunction
  fun addItem(args: IArguments): Item3d {
    val pos = args.getVec3(0)
    val (item, nbt) = args.getItem(3)
    val scale = args.optDouble(4, 1.0).toFloat()

    val item3d = Item3d(canvasRootServer.newObjectId(), id, canvasRootServer)
    item3d.position = pos
    item3d.scale = scale
    item3d.item = item
    item3d.nbt = nbt

    canvasRootServer.add(item3d)
    return item3d
  }

  /**
   * function(function(x:number, y:number, z:number):ObjectFrame3d -- Create a new frame to put 2D objects in.
   */
  @LuaFunction
  fun addFrame(args: IArguments): ObjectFrame3d {
    val pos = args.getVec3(0)

    val frame3d = ObjectFrame3d(canvasRootServer.newObjectId(), id, canvasRootServer)
    frame3d.position = pos

    canvasRootServer.add(frame3d)
    return frame3d
  }
}
