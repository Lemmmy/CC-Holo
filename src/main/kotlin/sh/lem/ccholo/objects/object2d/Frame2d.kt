package sh.lem.ccholo.objects.object2d

import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import sh.lem.ccholo.canvas.CanvasRoot.Companion.HEIGHT
import sh.lem.ccholo.canvas.CanvasRoot.Companion.WIDTH

/**
 * A group for 2D objects with a fixed size
 */
interface Frame2d : Group2d {
  val width: Int
    get() = WIDTH
  val height: Int
    get() = HEIGHT

  /**
   * function():number, number -- Get the size of this frame.
   */
  @LuaFunction
  fun getSize(): MethodResult = MethodResult.of(width, height)
}
