package sh.lem.ccholo.util

import com.mojang.brigadier.exceptions.CommandSyntaxException
import dan200.computercraft.api.lua.IArguments
import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaValues
import dan200.computercraft.api.lua.LuaValues.badArgumentOf
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.Item
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import net.minecraftforge.server.ServerLifecycleHooks
import sh.lem.ccholo.CCHolo
import sh.lem.ccholo.objects.ItemObject
import java.nio.charset.StandardCharsets

fun IArguments.getVec2(startIndex: Int = 0): Vec2 =
  getVec2Nullable(startIndex) ?: throw badArgumentOf(this, startIndex, "number")

fun IArguments.getVec2Nullable(startIndex: Int = 0): Vec2? =
  if (count() < startIndex || count() == startIndex && this[0] == null) {
    null
  } else {
    Vec2(getDouble(startIndex).toFloat(), getDouble(startIndex + 1).toFloat())
  }

fun IArguments.getVec2Table(index: Int = 0): Vec2 {
  val point = getTable(index)
  return if (point.containsKey("x")) {
    Vec2(point.getFiniteDouble("x").toFloat(), point.getFiniteDouble("y").toFloat())
  } else {
    Vec2(point.getFiniteDouble(1.0).toFloat(), point.getFiniteDouble(2.0).toFloat())
  }
}

fun IArguments.getVec2Tables(optArgCount: Int = 0): MutableList<Vec2> {
  val points = mutableListOf<Vec2>()
  var i = 0
  while (i < count()) {
    val arg = get(i)
    if (i >= count() - optArgCount && arg !is Map<*, *>) {
      break
    } else {
      points.add(getVec2Table(i))
    }
    i++
  }
  return points
}

fun IArguments.getVec3Nullable(startIndex: Int = 0): Vec3? =
  if (count() < startIndex || count() == startIndex && this[0] == null) {
    null
  } else {
    Vec3(getDouble(startIndex), getDouble(startIndex + 1), getDouble(startIndex + 2))
  }

fun IArguments.getVec3(startIndex: Int = 0): Vec3 =
  getVec3Nullable(startIndex) ?: throw badArgumentOf(this, startIndex, "number")

fun IArguments.getVec3Table(index: Int = 0): Vec3 {
  val point = getTable(index)
  return if (point.containsKey("x")) {
    Vec3(point.getFiniteDouble("x"), point.getFiniteDouble("y"), point.getFiniteDouble("z"))
  } else {
    Vec3(point.getFiniteDouble(1), point.getFiniteDouble(2), point.getFiniteDouble(3))
  }
}

fun assertIntBetweenImpl(value: Int, min: Int, max: Int, message: String): Int {
  if (value < min || value > max) {
    throw LuaException(String.format(message, "between $min and $max"))
  }
  return value
}

fun IArguments.assertIntBetween(index: Int, min: Int, max: Int, message: String) =
  assertIntBetweenImpl(getInt(index), min, max, message)

fun assertDoubleBetween(value: Double, min: Double, max: Double, message: String): Double {
  if (value < min || value > max || value.isNaN()) {
    throw LuaException(String.format(message, "between $min and $max"))
  }
  return value
}

fun IArguments.assertDoubleBetween(index: Int, min: Double, max: Double, message: String) =
  assertDoubleBetween(getFiniteDouble(index), min, max, message)

fun IArguments.assertStringLength(index: Int, min: Int, max: Int,
                                  message: String = "string length out of bounds (%s)"): String {
  val value = getString(index)
  assertIntBetweenImpl(value.length, min, max, message)
  return value
}

fun IArguments.getItem(index: Int): Pair<Item, CompoundTag?> {
  val def = getString(index)

  try {
    val server = ServerLifecycleHooks.getCurrentServer()
    val registryAccess = server.registryAccess()
    val enabledFeatures = server.worldData.enabledFeatures()

    return ItemObject.parseObject(registryAccess, enabledFeatures, def)
  } catch (e: CommandSyntaxException) {
    CCHolo.log.error("Could not construct item (CommandSyntaxException) ${def}:", e)
    throw LuaException("Invalid item or NBT '${def}'")
  } catch (e: Exception) {
    CCHolo.log.error("Could not construct item ${def}:", e)
    throw LuaException("Unexpected error while parsing item")
  }
}

fun IArguments.getUtf8String(index: Int): String {
  val buf = getBytes(index)
  return StandardCharsets.UTF_8.decode(buf).toString()
}

fun IArguments.assertUtf8StringLength(index: Int, min: Int, max: Int,
                                      message: String = "string length out of bounds (%s)"): String {
  val value = getUtf8String(index)
  assertIntBetweenImpl(value.length, min, max, message)
  return value
}

private fun Map<*, *>.getFiniteDouble(key: Any): Double {
  val obj = this[key] ?: throw LuaException("Expected number for key $key, got nil")
  if (obj !is Number) throw badKey(obj, key.toString(), "number")

  val value = obj.toDouble()
  if (!value.isFinite()) throw badKey(obj, key.toString(), "number")

  return value
}

private fun badKey(obj: Any?, key: String, expected: String) =
  LuaException("Expected " + expected + " for key " + key + ", got " + LuaValues.getType(obj))
