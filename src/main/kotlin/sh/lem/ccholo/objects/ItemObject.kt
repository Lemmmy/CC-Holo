package sh.lem.ccholo.objects

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.exceptions.CommandSyntaxException
import dan200.computercraft.api.lua.IArguments
import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.arguments.item.ItemParser
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.StringRepresentable
import net.minecraft.world.flag.FeatureFlagSet
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import net.minecraftforge.registries.ForgeRegistries
import sh.lem.ccholo.CCHolo
import sh.lem.ccholo.util.getItem

interface ItemObject {
  var item: Item?
  var nbt: CompoundTag?
  var displayContext: ItemDisplayContext
  var forceUnlit: Boolean

  /**
   * function():string -- Get the item for this object.
   */
  @LuaFunction
  fun getItem() =
    item?.let { ForgeRegistries.ITEMS.getKey(it)?.toString() } ?: throw LuaException("Invalid item")

  /**
   * function():string -- Get the NBT for this object.
   */
  @LuaFunction
  fun getNBT(): MethodResult = nbt?.let { nbt ->
    try {
      MethodResult.of(convertNbtToMap(nbt))
    } catch (e: Exception) {
      MethodResult.of(null, "Failed to convert NBT: ${e.message}")
    }
  } ?: MethodResult.of(null)

  /**
   * function(string) -- Set the item for this object.
   */
  @LuaFunction
  fun setItem(args: IArguments) {
    args.getItem(0).let {
      item = it.first
      nbt = it.second
    }
  }

  /**
   * function():string -- Return the display context for this item.
   */
  @LuaFunction
  fun getDisplayContext(): MethodResult = MethodResult.of(displayContext.name)

  /**
   * function(displayContext:string) -- Set the display context for this item. Vanilla modes are "none", "gui",
   *   "ground", "head", "fixed", "thirdperson_lefthand", "thirdperson_righthand", "firstperson_lefthand",
   *   "firstperson_righthand".
   */
  @LuaFunction
  fun setDisplayContext(args: IArguments) {
    val ctxValue = args.getString(0)
    displayContext = DISPLAY_CONTEXT_CODEC.byName(ctxValue)
      ?: throw IllegalArgumentException("Invalid display context: $ctxValue")
  }

  /**
   * function():boolean - Return whether this item attempts to be rendered unlit.
   */
  @LuaFunction
  fun getForcedUnlit() = forceUnlit

  /**
   * function(unlit:boolean) - Sets whether this item attempts to be rendered unlit.
   */
  @LuaFunction
  fun setForcedUnlit(args: IArguments) {
    forceUnlit = args.getBoolean(0)
  }

  companion object {
    private val errorLoggedInstances = IntOpenHashSet()
    private val DISPLAY_CONTEXT_CODEC = StringRepresentable.fromEnum(ItemDisplayContext::values)

    @Throws(CommandSyntaxException::class)
    fun parseObject(
      registryAccess: HolderLookup.Provider,
      enabledFeatures: FeatureFlagSet,
      input: String
    ): Pair<Item, CompoundTag?> {
      val reader = StringReader(input)
      val ctx = CommandBuildContext.simple(registryAccess, enabledFeatures)
      val res = ItemParser.parseForItem(ctx.holderLookup(Registries.ITEM), reader)
      return Pair(res.item().get(), res.nbt())
    }

    private fun convertNbtToMap(tag: Any): Map<String, Any?> = when (tag) {
      is CompoundTag -> {
        val map = mutableMapOf<String, Any?>()
        tag.allKeys.forEach { key ->
          map[key] = convertNbtValue(tag.get(key))
        }
        map
      }
      else -> emptyMap()
    }

    private fun convertNbtValue(tag: Any?): Any? = when (tag) {
      is CompoundTag -> convertNbtToMap(tag)
      is net.minecraft.nbt.ListTag -> tag.map { convertNbtValue(it) }
      is net.minecraft.nbt.ByteArrayTag -> tag.asByteArray.toList()
      is net.minecraft.nbt.IntArrayTag -> tag.asIntArray.toList()
      is net.minecraft.nbt.LongArrayTag -> tag.asLongArray.toList()
      is net.minecraft.nbt.NumericTag -> tag.asNumber
      is net.minecraft.nbt.StringTag -> tag.asString
      is net.minecraft.nbt.EndTag -> null
      else -> tag?.toString()
    }

    internal fun tryMakeStack(item: Item, nbt: CompoundTag?): ItemStack {
      try {
        return ItemStack(item).also {
          it.tag = nbt?.copy()
        }
      } catch (e: Exception) {
        // avoid spamming logs with rendering errors
        if (errorLoggedInstances.size > 32) return ItemStack.EMPTY

        // don't short-circuit
        val itemUnlogged = errorLoggedInstances.add(item.hashCode())
        val nbtUnlogged = errorLoggedInstances.add(nbt.hashCode())
        if (itemUnlogged || nbtUnlogged) CCHolo.log.error("Failed to create stack", e)
        return ItemStack.EMPTY
      }
    }
  }
}
