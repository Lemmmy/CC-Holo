package sh.lem.ccholo.objects.object2d

import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import sh.lem.ccholo.canvas.CanvasRoot.Companion.BASE_FRAME_HEIGHT
import sh.lem.ccholo.canvas.CanvasRoot.Companion.BASE_FRAME_WIDTH
import sh.lem.ccholo.canvas.CanvasRoot.Companion.MAX_FRAME_WIDTH

/**
 * A group for 2D objects with a given size
 */
interface Frame2d : Group2d {
  open val width: Int
    get() = BASE_FRAME_WIDTH
  open val height: Int
    get() = BASE_FRAME_HEIGHT

  /**
   * function():number, number -- Get the size of this frame.
   */
  @LuaFunction
  fun getSize(): MethodResult = MethodResult.of(width, height)
}

/**
 * A frame with resizable dimensions
 */
interface ResizableFrame2d : Frame2d {
  /**
   * function(width:number, height:number) -- Set the size of this frame.
   */
  @LuaFunction
  fun setSize(width: Int, height: Int)
}
