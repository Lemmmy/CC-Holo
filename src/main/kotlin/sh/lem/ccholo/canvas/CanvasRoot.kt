package sh.lem.ccholo.canvas

import it.unimi.dsi.fastutil.ints.IntOpenHashSet

abstract class CanvasRoot {
  /** Whether the player is currently in 'capture' mode (all keyboard & mouse inputs are being captured modally) */
  var capturing = false
  var capturingMouseMove = false

  /** Defined specific key captures that happen globally during gameplay. GLFW keycodes */
  val keyCaptures = IntOpenHashSet()

  companion object {
    const val ID_2D = 0
    const val ID_3D = 1

    // TODO: Configurable framebuffer scale
    const val WIDTH = 512
    const val HEIGHT = 512 / 16 * 9

    // In practice, it's GLFW_KEY_LAST (GLFW_KEY_MENU=348) but we can't access that on the server, so use a safe value
    const val MAX_KEY_CODE = 512
  }
}
