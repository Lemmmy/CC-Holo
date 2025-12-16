package sh.lem.ccholo.peripheral

import dan200.computercraft.api.lua.*
import dan200.computercraft.api.peripheral.IComputerAccess
import dan200.computercraft.api.peripheral.IPeripheral
import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.server.ServerLifecycleHooks
import sh.lem.ccholo.CCHolo
import sh.lem.ccholo.canvas.CanvasHandlerServer
import sh.lem.ccholo.canvas.CanvasRoot.Companion.MAX_KEY_CODE
import sh.lem.ccholo.canvas.CanvasRootServer
import sh.lem.ccholo.canvas.DEFAULT_RAYCAST_RANGE
import sh.lem.ccholo.networking.S2CCanvasOpenLinkPacket
import sh.lem.ccholo.networking.S2CCanvasSetClipboardPacket
import sh.lem.ccholo.objects.object2d.RootFrame2d
import sh.lem.ccholo.objects.object3d.WrappedOrigin3d
import sh.lem.ccholo.util.*
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

    val player = blockEntity.level?.server?.playerList?.players
      ?.firstOrNull { it.gameProfile.id == uuid || it.gameProfile.name.lowercase() == name }
      ?: throw LuaException("Player '$nameOrUuid' not found")

    val root = CanvasHandlerServer.getRootForPlayer(player)
    root.addListener(this)
    return Pair(player, root)
  }

  /**
   * function(player:string):RootFrame2d -- Gets the 2D canvas methods for a player.
   */
  @LuaFunction(mainThread = true)
  fun getCanvas2d(playerName: String): RootFrame2d {
    val (_, root) = getPlayerCanvasRoot(playerName)
    return root.canvas2d
  }

  /**
   * function(player:string):Origin3d -- Gets the 3D canvas methods for a player in the same world that the hologram
   *   peripheral is in.
   */
  @LuaFunction(mainThread = true)
  fun getCanvas3d(playerName: String): WrappedOrigin3d {
    val (_, root) = getPlayerCanvasRoot(playerName)
    return WrappedOrigin3d(this, root.canvas3d)
  }

  /**
   * function() -- Clear all canvases in the server
   */
  @LuaFunction(mainThread = true)
  fun clearAllCanvasesGlobally() {
    CanvasHandlerServer.clearAllRoots()
  }

  /**
   * function(player:string[, includeMouseMove:boolean[, hideMouse:boolean[, hideText:string]]]])
   * -- Starts capturing all mouse and keyboard inputs for a player.
   * If `includeMouseMove` is `true`, then `hologram_mouse_move` events will also be fired.
   * If `hideMouse` is `true`, then the mouse cursor will be hidden.
   * If `hideText` is `true`, then the text that tells the player how to exit capture mode will be hidden. Use with
   *   caution, as it may confuse players who do not realise they are in capture mode.
   */
  @LuaFunction(mainThread = true)
  fun startCapture(
    playerName: String,
    includeMouseMove: Optional<Boolean>,
    hideMouse: Optional<Boolean>,
    hideText: Optional<Boolean>
  ) {
    val (player, root) = getPlayerCanvasRoot(playerName)
    root.startCapture(player, includeMouseMove.orElse(false), hideMouse.orElse(false), hideText.orElse(false))
  }

  /**
   * function(player:string) -- Stops capturing all mouse and keyboard inputs for a player.
   */
  @LuaFunction(mainThread = true)
  fun stopCapture(playerName: String) {
    val (player, root) = getPlayerCanvasRoot(playerName)
    root.stopCapture(player, true)
  }

  /**
   * function(player:string, keyCode:number) -- Starts capturing a specific keyboard input for a player, even during
   *   gameplay.
   */
  @LuaFunction(mainThread = true)
  fun startKeyCapture(playerName: String, keyCode: Int) {
    assertIntBetweenImpl(keyCode, 0, MAX_KEY_CODE, "key code out of bounds (%s)")
    val (player, root) = getPlayerCanvasRoot(playerName)
    root.startKeyCapture(player, keyCode)
  }

  /**
   * function(player:string, keyCode:number) -- Stops capturing a specific keyboard input for a player.
   */
  @LuaFunction(mainThread = true)
  fun stopKeyCapture(playerName: String, keyCode: Int) {
    assertIntBetweenImpl(keyCode, 0, MAX_KEY_CODE, "key code out of bounds (%s)")
    val (player, root) = getPlayerCanvasRoot(playerName)
    root.stopKeyCapture(player, keyCode)
  }

  /**
   * function(player:string) -- Clear all key captures for a player (does not stop capture mode).
   */
  @LuaFunction(mainThread = true)
  fun clearKeyCaptures(playerName: String) {
    val (player, root) = getPlayerCanvasRoot(playerName)
    root.clearKeyCaptures(player)
  }

  /**
   * function(player:string, clipboard:string) -- Sets the clipboard for a player.
   */
  @LuaFunction(mainThread = true)
  fun setClipboard(args: IArguments) {
    val playerName = args.getString(0)
    val clipboard = args.assertUtf8StringLength(1, 1, S2CCanvasSetClipboardPacket.PASTE_LIMIT)

    val (player, root) = getPlayerCanvasRoot(playerName)
    root.setClipboard(player, clipboard)
  }

  /**
   * function(player:string, url:string) -- Prompts a player to open the given URL.
   */
  @LuaFunction(mainThread = true)
  fun openLink(args: IArguments) {
    val playerName = args.getString(0)
    val url = args.assertUtf8StringLength(1, 1, S2CCanvasOpenLinkPacket.URL_LIMIT)

    val (player, root) = getPlayerCanvasRoot(playerName)
    root.openLink(player, url)
  }

  /**
   * function(player:string):string,number -- Returns a player's timezone, as an IANA timezone ID string, and a
   *   UTC offset in seconds.
   */
  @LuaFunction(mainThread = true)
  fun getTimezone(playerName: String): MethodResult {
    val (_, root) = getPlayerCanvasRoot(playerName)
    return MethodResult.of(root.timezone, root.timezoneOffsetSeconds)
  }

  /**
   * function(player:string[, range:number=20]) -- Requests a raycast for a player asynchronously.
   * The raycast will be requested asynchronously and the result will be returned in a hologram_raycast event.
   *   requestRaycast will return the player name, UUID, and request ID.
   * If the player goes offline or doesn't respond to the raycast request, it will time out after 40 ticks and return a
   *   nil table.
   */
  @LuaFunction(mainThread = true)
  fun requestRaycastAsync(
    playerName: String,
    range: Optional<Double>,
    computer: IComputerAccess,
  ): MethodResult {
    val (player, _) = getPlayerCanvasRoot(playerName)
    val raycast = CanvasHandlerServer.requestRaycast(player, range.orElse(DEFAULT_RAYCAST_RANGE), computer)
    return MethodResult.of(player.gameProfile.name, player.gameProfile.id.toString(), raycast.requestId)
  }

  /**
   * function(player:string[, range:number=20]) -- Requests a raycast for a player.
   * Will yield and wait for the result from the player, and return the result table or `nil`.
   * If the player goes offline or doesn't respond to the raycast request, it will time out after 40 ticks and return a
   *   nil table.
   */
  @LuaFunction
  fun requestRaycast(
    playerName: String,
    range: Optional<Double>,
    ctx: ILuaContext,
    computer: IComputerAccess,
  ): MethodResult {
    // Since this is off-thread, we can't check the player yet. Create the raycast request, then queue a separate
    // main-thread task to fetch the player and send it. Cancel the request immediately if the player goes offline.
    val raycast = CanvasHandlerServer.requestRaycast(null, range.orElse(DEFAULT_RAYCAST_RANGE), computer)

    ServerLifecycleHooks.getCurrentServer().executeIfPossible {
      val (player, _) = getPlayerCanvasRoot(playerName)
      raycast.player = player
      raycast.send()
    }

    return RaycastTaskCallback.make(ctx, raycast)
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
