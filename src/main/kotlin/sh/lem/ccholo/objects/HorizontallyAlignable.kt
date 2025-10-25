package sh.lem.ccholo.objects

import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaFunction

interface HorizontallyAlignable {
  var horizontalAlignment: Alignment

  /**
   * function():string -- Get the horizontal alignment for this object.
   */
  @LuaFunction
  fun getHorizontalAlignment() = horizontalAlignment.name.lowercase()

  /**
   * function(alignment:string) -- Set the horizontal alignment for this object. Must be "left", "center"/"centre",
   * "right".
   */
  @LuaFunction
  fun setHorizontalAlignment(alignment: String) {
    horizontalAlignment = when (alignment.lowercase()) {
      "left" -> Alignment.LEFT
      "center", "centre" -> Alignment.CENTER
      "right" -> Alignment.RIGHT
      else -> throw LuaException("Invalid alignment: $alignment")
    }
  }

  enum class Alignment {
    LEFT, CENTER, RIGHT
  }
}
