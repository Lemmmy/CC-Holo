package sh.lem.ccholo.objects

import dan200.computercraft.api.lua.IArguments
import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaFunction
import net.minecraft.world.item.Item
import net.minecraftforge.registries.ForgeRegistries
import sh.lem.ccholo.util.getItem

interface ItemObject {
  var item: Item?

  /**
   * function():string -- Get the item for this object.
   */
  @LuaFunction
  fun getItem() =
    item?.let { ForgeRegistries.ITEMS.getKey(it)?.toString() } ?: throw LuaException("Invalid item")

  /**
   * function(string) -- Set the item for this object.
   */
  @LuaFunction
  fun setItem(args: IArguments) {
    item = args.getItem(0)
  }
}
