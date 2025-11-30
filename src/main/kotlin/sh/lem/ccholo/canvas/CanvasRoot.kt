package sh.lem.ccholo.canvas

import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import it.unimi.dsi.fastutil.ints.IntSet
import sh.lem.ccholo.objects.BaseObject
import sh.lem.ccholo.util.BaseDirtyable
import sh.lem.ccholo.util.DirtyingProperty

abstract class CanvasRoot: BaseDirtyable {
  override var dirty = true

  protected val objects: Int2ObjectMap<BaseObject> = Int2ObjectOpenHashMap()
  protected val childrenOf: Int2ObjectMap<IntSet> = Int2ObjectOpenHashMap()

  /** Whether the player is currently in 'capture' mode (all keyboard & mouse inputs are being captured modally) */
  var capturing = false
  var capturingMouseMove = false
  var hidingMouse = false

  /** Defined specific key captures that happen globally during gameplay. GLFW keycodes */
  val keyCaptures = IntOpenHashSet()

  /** Attempt to track the player's screen size */
  protected var screenSizeDirty = false
  var screenWidth by DirtyingProperty(0) { _, _, _ -> screenSizeDirty = true }
  var screenHeight by DirtyingProperty(0) { _, _, _ -> screenSizeDirty = true }
  var guiScaledWidth by DirtyingProperty(0) { _, _, _ -> screenSizeDirty = true }
  var guiScaledHeight by DirtyingProperty(0) { _, _, _ -> screenSizeDirty = true }
  var guiScale by DirtyingProperty(0.0) { _, _, _ -> screenSizeDirty = true }

  abstract fun makeChildSet(): IntSet

  open fun reset() {
    objects.clear()
    childrenOf.clear()
    childrenOf.put(ID_2D, makeChildSet())
    childrenOf.put(ID_3D, makeChildSet())

    capturing = false
    capturingMouseMove = false
    hidingMouse = false

    keyCaptures.clear()
  }

  fun pollScreenSizeDirty(): Boolean {
    val value = screenSizeDirty
    screenSizeDirty = false
    return value
  }

  companion object {
    const val ID_2D = 0
    const val ID_3D = 1

    const val BASE_FRAME_WIDTH = 512
    const val BASE_FRAME_HEIGHT = 512 / 16 * 9
    const val MAX_FRAME_WIDTH = 8192
    const val MAX_FRAME_HEIGHT = 8192 // TODO: does this depend on GPU in 2025?

    // In practice, it's GLFW_KEY_LAST (GLFW_KEY_MENU=348) but we can't access that on the server, so use a safe value
    const val MAX_KEY_CODE = 512
  }
}
