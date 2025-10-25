package sh.lem.ccholo.canvas

import net.minecraft.client.Minecraft
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.client.event.ClientPlayerNetworkEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import sh.lem.ccholo.CCHolo
import sh.lem.ccholo.networking.S2CCanvasCaptureStatePacket
import sh.lem.ccholo.networking.S2CCanvasInitPacket
import sh.lem.ccholo.networking.S2CCanvasRemovePacket
import sh.lem.ccholo.networking.S2CCanvasUpdatePacket

@Mod.EventBusSubscriber(Dist.CLIENT)
object CanvasHandlerClient {
  /// Client packets should already be running on the render thread (which has authority over the canvas root)
  private fun checkMainThread(name: String): Boolean {
    if (!Minecraft.getInstance().isSameThread) {
      CCHolo.log.error("$name ran off-thread, refusing!")
      return false
    }
    return true
  }

  internal fun onCanvasInitPacket(msg: S2CCanvasInitPacket) {
    if (!checkMainThread("S2CCanvasInitPacket")) return

    CCHolo.log.debug("Received canvas init packet for canvas ${msg.canvasId} with ${msg.objects.size} objects")

    val root = CanvasRootClient
    root.initialise()
    msg.objects.onEach(root::updateObject)
    root.updateCaptureState(
      capturing = msg.capturing,
      capturingMouseMove = msg.capturingMouseMove,
      keyCaptures = msg.keyCaptures
    )
  }

  internal fun onCanvasRemovePacket(msg: S2CCanvasRemovePacket) {
    if (!checkMainThread("S2CCanvasRemovePacket")) return

    CanvasRootClient.initialise()
  }

  internal fun onCanvasUpdatePacket(msg: S2CCanvasUpdatePacket) {
    if (!checkMainThread("S2CCanvasUpdatePacket")) return

    val root = CanvasRootClient
    msg.changed.onEach(root::updateObject)
    msg.removed.onEach(root::remove)
  }

  internal fun onCanvasCaptureStatePacket(msg: S2CCanvasCaptureStatePacket) {
    if (!checkMainThread("S2CCanvasCaptureStatePacket")) return

    val root = CanvasRootClient
    root.updateCaptureState(
      capturing = msg.capturing,
      capturingMouseMove = msg.capturingMouseMove,
      keyCaptures = msg.keyCaptures
    )
  }

  @SubscribeEvent
  fun onLogIn(event: ClientPlayerNetworkEvent.LoggingIn) {
    // Set up a fresh canvas root when joining a world, before any canvas packets arrive from the server
    CCHolo.log.debug("Logging in, initialising canvas root")
    CanvasRootClient.initialise()
  }

  @SubscribeEvent
  fun onLogOut(event: ClientPlayerNetworkEvent.LoggingOut) {
    // Just in case, clear the canvas root when leaving a server too, since there's no need to keep old objects around
    // in memory after leaving
    CCHolo.log.debug("Logging out, clearing canvas root")
    CanvasRootClient.initialise()
  }
}
