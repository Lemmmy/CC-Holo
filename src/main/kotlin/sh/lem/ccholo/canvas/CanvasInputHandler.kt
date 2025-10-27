package sh.lem.ccholo.canvas

import net.minecraft.client.Minecraft
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.client.event.InputEvent
import net.minecraftforge.event.TickEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import org.lwjgl.glfw.GLFW.GLFW_PRESS
import org.lwjgl.glfw.GLFW.GLFW_REPEAT
import sh.lem.ccholo.networking.C2SCanvasCaptureKeyPacket
import sh.lem.ccholo.networking.send

@Mod.EventBusSubscriber(Dist.CLIENT)
object CanvasInputHandler {
  private val mc by lazy { Minecraft.getInstance() }

  @SubscribeEvent
  fun onClientTick(event: TickEvent.ClientTickEvent) {
    if (event.phase === TickEvent.Phase.END && mc.level != null) {
      val root = CanvasRootClient

      // Open the key capture screen if an open is pending
      if (root.capturePendingOpen && mc.screen == null) {
        mc.setScreen(CanvasCapturingScreen())
        root.capturePendingOpen = false
      }

      // Send screen size packet if the screen size has changed
      root.updateScreenSize()
      if (root.pollScreenSizeDirty()) {
        root.sendScreenSizePacket()
      }
    }
  }

  @SubscribeEvent
  fun onKeyInput(event: InputEvent.Key) {
    // Forward subscribed key captures
    if (mc.screen == null && CanvasRootClient.keyCaptures.contains(event.key)) {
      C2SCanvasCaptureKeyPacket(
        key = event.key,
        down = event.action == GLFW_PRESS || event.action == GLFW_REPEAT,
        repeat = event.action == GLFW_REPEAT,
        captureMode = false
      ).send()
    }
  }
}
