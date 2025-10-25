package sh.lem.ccholo.objects

import dan200.computercraft.api.lua.LuaFunction
import net.minecraft.network.FriendlyByteBuf
import sh.lem.ccholo.canvas.CanvasRoot
import sh.lem.ccholo.canvas.CanvasRootClient
import sh.lem.ccholo.canvas.CanvasRootServer
import sh.lem.ccholo.util.Dirtyable

abstract class BaseObject(
  val id: Int,
  val parent: Int,
  val type: Byte,
  val canvasRoot: CanvasRoot
) : Dirtyable {
  private var dirty = true

  open val canvasRootClient: CanvasRootClient
    get() = canvasRoot as? CanvasRootClient ?: error("Tried to access client canvas from server context")
  open val canvasRootServer: CanvasRootServer
    get() = canvasRoot as? CanvasRootServer ?: error("Tried to access server canvas from client context")

  override fun pollDirty(): Boolean {
    val value = dirty
    dirty = false
    return value
  }

  override fun setDirty() {
    dirty = true
  }

  /**
   * Read the initial data for this object.
   *
   * @param buf The buffer to read from.
   */
  abstract fun readInitial(buf: FriendlyByteBuf)

  /**
   * Write the initial buffer for this object.
   *
   * @param buf The buffer to write to.
   */
  abstract fun writeInitial(buf: FriendlyByteBuf)

  /**
   * function() -- Remove this object from the canvas.
   */
  @LuaFunction
  fun remove() {
    canvasRootServer.remove(this)
  }

  fun interface Factory {
    fun create(id: Int, parent: Int, root: CanvasRoot): BaseObject
  }

  companion object {
    val SORTING_ORDER: Comparator<BaseObject> =
      Comparator.comparingInt { a: BaseObject -> a.id }
  }
}
