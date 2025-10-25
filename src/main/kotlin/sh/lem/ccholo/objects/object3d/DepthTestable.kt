package sh.lem.ccholo.objects.object3d

import dan200.computercraft.api.lua.LuaFunction

interface DepthTestable {
  var hasDepthTest: Boolean

  /**
   * function():boolean -- Determine whether depth testing is enabled for this object.
   */
  @LuaFunction
  fun isDepthTested() = hasDepthTest

  /**
   * function(boolean) -- Set whether depth testing is enabled for this object.
   */
  @LuaFunction
  fun setDepthTested(enabled: Boolean) {
    hasDepthTest = enabled
  }
}
