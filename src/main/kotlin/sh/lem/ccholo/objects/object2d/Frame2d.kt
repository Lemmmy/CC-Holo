package sh.lem.ccholo.objects.object2d

import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import sh.lem.ccholo.canvas.CanvasRoot.Companion.BASE_HEIGHT
import sh.lem.ccholo.canvas.CanvasRoot.Companion.BASE_WIDTH

/**
 * A group for 2D objects with a given size
 */
interface Frame2d : Group2d {
  val width: Int
    get() = BASE_WIDTH
  val height: Int
    get() = BASE_HEIGHT

  /**
   * function():number, number -- Get the size of this frame.
   */
  @LuaFunction
  fun getSize(): MethodResult = MethodResult.of(width, height)
}
