package sh.lem.ccholo.canvas

import net.minecraft.Util
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.ConfirmLinkScreen
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.client.event.ClientPlayerNetworkEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import sh.lem.ccholo.CCHolo
import sh.lem.ccholo.networking.*
import sh.lem.ccholo.objects.renderers.FramebufferPool
import java.net.URI

@Mod.EventBusSubscriber(Dist.CLIENT)
object CanvasHandlerClient {
  private val mc by lazy { Minecraft.getInstance() }

  /// Client packets should already be running on the render thread (which has authority over the canvas root)
  private fun checkMainThread(name: String): Boolean {
    if (!mc.isSameThread) {
      CCHolo.log.error("$name ran off-thread, refusing!")
      return false
    }
    return true
  }

  internal fun onCanvasInitPacket(msg: S2CCanvasInitPacket) {
    if (!checkMainThread("S2CCanvasInitPacket")) return

    CCHolo.log.debug("Received canvas init packet for canvas ${msg.canvasId} with ${msg.objects.size} objects")

    val root = CanvasRootClient
    root.reset()
    msg.objects.onEach(root::updateObject)
    root.updateCaptureState(
      capturing = msg.capturing,
      capturingMouseMove = msg.capturingMouseMove,
      hidingMouse = msg.hidingMouse,
      hidingText = msg.hidingText,
      keyCaptures = msg.keyCaptures
    )
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
      hidingMouse = msg.hidingMouse,
      hidingText = msg.hidingText,
      keyCaptures = msg.keyCaptures
    )
  }

  internal fun onCanvasSetClipboardPacket(msg: S2CCanvasSetClipboardPacket) {
    if (!checkMainThread("S2CCanvasSetClipboardPacket")) return

    try {
      mc.keyboardHandler.clipboard = msg.text
    } catch (e: Exception) {
      CCHolo.log.error("Error while setting clipboard", e)
    }
  }

  internal fun onCanvasOpenLinkPacket(msg: S2CCanvasOpenLinkPacket) {
    if (!checkMainThread("S2CCanvasOpenLinkPacket")) return

    try {
      val uri = URI.create(msg.url)
      val parent = mc.screen

      mc.setScreen(ConfirmLinkScreen({ open: Boolean ->
        if (open) Util.getPlatform().openUri(uri)
        mc.setScreen(parent)
      }, msg.url, false))
    } catch (e: Exception) {
      CCHolo.log.error("Error while opening link", e)
    }
  }

  @SubscribeEvent
  fun onLogIn(event: ClientPlayerNetworkEvent.LoggingIn) {
    // Set up a fresh canvas root when joining a world, before any canvas packets arrive from the server
    CCHolo.log.debug("Logging in, initialising canvas root")
    CanvasRootClient.reset()
    CanvasRootClient.sendScreenSizePacket()
  }

  @SubscribeEvent
  fun onLogOut(event: ClientPlayerNetworkEvent.LoggingOut) {
    // Just in case, clear the canvas root when leaving a server too, since there's no need to keep old objects around
    // in memory after leaving
    CCHolo.log.debug("Logging out, clearing canvas root")
    CanvasRootClient.reset()
    FramebufferPool.clear()
  }
}
