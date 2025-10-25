package sh.lem.ccholo.objects

import dan200.computercraft.api.lua.LuaFunction
import sh.lem.ccholo.canvas.CanvasRoot
import sh.lem.ccholo.canvas.CanvasRootClient
import sh.lem.ccholo.canvas.CanvasRootServer

/**
 * Represents a holder for [BaseObject]s.
 */
interface ObjectGroup {
  val canvasRoot: CanvasRoot

  val canvasRootClient: CanvasRootClient
    get() = canvasRoot as? CanvasRootClient ?: error("Tried to access client canvas from server context")
  val canvasRootServer: CanvasRootServer
    get() = canvasRoot as? CanvasRootServer ?: error("Tried to access server canvas from client context")

  /**
   * @return The ID for this group.
   */
  val id: Int

  /**
   * function() -- Remove all objects.
   */
  @LuaFunction
  fun clear() {
    canvasRootServer.clear(this)
  }
}
