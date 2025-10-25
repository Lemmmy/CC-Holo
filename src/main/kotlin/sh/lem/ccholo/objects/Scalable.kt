package sh.lem.ccholo.objects

import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult

interface Scalable {
  var scale: Float

  /**
   * function():number -- Get the scale for this object.
   */
  @LuaFunction
  fun getScale(): MethodResult = MethodResult.of(scale) // wrap in MethodResult to avoid signature conflict

  /**
   * function(number) -- Set the scale for this object.
   */
  @LuaFunction
  fun setScale(scale: Double) {
    val newScale = scale.toFloat()
    if (newScale <= 0) throw LuaException("Scale must be > 0")
    this.scale = newScale
  }
}
