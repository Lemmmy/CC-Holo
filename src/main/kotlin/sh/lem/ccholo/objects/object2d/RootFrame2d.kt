package sh.lem.ccholo.objects.object2d

import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import sh.lem.ccholo.canvas.CanvasRoot.Companion.BASE_FRAME_HEIGHT
import sh.lem.ccholo.canvas.CanvasRoot.Companion.BASE_FRAME_WIDTH

interface RootFrame2d : Frame2d {
  override val width: Int
    get() = canvasRoot.guiScaledWidth.takeIf { it > 0 } ?: BASE_FRAME_WIDTH
  override val height: Int
    get() = canvasRoot.guiScaledHeight.takeIf { it > 0 } ?: BASE_FRAME_HEIGHT

  /**
   * function():number, number, number, number, number -- Get the size of the player's screen, the gui scaled size
   * (the size used for the frame), and their gui scale.
   */
  @LuaFunction
  fun getScreenSize(): MethodResult = MethodResult.of(
    canvasRoot.screenWidth,
    canvasRoot.screenHeight,
    canvasRoot.guiScaledWidth,
    canvasRoot.guiScaledHeight,
    canvasRoot.guiScale,
  )
}
