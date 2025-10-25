package sh.lem.ccholo.peripheral

import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import dan200.computercraft.api.peripheral.IComputerAccess
import dan200.computercraft.api.peripheral.IPeripheral
import net.minecraft.server.level.ServerPlayer
import sh.lem.ccholo.CCHolo
import sh.lem.ccholo.canvas.CanvasHandlerServer
import sh.lem.ccholo.canvas.CanvasRoot.Companion.MAX_KEY_CODE
import sh.lem.ccholo.canvas.CanvasRootServer
import sh.lem.ccholo.objects.object2d.Frame2d
import sh.lem.ccholo.objects.object3d.WrappedOrigin3d
import sh.lem.ccholo.util.CCAttachedComputerSet
import sh.lem.ccholo.util.assertIntBetweenImpl
import sh.lem.ccholo.util.toResult
import sh.lem.ccholo.util.tryParseUuid
import java.util.*

class HologramPeripheral(
  val blockEntity: HologramBlockEntity,
) : IPeripheral {
  private val attachedComputers = CCAttachedComputerSet()

  /**
   * function():number,number,number -- Convenience function to get the absolute world position of the hologram
   *   peripheral
   */
  @LuaFunction
  fun getPosition() = blockEntity.blockPos.toResult()

  /**
   * function():string -- Convenience function to get the world name of the hologram peripheral
   */
  @LuaFunction
  fun getDimension(): String = blockEntity.level?.dimension()?.location().toString()

  private fun getPlayerCanvasRoot(nameOrUuid: String): Pair<ServerPlayer, CanvasRootServer> {
    val uuid = tryParseUuid(nameOrUuid)
    val name = nameOrUuid.trim().lowercase()

    val player = blockEntity.level?.players()
      ?.firstOrNull { it.gameProfile.id == uuid || it.gameProfile.name.lowercase() == name }
      as? ServerPlayer
      ?: throw LuaException("Player '$nameOrUuid' not found")

    val root = CanvasHandlerServer.getRootForPlayer(player)

    return Pair(player, root)
  }

  /**
   * function(player:string):Frame2d -- Gets the 2D canvas methods for a player.
   */
  @LuaFunction(unsafe = true)
  fun getCanvas2d(playerName: String): Frame2d {
    val (_, root) = getPlayerCanvasRoot(playerName)
    return root.canvas2d
  }

  /**
   * function(player:string):Origin3d -- Gets the 3D canvas methods for a player in the same world that the hologram
   *   peripheral is in.
   */
  @LuaFunction(unsafe = true)
  fun getCanvas3d(playerName: String): WrappedOrigin3d {
    val (_, root) = getPlayerCanvasRoot(playerName)
    return WrappedOrigin3d(this, root.canvas3d)
  }

  /**
   * function() -- Clear all canvases in the server
   */
  @LuaFunction(unsafe = true)
  fun clearAllCanvasesGlobally(): MethodResult {
    CanvasHandlerServer.removeAllRoots()
    return MethodResult.of(true)
  }

  /**
   * function(player:string, includeMouseMove:boolean) -- Starts capturing all mouse and keyboard inputs for a player.
   * If `includeMouseMove` is `true`, then `hologram_mouse_move` events will also be fired.
   */
  @LuaFunction(unsafe = true)
  fun startCapture(playerName: String, includeMouseMove: Optional<Boolean>): MethodResult {
    val (player, root) = getPlayerCanvasRoot(playerName)
    root.startCapture(this, player, includeMouseMove.orElse(false))
    return MethodResult.of(true)
  }

  /**
   * function(player:string) -- Stops capturing all mouse and keyboard inputs for a player.
   */
  @LuaFunction(unsafe = true)
  fun stopCapture(playerName: String): MethodResult {
    val (player, root) = getPlayerCanvasRoot(playerName)
    root.stopCapture(this, player, true)
    return MethodResult.of(true)
  }

  /**
   * function(player:string, keyCode:number) -- Starts capturing a specific keyboard input for a player, even during
   *   gameplay.
   */
  @LuaFunction(unsafe = true)
  fun startKeyCapture(playerName: String, keyCode: Int): MethodResult {
    assertIntBetweenImpl(keyCode, 0, MAX_KEY_CODE, "key code out of bounds (%s)")
    val (player, root) = getPlayerCanvasRoot(playerName)
    root.startKeyCapture(this, player, keyCode)
    return MethodResult.of(true)
  }

  /**
   * function(player:string, keyCode:number) -- Stops capturing a specific keyboard input for a player.
   */
  @LuaFunction(unsafe = true)
  fun stopKeyCapture(playerName: String, keyCode: Int): MethodResult {
    assertIntBetweenImpl(keyCode, 0, MAX_KEY_CODE, "key code out of bounds (%s)")
    val (player, root) = getPlayerCanvasRoot(playerName)
    root.stopKeyCapture(this, player, keyCode)
    return MethodResult.of(true)
  }

  /**
   * function(player:string) -- Clear all key captures for a player (does not stop capture mode).
   */
  @LuaFunction(unsafe = true)
  fun clearKeyCaptures(playerName: String): MethodResult {
    val (player, root) = getPlayerCanvasRoot(playerName)
    root.clearKeyCaptures(this, player)
    return MethodResult.of(true)
  }

  override fun attach(computer: IComputerAccess) {
    attachedComputers.add(computer)
  }

  override fun detach(computer: IComputerAccess) {
    attachedComputers.remove(computer)
  }

  fun queueEvent(event: String, vararg args: Any) {
    attachedComputers.queueEvent(event, *args)
  }

  fun onBreak() {
    CanvasHandlerServer.removeListener(this)
  }

  override fun getType(): String =
    CCHolo.Peripherals.HOLOGRAM_PERIPHERAL_ID.path

  override fun equals(other: IPeripheral?) =
    other is HologramPeripheral && blockEntity == other.blockEntity
}
